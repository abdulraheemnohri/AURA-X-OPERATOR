package com.aurax.operator.voice.conversation

import android.util.Log
import com.aurax.operator.voice.stt.WhisperRecognizer
import com.aurax.operator.voice.tts.AndroidTTSEngine
import com.aurax.operator.voice.tts.TextToSpeechEngine
import com.aurax.operator.voice.wakeword.WakeWordManager
import com.aurax.operator.voice.wakeword.WakeWordSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Coordinates wake-word, STT and TTS engines. Runtime-specific adapters implement
 * SpeechToTextEngine; the conversation state machine itself remains engine-neutral.
 */
class ConversationManager(
    private val wakeWordManager: WakeWordManager,
    private val sttEngine: SpeechToTextEngine,
    private val ttsEngine: TextToSpeechEngine,
    private var settings: VoiceSettings
) {
    enum class State { IDLE, LISTENING, PROCESSING, SPEAKING }

    private var state = State.IDLE
    private var isBargeInEnabled = false

    init {
        if (ttsEngine is AndroidTTSEngine) {
            ttsEngine.setOnBargeIn { handleBargeIn() }
        }
    }

    fun start() {
        if (!settings.continuousConversationEnabled) return
        setBargeInEnabled(settings.bargeInEnabled)
        wakeWordManager.startListening()
        state = State.IDLE
        Log.d("ConversationManager", "Continuous conversation started")
    }

    fun stop() {
        wakeWordManager.stopListening()
        sttEngine.stop()
        ttsEngine.stop()
        state = State.IDLE
    }

    fun onWakeWordDetected() {
        if (state != State.IDLE) return
        startListening()
    }

    private fun startListening() {
        state = State.LISTENING
        if (!sttEngine.startListening { transcript -> onTranscriptReceived(transcript) }) {
            state = State.IDLE
            Log.w("ConversationManager", "STT runtime is unavailable")
        }
    }

    private fun onTranscriptReceived(transcript: String) {
        if (state != State.LISTENING) return
        state = State.PROCESSING
        CoroutineScope(Dispatchers.IO).launch {
            val response = processTranscript(transcript)
            speakResponse(response)
        }
    }

    private suspend fun processTranscript(transcript: String): String =
        "I received: $transcript"

    private fun speakResponse(response: String) {
        state = State.SPEAKING
        CoroutineScope(Dispatchers.Main).launch { ttsEngine.speak(response) }
    }

    fun handleBargeIn() {
        if (!isBargeInEnabled || state != State.SPEAKING) return
        ttsEngine.stop()
        startListening()
    }

    fun setBargeInEnabled(enabled: Boolean) {
        isBargeInEnabled = enabled
        ttsEngine.setBargeInEnabled(enabled)
    }

    fun getState(): State = state

    fun updateSettings(newSettings: VoiceSettings) {
        if (state != State.IDLE) stop()
        settings = newSettings
        wakeWordManager.updateSettings(newSettings.wakeWordSettings)
        setBargeInEnabled(newSettings.bargeInEnabled)
        if (newSettings.continuousConversationEnabled) start()
    }

    fun release() {
        stop()
        wakeWordManager.release()
    }
}

/** Engine-neutral STT contract used by the conversation state machine. */
interface SpeechToTextEngine {
    fun startListening(onTranscript: (String) -> Unit): Boolean
    fun stop()
    fun isAvailable(): Boolean
}

/** Adapter for the real local Whisper recognizer. */
class WhisperSpeechToTextEngine(
    private val recognizer: WhisperRecognizer,
    private val modelPathProvider: () -> String?,
    private val languageProvider: () -> String
) : SpeechToTextEngine {
    override fun startListening(onTranscript: (String) -> Unit): Boolean {
        val path = modelPathProvider() ?: return false
        return recognizer.start(path, languageProvider(), onTranscript)
    }

    override fun stop() = recognizer.stop()
    override fun isAvailable(): Boolean = recognizer.isRuntimeAvailable() && modelPathProvider()?.let(recognizer::isAvailable) == true
}

data class VoiceSettings(
    val continuousConversationEnabled: Boolean = false,
    val wakeWordSettings: WakeWordSettings = WakeWordSettings(),
    val bargeInEnabled: Boolean = true,
    val sttModel: String = "whisper-base",
    val ttsVoice: String = "en-US",
    val language: String = "en"
)
