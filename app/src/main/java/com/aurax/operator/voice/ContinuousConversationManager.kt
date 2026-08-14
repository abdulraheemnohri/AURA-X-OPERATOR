package com.aurax.operator.voice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Conversation state machine independent from the concrete STT/TTS engines.
 * This keeps wake, listen, think, speak and sleep transitions testable.
 */
class ContinuousConversationManager(
    private val scope: CoroutineScope,
    private val wakeWordDetector: WakeWordDetector,
    private val endOfSpeechTimeoutMs: Long = 2_000L,
    private val sleepTimeoutMs: Long = 10_000L
) {
    enum class State { SLEEPING, LISTENING, PROCESSING, SPEAKING }

    private val _state = MutableStateFlow(State.SLEEPING)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _lastTranscript = MutableStateFlow("")
    val lastTranscript: StateFlow<String> = _lastTranscript.asStateFlow()

    private var sleepJob: Job? = null
    private var speechJob: Job? = null

    fun start() {
        wakeWordDetector.setEnabled(true)
        _state.value = State.SLEEPING
        armSleepTimer()
    }

    fun stop() {
        speechJob?.cancel()
        sleepJob?.cancel()
        wakeWordDetector.setEnabled(false)
        _state.value = State.SLEEPING
    }

    fun onTranscript(text: String): Boolean {
        _lastTranscript.value = text
        if (_state.value == State.SLEEPING && wakeWordDetector.onTranscript(text)) {
            _state.value = State.LISTENING
            speechJob?.cancel()
            speechJob = scope.launch {
                delay(endOfSpeechTimeoutMs)
                if (_state.value == State.LISTENING) _state.value = State.PROCESSING
            }
            return true
        }
        if (_state.value == State.LISTENING) {
            speechJob?.cancel()
            speechJob = scope.launch {
                delay(endOfSpeechTimeoutMs)
                if (_state.value == State.LISTENING) _state.value = State.PROCESSING
            }
        }
        return false
    }

    fun markSpeaking() {
        _state.value = State.SPEAKING
        armSleepTimer()
    }

    fun markResponseComplete() {
        _state.value = State.LISTENING
        armSleepTimer()
    }

    fun markProcessing() {
        _state.value = State.PROCESSING
    }

    fun bargeIn() {
        _state.value = State.LISTENING
        sleepJob?.cancel()
    }

    private fun armSleepTimer() {
        sleepJob?.cancel()
        sleepJob = scope.launch {
            delay(sleepTimeoutMs)
            _state.value = State.SLEEPING
        }
    }
}
