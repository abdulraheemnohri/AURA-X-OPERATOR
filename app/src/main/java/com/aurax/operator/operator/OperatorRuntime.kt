package com.aurax.operator.operator

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    /** Immediate process-wide emergency stop. Safe to call repeatedly from UI, voice or hardware handlers. */
    fun emergencyStop() = abort()

    fun abort() {
        aborted = true
        _countdown.value = null
        _indicator.value = OperatorIndicatorState.ABORTED
    }

    fun ensureNotAborted() {
        check(!aborted) { "AURA-X operation aborted by user" }
    }

    suspend fun safetyCountdown(action: String, seconds: Int = 3): Boolean {
        if (aborted) return false
        val safeSeconds = seconds.coerceIn(1, 10)
        _indicator.value = OperatorIndicatorState.COUNTDOWN
        for (remaining in safeSeconds downTo 1) {
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
