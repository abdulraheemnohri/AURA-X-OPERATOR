package com.aurax.operator.voice.tts

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

/**
 * Android TTS engine implementation with barge-in support.
 * Uses AudioRecord to detect speech during TTS playback.
 */
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
    
    init {
        initializeTTS()
    }
    
    /**
     * Initializes the TTS engine.
     */
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                Log.d("AndroidTTSEngine", "TTS initialized successfully")
            } else {
                Log.e("AndroidTTSEngine", "TTS initialization failed")
                isInitialized = false
            }
        }
    }
    
    override suspend fun speak(text: String) {
        if (!isInitialized) {
            Log.e("AndroidTTSEngine", "TTS not initialized")
            return
        }
        
        if (isBargeInEnabled) {
            startBargeInListener()
            val utteranceId = UUID.randomUUID().toString()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    Log.d("AndroidTTSEngine", "TTS started: $utteranceId")
                }
                
                override fun onDone(utteranceId: String) {
                    Log.d("AndroidTTSEngine", "TTS completed: $utteranceId")
                    stopBargeInListener()
                }
                
                override fun onError(utteranceId: String) {
                    Log.e("AndroidTTSEngine", "TTS error: $utteranceId")
                    stopBargeInListener()
                }
                
                override fun onRangeStart(utteranceId: String, start: Int, end: Int, frame: Int) {
                    // Not used for barge-in
                }
            })
            
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle().apply {
                putString(TextToSpeech.Engine.KEY_UTTERANCE_ID, utteranceId)
            }, utteranceId)
        } else {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    
    /**
     * Starts listening for barge-in (speech during TTS).
     */
    private fun startBargeInListener() {
        if (!isBargeInEnabled || isListeningForBargeIn) return
        
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
            
            // Start a background thread to monitor audio input
            Thread {
                val buffer = ShortArray(audioBufferSize)
                while (isListeningForBargeIn) {
                    val bytesRead = audioRecord?.read(buffer, 0, audioBufferSize) ?: 0
                    if (bytesRead > 0) {
                        // Simple voice detection: Check if the audio level exceeds a threshold
                        val amplitude = buffer.maxOrNull() ?: 0
                        if (amplitude > 5000) { // Threshold for speech detection
                            onBargeIn?.invoke()
                            break
                        }
                    }
                }
            }.start()
            
            Log.d("AndroidTTSEngine", "Barge-in listener started")
        } catch (e: Exception) {
            Log.e("AndroidTTSEngine", "Failed to start barge-in listener: ${e.message}")
            isListeningForBargeIn = false
        }
    }
    
    /**
     * Stops listening for barge-in.
     */
    private fun stopBargeInListener() {
        try {
            isListeningForBargeIn = false
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d("AndroidTTSEngine", "Barge-in listener stopped")
        } catch (e: Exception) {
            Log.e("AndroidTTSEngine", "Failed to stop barge-in listener: ${e.message}")
        }
    }
    
    override fun stop() {
        stopBargeInListener()
        tts?.stop()
        Log.d("AndroidTTSEngine", "TTS stopped")
    }
    
    override fun isSpeaking(): Boolean = tts?.isSpeaking ?: false
    
    override fun isAvailable(): Boolean = isInitialized
    
    override fun setBargeInEnabled(enabled: Boolean) {
        isBargeInEnabled = enabled
    }
    
    /**
     * Sets a callback for barge-in events.
     */
    fun setOnBargeIn(callback: () -> Unit) {
        onBargeIn = callback
    }
    
    /**
     * Releases TTS resources.
     */
    fun shutdown() {
        stopBargeInListener()
        tts?.shutdown()
        isInitialized = false
    }
}

/**
 * Interface for TTS engines.
 */
interface TextToSpeechEngine {
    suspend fun speak(text: String)
    fun stop()
    fun isSpeaking(): Boolean
    fun isAvailable(): Boolean
    fun setBargeInEnabled(enabled: Boolean)
}
