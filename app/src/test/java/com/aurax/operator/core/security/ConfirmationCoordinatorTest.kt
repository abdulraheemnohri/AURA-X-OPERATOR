package com.aurax.operator.core.security

import com.aurax.operator.core.app.AppState
import com.aurax.operator.core.app.OperatorPhase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConfirmationCoordinatorTest {
    @After
    fun resetState() {
        AppState.reset()
    }

    @Test
    fun confirmWithoutPendingActionDoesNotAdvancePhase() = runBlocking {
        val coordinator = ConfirmationCoordinator(PendingActionStore())

        assertNull(coordinator.confirmPendingForTest())
        assertEquals(OperatorPhase.IDLE, AppState.operator.value.phase)
    }

    @Test
    fun confirmConsumesPendingActionAndEntersExecuting() = runBlocking {
        val store = PendingActionStore()
        val coordinator = ConfirmationCoordinator(store)
        val action = PendingActionStore.PendingAction(7L, "delete test", "com.example")
        store.set(action)

        val confirmed = coordinator.confirm()

        assertEquals(action, confirmed)
        assertEquals(OperatorPhase.EXECUTING, AppState.operator.value.phase)
        assertNull(store.get())
    }

    private suspend fun ConfirmationCoordinator.confirmPendingForTest(): PendingActionStore.PendingAction? = confirm()
}
