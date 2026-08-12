package com.aurax.operator.core.security

import com.aurax.operator.core.app.AppState
import com.aurax.operator.core.app.OperatorPhase

object SafetyController {
    private val sensitiveTerms = setOf("password", "passcode", "otp", "one-time code", "verification code", "credit card", "cvv", "bank account", "checkout", "payment", "pay now", "transfer", "login", "sign in")
    private val blockedPackages = setOf("com.google.android.apps.authenticator2", "com.android.settings.security")

    fun requestAbort(reason: String = "User aborted automation") {
        AppState.update { it.copy(abortRequested = true, phase = OperatorPhase.ABORTED, message = reason) }
    }

    fun isAbortRequested(): Boolean = AppState.operator.value.abortRequested

    fun clearAbort() = AppState.clearAbort()

    fun isSensitiveText(value: CharSequence?): Boolean {
        val text = value?.toString()?.trim()?.lowercase() ?: return false
        return sensitiveTerms.any { text.contains(it) }
    }

    fun isBlockedPackage(packageName: String?): Boolean = packageName != null && packageName in blockedPackages

    fun requireSafeScreen(packageName: String?, visibleText: String): Boolean {
        return !isBlockedPackage(packageName) && !isSensitiveText(visibleText)
    }

    fun checkBeforeAction(packageName: String?, visibleText: String): Boolean {
        if (isAbortRequested()) return false
        return requireSafeScreen(packageName, visibleText)
    }
}
