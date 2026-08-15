package com.aurax.operator.voice.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

/**
 * Android TTS engine implementation with barge-in support.
 */
class AndroidTTSEngine(
    private val context: Context
) : TextToSpeechEngine {
    
    private var tts: TextToSpeech? = null
    private var isBargeInEnabled = false
    private var isInitialized = false
    private var onBargeIn: (() -> Unit)? = null
    
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
            val utteranceId = UUID.randomUUID().toString()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    Log.d("AndroidTTSEngine", "TTS started: $utteranceId")
                }
                
                override fun onDone(utteranceId: String) {
                    Log.d("AndroidTTSEngine", "TTS completed: $utteranceId")
                }
                
                override fun onError(utteranceId: String) {
                    Log.e("AndroidTTSEngine", "TTS error: $utteranceId")
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
    
    override fun stop() {
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
