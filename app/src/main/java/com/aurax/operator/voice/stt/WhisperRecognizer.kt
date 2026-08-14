package com.aurax.operator.voice.stt

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.max

/** Offline Whisper.cpp speech recognizer. Falls back to the Android recognizer at the UI layer when unavailable. */
class WhisperRecognizer {
    companion object {
        private const val SAMPLE_RATE = 16_000
        private const val CHANNELS = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val MAX_SECONDS = 30
        private val nativeLoaded = runCatching {
            System.loadLibrary("aurax_whisper")
            true
        }.getOrDefault(false)

        @JvmStatic
        private external fun nativeTranscribe(
            modelPath: String,
            pcm: FloatArray,
            language: String,
            threads: Int
        ): String
    }

    @Volatile
    private var running = false
    private var recorder: AudioRecord? = null
    private var worker: Thread? = null

    fun isAvailable(): Boolean = nativeLoaded

    @Synchronized
    fun start(modelPath: String, language: String, onText: (String) -> Unit): Boolean {
        if (!nativeLoaded || running || !File(modelPath).isFile) return false

        val minimum = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNELS, ENCODING)
        if (minimum <= 0) return false
        val bufferSize = max(minimum, 8_192)
        val audioRecord = runCatching {
            AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(ENCODING)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNELS)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()
        }.getOrNull() ?: return false

        return try {
            audioRecord.startRecording()
            if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.release()
                false
            } else {
                recorder = audioRecord
                running = true
                worker = thread(name = "aura-whisper") {
                    val maxSamples = SAMPLE_RATE * MAX_SECONDS
                    val pcm = FloatArray(maxSamples)
                    val shortBuffer = ShortArray(2_048)
                    var offset = 0

                    try {
                        while (running && offset < maxSamples) {
                            val read = audioRecord.read(shortBuffer, 0, shortBuffer.size, AudioRecord.READ_BLOCKING)
                            if (read <= 0) break
                            val copy = minOf(read, maxSamples - offset)
                            for (i in 0 until copy) {
                                pcm[offset + i] = shortBuffer[i] / 32768.0f
                            }
                            offset += copy
                        }

                        if (offset > SAMPLE_RATE / 4) {
                            val result = runCatching {
                                nativeTranscribe(
                                    modelPath,
                                    pcm.copyOf(offset),
                                    language.ifBlank { "auto" },
                                    4
                                )
                            }.getOrDefault("").trim()
                            Handler(Looper.getMainLooper()).post { onText(result) }
                        } else {
                            Handler(Looper.getMainLooper()).post { onText("") }
                        }
                    } finally {
                        running = false
                        runCatching { audioRecord.stop() }
                        audioRecord.release()
                        synchronized(this@WhisperRecognizer) {
                            recorder = null
                            worker = null
                        }
                    }
                }
                true
            }
        } catch (_: Throwable) {
            runCatching { audioRecord.release() }
            running = false
            recorder = null
            worker = null
            false
        }
    }

    @Synchronized
    fun stop() {
        running = false
        recorder?.let { runCatching { it.stop() } }
    }
}
