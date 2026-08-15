package com.aurax.operator.core.capabilities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityGateTest {
    private val ready = Capability("ready", "Ready", "", CapabilityStatus.READY)
    private val permission = Capability("permission", "Permission", "", CapabilityStatus.REQUIRES_PERMISSION)
    private val model = Capability("model", "Model", "", CapabilityStatus.REQUIRES_MODEL)

    @Test fun readyCapabilityIsUsable() {
        val gate = CapabilityGate.evaluate(ready)
        assertEquals(CapabilityStatus.READY, gate.status)
        assertTrue(gate.usable)
    }

    @Test fun permissionGateBlocksWithoutPermission() {
        val gate = CapabilityGate.evaluate(permission, permissionGranted = false)
        assertEquals(CapabilityStatus.REQUIRES_PERMISSION, gate.status)
        assertFalse(gate.usable)
    }

    @Test fun modelGateBlocksWithoutModel() {
        val gate = CapabilityGate.evaluate(model, modelReady = false)
        assertEquals(CapabilityStatus.REQUIRES_MODEL, gate.status)
        assertFalse(gate.usable)
    }

    @Test fun nonBundledAlwaysWins() {
        val gate = CapabilityGate.evaluate(ready, bundled = false)
        assertEquals(CapabilityStatus.NOT_BUNDLED, gate.status)
        assertFalse(gate.usable)
    }
}
