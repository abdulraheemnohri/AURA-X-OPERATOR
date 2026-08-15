package com.aurax.operator.nexus

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NexusSettingsPolicyTest {
    @Test fun invalidRangesAreRejected() {
        val r = NexusSettingsPolicy.validate(NexusSettingsPolicy.Settings(wakeWordSensitivity = 2f, ragTopK = 0, maxAutomationSteps = 0))
        assertFalse(r.isValid)
        assertTrue(r.errors.size == 3)
    }
    @Test fun unsafeNetworkConfigurationIsRejected() {
        val r = NexusSettingsPolicy.validate(NexusSettingsPolicy.Settings(networkToolsEnabled = true, confirmationRequiredForConsequentialActions = false))
        assertFalse(r.isValid)
    }
    @Test fun sanitizeClampsValues() {
        val s = NexusSettingsPolicy.sanitize(NexusSettingsPolicy.Settings(wakeWordSensitivity = -1f, ragTopK = 999, maxAutomationSteps = 999))
        assertTrue(s.wakeWordSensitivity == 0f && s.ragTopK == 50 && s.maxAutomationSteps == 100)
    }
}
