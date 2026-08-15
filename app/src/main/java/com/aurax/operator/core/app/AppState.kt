package com.aurax.operator.core.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class OperatorPhase {
    IDLE, LISTENING, UNDERSTANDING, OBSERVING, THINKING, PLANNING, CONFIRMING, COUNTDOWN,
    EXECUTING, VERIFYING, RECOVERING, COMPLETED, BLOCKED, ABORTED, ERROR
}

data class OperatorUiState(
    val phase: OperatorPhase = OperatorPhase.IDLE,
    val currentTaskId: Long? = null,
    val currentStep: String = "",
    val progress: Float = 0f,
    val countdown: Int = 0,
    val message: String = "Ready",
    val abortRequested: Boolean = false,
    val accessibilityConnected: Boolean = false
)

/** Central observable state for the closed-loop operator lifecycle. */
object AppState {
    private val _operator = MutableStateFlow(OperatorUiState())
    val operator: StateFlow<OperatorUiState> = _operator.asStateFlow()

    fun update(transform: (OperatorUiState) -> OperatorUiState) { _operator.value = transform(_operator.value) }
    fun setPhase(phase: OperatorPhase, message: String = _operator.value.message) { update { it.copy(phase = phase, message = message) } }
    fun setStep(step: String, progress: Float) { update { it.copy(currentStep = step, progress = progress.coerceIn(0f, 1f)) } }
    fun setTask(taskId: Long?) { update { it.copy(currentTaskId = taskId) } }
    fun setCountdown(seconds: Int) { update { it.copy(countdown = seconds.coerceAtLeast(0)) } }
    fun requestAbort() { update { it.copy(abortRequested = true, phase = OperatorPhase.ABORTED, message = "Automation aborted") } }
    fun clearAbort() { update { it.copy(abortRequested = false) } }
    fun reset() { _operator.value = OperatorUiState() }
}
