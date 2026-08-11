package com.aurax.operator.operator

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Process-wide safety state. Abort is monotonic until a new operator session begins. */
enum class OperatorIndicatorState { OBSERVING, COUNTDOWN, ACTING, BLOCKED, ABORTED }

data class CountdownState(val action: String, val remainingSeconds: Int)

object OperatorRuntime {
    @Volatile var aborted = false
        private set

    private val _indicator = MutableStateFlow(OperatorIndicatorState.OBSERVING)
    val indicator: StateFlow<OperatorIndicatorState> = _indicator.asStateFlow()

    private val _countdown = MutableStateFlow<CountdownState?>(null)
    val countdown: StateFlow<CountdownState?> = _countdown.asStateFlow()

    fun begin() {
        aborted = false
        _countdown.value = null
        _indicator.value = OperatorIndicatorState.OBSERVING
    }

    fun acting() {
        if (!aborted) _indicator.value = OperatorIndicatorState.ACTING
    }

    fun blocked() {
        _countdown.value = null
        _indicator.value = OperatorIndicatorState.BLOCKED
    }

    fun abort() {
        aborted = true
        _countdown.value = null
        _indicator.value = OperatorIndicatorState.ABORTED
    }

    fun ensureNotAborted() {
        check(!aborted) { "AURA-X operation aborted by user" }
    }

    /** Three-second visible safety window. Tapping the orb or pressing Volume Down aborts it. */
    suspend fun safetyCountdown(action: String, seconds: Int = 3): Boolean {
        if (aborted) return false
        _indicator.value = OperatorIndicatorState.COUNTDOWN
        for (remaining in seconds downTo 1) {
            ensureNotAborted()
            _countdown.value = CountdownState(action, remaining)
            delay(1_000)
        }
        _countdown.value = null
        ensureNotAborted()
        _indicator.value = OperatorIndicatorState.ACTING
        return true
    }
}
