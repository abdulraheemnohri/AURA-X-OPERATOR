package com.aurax.operator.core.security

import com.aurax.operator.core.app.AppState
import com.aurax.operator.core.app.OperatorPhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ConfirmationCoordinator(
    private val pendingActions: PendingActionStore
) {
    private val mutex = Mutex()
    private var generation = 0L

    suspend fun request(action: PendingActionStore.PendingAction, seconds: Int = 3): Boolean = mutex.withLock {
        generation++
        val token = generation
        pendingActions.set(action)
        OperatorSafety.beginConfirmation(seconds)
        for (remaining in seconds downTo 1) {
            if (token != generation) return false
            AppState.setCountdown(remaining)
            delay(1000)
        }
        AppState.setCountdown(0)
        token == generation
    }

    suspend fun confirm(): PendingActionStore.PendingAction? = mutex.withLock {
        generation++
        AppState.setPhase(OperatorPhase.EXECUTING, "Confirmed")
        pendingActions.consume()
    }

    suspend fun abort(): PendingActionStore.PendingAction? = mutex.withLock {
        generation++
        AppState.setPhase(OperatorPhase.BLOCKED, "Action aborted")
        pendingActions.consume()
    }
}
