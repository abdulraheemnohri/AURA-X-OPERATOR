package com.aurax.operator.voice.tts

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.UUID

/** Android TTS engine implementation with barge-in support. */
class AndroidTTSEngine(
    private val context: Context
) : TextToSpeechEngine {
    private var tts: TextToSpeech? = null
    private var isBargeInEnabled = false
    private var isInitialized = false
    private var onBargeIn: (() -> Unit)? = null
    private var audioRecord: AudioRecord? = null
    private var isListeningForBargeIn = false
    private val audioBufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    init { initializeTTS() }

    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
            Log.d("AndroidTTSEngine", if (isInitialized) "TTS initialized successfully" else "TTS initialization failed")
        }
    }

    override suspend fun speak(text: String) {
        if (!isInitialized) {
            Log.e("AndroidTTSEngine", "TTS not initialized")
            return
        }
        val utteranceId = UUID.randomUUID().toString()
        if (isBargeInEnabled) {
            startBargeInListener()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) = Unit
                override fun onDone(utteranceId: String) { stopBargeInListener() }
                override fun onError(utteranceId: String) { stopBargeInListener() }
                override fun onRangeStart(utteranceId: String, start: Int, end: Int, frame: Int) = Unit
            })
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }, utteranceId)
        } else {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    private fun startBargeInListener() {
        if (!isBargeInEnabled || isListeningForBargeIn || audioBufferSize <= 0) return
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioBufferSize
            )
            audioRecord?.startRecording()
            isListeningForBargeIn = true
            Thread {
                val buffer = ShortArray(audioBufferSize)
                while (isListeningForBargeIn) {
                    val samples = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (samples > 0) {
                        val amplitude = buffer.take(samples).maxOfOrNull { kotlin.math.abs(it.toInt()) } ?: 0
                        if (amplitude > 5000) {
                            onBargeIn?.invoke()
                            break
                        }
                    }
                }
            }.start()
        } catch (e: Exception) {
            Log.e("AndroidTTSEngine", "Failed to start barge-in listener", e)
            isListeningForBargeIn = false
        }
    }

    private fun stopBargeInListener() {
        isListeningForBargeIn = false
        runCatching { audioRecord?.stop() }
        runCatching { audioRecord?.release() }
        audioRecord = null
    }

    override fun stop() {
        stopBargeInListener()
        tts?.stop()
    }

    override fun isSpeaking(): Boolean = tts?.isSpeaking ?: false
    override fun isAvailable(): Boolean = isInitialized
    override fun setBargeInEnabled(enabled: Boolean) { isBargeInEnabled = enabled }
    fun setOnBargeIn(callback: () -> Unit) { onBargeIn = callback }
    fun shutdown() { stop(); tts?.shutdown(); isInitialized = false }
}

interface TextToSpeechEngine {
    suspend fun speak(text: String)
    fun stop()
    fun isSpeaking(): Boolean
    fun isAvailable(): Boolean
    fun setBargeInEnabled(enabled: Boolean)
}
