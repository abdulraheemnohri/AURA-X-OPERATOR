package com.aurax.operator.nexus

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusCapabilityGateTest {
    @Test
    fun unavailableModelIsBlocked() {
        val gate = NexusCapabilityGate { NexusRuntimeAvailability() }
        assertFalse(gate.isAvailable("models.llama_cpp"))
    }

    @Test(expected = IllegalStateException::class)
    fun unavailableModelThrowsOnRequire() {
        val gate = NexusCapabilityGate { NexusRuntimeAvailability() }
        gate.requireAvailable("models.llama_cpp")
    }

    @Test
    fun installedModelIsAllowed() {
        val gate = NexusCapabilityGate {
            NexusRuntimeAvailability(
                installedModels = setOf("llama.cpp + GGUF"),
                installedRuntimes = setOf("llama.cpp")
            )
        }
        assertTrue(gate.isAvailable("models.llama_cpp"))
    }

    @Test
    fun unknownCapabilityIsRejected() {
        val gate = NexusCapabilityGate { NexusRuntimeAvailability() }
        assertFalse(gate.isAvailable("does.not.exist"))
    }

    @Test(expected = IllegalStateException::class)
    fun unknownCapabilityThrowsOnRequire() {
        val gate = NexusCapabilityGate { NexusRuntimeAvailability() }
        gate.requireAvailable("does.not.exist")
    }
}
