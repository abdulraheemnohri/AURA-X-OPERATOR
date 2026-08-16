package com.aurax.operator.ai.model

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ModelDownloadConcurrencyGateTest {
    @Test
    fun maximumParallelDownloadsIsEnforced() = runBlocking {
        val active = AtomicInteger(0)
        val peak = AtomicInteger(0)

        (1..4).map {
            async {
                ModelDownloadConcurrencyGate.withPermit(2) {
                    val now = active.incrementAndGet()
                    peak.updateAndGet { current -> maxOf(current, now) }
                    delay(40)
                    active.decrementAndGet()
                }
            }
        }.awaitAll()

        assertEquals(0, active.get())
        assertEquals(2, peak.get())
    }
}
