package com.aurax.operator.nexus

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NexusCapabilityGateTest {
    @Test
    fun unavailableModelIsBlocked() {
        val gate = NexusCapabilityGate { NexusRuntimeAvailability() }
        assertFalse(gate.isAvailable("models.llama_cpp"))
        assertFailsWith<IllegalStateException> { gate.requireAvailable("models.llama_cpp") }
    }

    @Test
    fun installedModelIsAllowed() {
        val gate = NexusCapabilityGate {
            NexusRuntimeAvailability(installedModels = setOf("llama.cpp + GGUF"))
        }
        assertTrue(gate.isAvailable("models.llama_cpp"))
    }

    @Test
    fun unknownCapabilityIsRejected() {
        val gate = NexusCapabilityGate { NexusRuntimeAvailability() }
        assertFalse(gate.isAvailable("does.not.exist"))
        assertFailsWith<IllegalStateException> { gate.requireAvailable("does.not.exist") }
    }
}
