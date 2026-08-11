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

    fun classify(label: String?, packageName: String?, blocked: Boolean): ActionRisk {
        if (blocked) return ActionRisk.BLOCKED
        val value = label.orEmpty().lowercase()
        if (destructiveWords.any(value::contains)) return ActionRisk.HIGH
        if (packageName.orEmpty() in AccessibilityGuardrails.BLOCKED_PACKAGES) return ActionRisk.BLOCKED
        return ActionRisk.LOW
    }

    fun canExecute(policy: AutomationPolicy, risk: ActionRisk): Boolean = when (policy) {
        AutomationPolicy.OBSERVE_ONLY, AutomationPolicy.SUGGEST_ONLY -> false
        AutomationPolicy.CONFIRM_ACTIONS, AutomationPolicy.FULL_AUTO_LOW_RISK -> risk == ActionRisk.LOW
    }
}
