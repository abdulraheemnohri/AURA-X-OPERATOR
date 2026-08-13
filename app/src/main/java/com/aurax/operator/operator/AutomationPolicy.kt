package com.aurax.operator.operator

/** User-selectable automation modes. Safe defaults are intentionally conservative. */
enum class AutomationPolicy { OBSERVE_ONLY, SUGGEST_ONLY, CONFIRM_ACTIONS, FULL_AUTO_LOW_RISK }
enum class ActionRisk { LOW, MEDIUM, HIGH, BLOCKED }

data class OperatorSettings(
    val policy: AutomationPolicy = AutomationPolicy.CONFIRM_ACTIONS,
    val confirmationSeconds: Int = 3,
    val maxActionsPerTask: Int = 30,
    val maxTaskSeconds: Int = 120,
    val showOverlay: Boolean = true,
    val allowChrome: Boolean = true,
    val allowYouTube: Boolean = true,
    val allowSystemNavigation: Boolean = false,
    val logActions: Boolean = true
)

object AutomationPolicyEngine {
    private val destructiveWords = setOf("delete", "purchase", "buy", "subscribe", "pay", "transfer", "send", "install", "uninstall", "reset")
    private val mediumWords = setOf("click", "type", "scroll", "open", "navigate", "search", "play", "pause", "volume", "back", "forward")

    fun classify(label: String?, packageName: String?, blocked: Boolean): ActionRisk {
        // A blocked target always wins over a textual risk classification.
        if (blocked || packageName.orEmpty() in AccessibilityGuardrails.BLOCKED_PACKAGES) return ActionRisk.BLOCKED
        val value = label.orEmpty().lowercase()
        if (destructiveWords.any(value::contains)) return ActionRisk.HIGH
        if (mediumWords.any(value::contains)) return ActionRisk.MEDIUM
        return ActionRisk.LOW
    }

    fun canExecute(policy: AutomationPolicy, risk: ActionRisk): Boolean = when (policy) {
        AutomationPolicy.OBSERVE_ONLY, AutomationPolicy.SUGGEST_ONLY -> false
        AutomationPolicy.CONFIRM_ACTIONS -> risk == ActionRisk.LOW || risk == ActionRisk.MEDIUM
        AutomationPolicy.FULL_AUTO_LOW_RISK -> risk == ActionRisk.LOW
    }

    fun requiresConfirmation(policy: AutomationPolicy, risk: ActionRisk): Boolean = when (policy) {
        AutomationPolicy.OBSERVE_ONLY, AutomationPolicy.SUGGEST_ONLY -> false
        AutomationPolicy.CONFIRM_ACTIONS -> risk == ActionRisk.MEDIUM
        AutomationPolicy.FULL_AUTO_LOW_RISK -> false
    }
}
