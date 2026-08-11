package com.aurax.operator.operator

object AccessibilityGuardrails {
    /** Packages that AURA-X must never automate. Add OEM/enterprise packages here as needed. */
    val BLOCKED_PACKAGES = setOf(
        "com.google.android.apps.authenticator2",
        "com.android.settings.security",
        "com.android.settings.intelligence",
        "com.android.permissioncontroller",
        "com.android.packageinstaller"
    )

    /** Never type into password-like controls. Plain editable fields are allowed only after screen checks. */
    val BLOCKED_NODE_CLASSES = setOf("android.widget.EditText")

    val CONFIRM_ACTIONS = setOf(
        "delete", "purchase", "subscribe", "pay", "transfer", "send",
        "install", "uninstall", "remove", "reset", "factory reset",
        "allow", "deny", "enable", "disable"
    )

    val SENSITIVE_TERMS = setOf(
        "password", "passcode", "otp", "one-time code", "verification code",
        "security code", "credit card", "card number", "cvv", "cvc",
        "bank account", "checkout", "payment", "pay now", "login", "sign in",
        "incognito", "private browsing", "private tab", "secret mode"
    )

    val BLOCKED_YOUTUBE_ACTIONS = setOf("like", "subscribe", "comment", "post comment", "ad", "skip ad")

    fun isSensitiveText(text: String): Boolean = SENSITIVE_TERMS.any { text.contains(it, ignoreCase = true) }

    fun requiresConfirmation(action: String): Boolean =
        CONFIRM_ACTIONS.any { action.contains(it, ignoreCase = true) }

    fun isBlockedYouTubeAction(action: String): Boolean =
        BLOCKED_YOUTUBE_ACTIONS.any { action.contains(it, ignoreCase = true) }
}
