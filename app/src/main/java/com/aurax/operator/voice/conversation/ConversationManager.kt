package com.aurax.operator.voice.conversation

import android.util.Log
import com.aurax.operator.voice.wakeword.WakeWordManager
import com.aurax.operator.voice.stt.SpeechToTextEngine
import com.aurax.operator.voice.tts.TextToSpeechEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages continuous conversation, including wake word detection,
 * speech-to-text, text-to-speech, and barge-in.
 */
class ConversationManager(
    private val wakeWordManager: WakeWordManager,
    private val sttEngine: SpeechToTextEngine,
    private val ttsEngine: TextToSpeechEngine,
    private val settings: VoiceSettings
) {
    
    enum class State {
        IDLE,
        LISTENING,
        PROCESSING,
        SPEAKING
    }
    
    private var state = State.IDLE
    private var isBargeInEnabled = false
    
    init {
        // Set up barge-in callback
        if (ttsEngine is AndroidTTSEngine) {
            (ttsEngine as AndroidTTSEngine).setOnBargeIn {
                handleBargeIn()
            }
        }
    }
    
    /**
     * Starts continuous conversation mode.
     */
    fun start() {
        if (!settings.continuousConversationEnabled) {
            Log.d("ConversationManager", "Continuous conversation is disabled")
            return
        }
        
        // Set barge-in enabled based on settings
        setBargeInEnabled(settings.bargeInEnabled)
        
        wakeWordManager.startListening {
            onWakeWordDetected()
        }
        state = State.IDLE
        Log.d("ConversationManager", "Continuous conversation started")
    }
    
    /**
     * Stops continuous conversation mode.
     */
    fun stop() {
        wakeWordManager.stopListening()
        sttEngine.stop()
        ttsEngine.stop()
        state = State.IDLE
        Log.d("ConversationManager", "Continuous conversation stopped")
    }
    
    /**
     * Called when the wake word is detected.
     */
    private fun onWakeWordDetected() {
        if (state != State.IDLE) return
        
        Log.d("ConversationManager", "Wake word detected")
        startListening()
    }
    
    /**
     * Starts listening for user input.
     */
    private fun startListening() {
        state = State.LISTENING
        sttEngine.startListening { transcript ->
            onTranscriptReceived(transcript)
        }
    }
    
    /**
     * Called when a transcript is received from STT.
     */
    private fun onTranscriptReceived(transcript: String) {
        if (state != State.LISTENING) return
        
        Log.d("ConversationManager", "Transcript received: $transcript")
        state = State.PROCESSING
        
        // Process the transcript (e.g., send to LLM)
        CoroutineScope(Dispatchers.IO).launch {
            val response = processTranscript(transcript)
            speakResponse(response)
        }
    }
    
    /**
     * Processes the user's transcript and generates a response.
     */
    private suspend fun processTranscript(transcript: String): String {
        // TODO: Replace with actual LLM processing
        return "I received: $transcript"
    }
    
    /**
     * Speaks the response using TTS.
     */
    private fun speakResponse(response: String) {
        state = State.SPEAKING
        CoroutineScope(Dispatchers.Main).launch {
            ttsEngine.speak(response)
        }
    }
    
    /**
     * Handles barge-in (interrupting TTS to listen).
     */
    fun handleBargeIn() {
        if (!isBargeInEnabled || state != State.SPEAKING) return
        
        Log.d("ConversationManager", "Barge-in detected")
        ttsEngine.stop()
        startListening()
    }
    
    /**
     * Enables or disables barge-in.
     */
    fun setBargeInEnabled(enabled: Boolean) {
        isBargeInEnabled = enabled
        ttsEngine.setBargeInEnabled(enabled)
    }
    
    /**
     * Gets the current conversation state.
     */
    fun getState(): State = state
    
    /**
     * Updates voice settings.
     */
    fun updateSettings(newSettings: VoiceSettings) {
        // Stop current conversation if running
        if (state != State.IDLE) {
            stop()
        }
        
        // Update settings
        wakeWordManager.updateSettings(newSettings.wakeWordSettings)
        setBargeInEnabled(newSettings.bargeInEnabled)
        
        // Restart if needed
        if (newSettings.continuousConversationEnabled) {
            start()
        }
    }
    
    /**
     * Releases resources.
     */
    fun release() {
        stop()
        wakeWordManager.release()
    }
}

/**
 * Settings for voice and conversation.
 */
data class VoiceSettings(
    val continuousConversationEnabled: Boolean = false,
    val wakeWordSettings: WakeWordSettings = WakeWordSettings(),
    val bargeInEnabled: Boolean = true,
    val sttModel: String = "whisper-base",
    val ttsVoice: String = "en-US",
    val language: String = "en"
)
