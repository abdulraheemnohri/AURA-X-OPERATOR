package com.aurax.operator.core.security

import com.aurax.operator.core.app.AppState
import com.aurax.operator.core.app.OperatorPhase

object OperatorSafety {
    data class Decision(val allowed: Boolean, val requiresConfirmation: Boolean, val reason: String)

    private val confirmationTerms = setOf("delete", "purchase", "subscribe", "pay", "transfer", "send", "install", "uninstall", "toggle")

    fun evaluate(packageName: String?, visibleText: String, actionDescription: String): Decision {
        if (SafetyController.isAbortRequested()) return Decision(false, false, "Abort requested")
        if (!SafetyController.requireSafeScreen(packageName, visibleText)) {
            AppState.setPhase(OperatorPhase.BLOCKED, "Sensitive or blocked screen")
            return Decision(false, false, "Sensitive or blocked screen")
        }
        val confirmation = confirmationTerms.any { actionDescription.contains(it, ignoreCase = true) }
        return Decision(true, confirmation, if (confirmation) "User confirmation required" else "Safe action")
    }

    fun beginConfirmation(seconds: Int = 3) {
        AppState.setPhase(OperatorPhase.CONFIRMING, "Confirm action")
        AppState.setCountdown(seconds)
    }
}
