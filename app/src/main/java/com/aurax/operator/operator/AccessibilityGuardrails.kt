package com.aurax.operator.operator

import android.text.InputType
import android.view.accessibility.AccessibilityNodeInfo

/** Central, deterministic safety policy used before every operator action. */
object AccessibilityGuardrails {
    /** High-risk/security packages that AURA-X must never automate. */
    val BLOCKED_PACKAGES = setOf(
        "com.google.android.apps.authenticator2",
        "com.google.android.apps.walletnfcrel",
        "com.samsung.android.spay",
        "com.samsung.android.samsungpay.gear",
        "com.android.permissioncontroller",
        "com.android.packageinstaller"
    )

    /** Exact security-sensitive package prefixes used by OEM security components. */
    val BLOCKED_PACKAGE_PREFIXES = setOf(
        "com.android.settings.security",
        "com.google.android.settings.intelligence.security"
    )

    val CONFIRM_ACTIONS = setOf(
        "delete", "purchase", "buy", "subscribe", "pay", "transfer", "send",
        "install", "uninstall", "remove", "reset", "factory reset",
        "allow", "deny", "enable", "disable", "grant", "revoke"
    )

    val SENSITIVE_TERMS = setOf(
        "password", "passcode", "pin", "otp", "one-time code", "verification code",
        "security code", "recovery code", "credit card", "debit card", "card number",
        "cvv", "cvc", "iban", "bank account", "routing number", "checkout", "payment",
        "pay now", "login", "sign in", "sign-in", "authenticate", "authentication",
        "incognito", "private browsing", "private tab", "secret mode"
    )

    val BLOCKED_YOUTUBE_ACTIONS = setOf(
        "like", "subscribe", "comment", "post comment", "send comment", "ad", "skip ad"
    )

    fun isBlockedPackage(packageName: String?): Boolean {
        val pkg = packageName.orEmpty()
        return pkg in BLOCKED_PACKAGES || BLOCKED_PACKAGE_PREFIXES.any(pkg::startsWith)
    }

    fun isPasswordLikeInputType(inputType: Int): Boolean {
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
    }

    fun isSensitiveNode(node: AccessibilityNodeInfo): Boolean {
        if (isBlockedPackage(node.packageName?.toString())) return true
        if (isPasswordLikeInputType(node.inputType)) return true
        val label = buildString {
            node.text?.let(::append)
            node.contentDescription?.let {
                if (isNotEmpty()) append(' ')
                append(it)
            }
            node.hintText?.let {
                if (isNotEmpty()) append(' ')
                append(it)
            }
        }
        return isSensitiveText(label)
    }

    fun isSensitiveText(text: String): Boolean =
        SENSITIVE_TERMS.any { text.contains(it, ignoreCase = true) }

    fun requiresConfirmation(action: String): Boolean =
        CONFIRM_ACTIONS.any { action.contains(it, ignoreCase = true) }

    fun isBlockedYouTubeAction(action: String): Boolean =
        BLOCKED_YOUTUBE_ACTIONS.any { action.contains(it, ignoreCase = true) }
}
