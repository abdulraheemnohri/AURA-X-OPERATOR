package com.aurax.operator.ai.model

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import java.util.concurrent.atomic.AtomicInteger

/**
 * Process-local concurrency gate for model transfers.
 *
 * WorkManager may schedule multiple uniquely-named model downloads at once. This
 * gate makes the user-facing maximum-parallel-download setting an actual runtime
 * limit instead of a value that only exists in preferences.
 */
object ModelDownloadConcurrencyGate {
    private val active = AtomicInteger(0)

    suspend fun <T> withPermit(maxParallel: Int, block: suspend () -> T): T {
        val limit = maxParallel.coerceIn(1, 2)
        while (true) {
            currentCoroutineContext().ensureActive()
            val current = active.get()
            if (current < limit && active.compareAndSet(current, current + 1)) break
            delay(250L)
        }

        return try {
            block()
        } finally {
            active.decrementAndGet()
        }
    }

    fun activeDownloads(): Int = active.get()
}
