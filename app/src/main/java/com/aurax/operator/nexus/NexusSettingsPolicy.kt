package com.aurax.operator.nexus

object NexusSettingsPolicy {
    data class Settings(
        val continuousConversation: Boolean = false,
        val wakeWordSensitivity: Float = 0.5f,
        val ragEnabled: Boolean = true,
        val ragTopK: Int = 5,
        val visionModelId: String? = null,
        val pluginExecutionEnabled: Boolean = false,
        val developerMode: Boolean = false,
        val confirmationRequiredForConsequentialActions: Boolean = true,
        val maxAutomationSteps: Int = 20,
        val networkToolsEnabled: Boolean = false
    )
    data class ValidationResult(val settings: Settings, val errors: List<String>) { val isValid: Boolean get() = errors.isEmpty() }
    fun validate(input: Settings): ValidationResult {
        val errors = buildList {
            if (input.wakeWordSensitivity !in 0f..1f) add("wakeWordSensitivity must be between 0 and 1")
            if (input.ragTopK !in 1..50) add("ragTopK must be between 1 and 50")
            if (input.maxAutomationSteps !in 1..100) add("maxAutomationSteps must be between 1 and 100")
            if (input.networkToolsEnabled && !input.confirmationRequiredForConsequentialActions) add("networkToolsEnabled requires consequential-action confirmation")
        }
        return ValidationResult(input, errors)
    }
    fun sanitize(input: Settings) = input.copy(wakeWordSensitivity = input.wakeWordSensitivity.coerceIn(0f, 1f), ragTopK = input.ragTopK.coerceIn(1, 50), maxAutomationSteps = input.maxAutomationSteps.coerceIn(1, 100))
}
