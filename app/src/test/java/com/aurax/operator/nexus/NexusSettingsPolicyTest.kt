package com.aurax.operator.nexus

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NexusSettingsPolicyTest {
    @Test
    fun invalidRangesAreRejected() {
        val result = NexusSettingsPolicy.validate(
            NexusSettingsPolicy.Settings(
                wakeWordSensitivity = 2f,
                ragTopK = 0,
                maxAutomationSteps = 0
            )
        )
        assertFalse(result.isValid)
        assertTrue(result.errors.size == 3)
    }

    @Test
    fun unsafeNetworkConfigurationIsRejected() {
        val result = NexusSettingsPolicy.validate(
            NexusSettingsPolicy.Settings(
                networkToolsEnabled = true,
                confirmationRequiredForConsequentialActions = false
            )
        )
        assertFalse(result.isValid)
    }

    @Test
    fun sanitizeClampsValuesForPersistence() {
        val sanitized = NexusSettingsPolicy.sanitize(
            NexusSettingsPolicy.Settings(
                wakeWordSensitivity = -1f,
                ragTopK = 999,
                maxAutomationSteps = 999
            )
        )
        assertTrue(sanitized.wakeWordSensitivity == 0f)
        assertTrue(sanitized.ragTopK == 50)
        assertTrue(sanitized.maxAutomationSteps == 100)
    }
}
