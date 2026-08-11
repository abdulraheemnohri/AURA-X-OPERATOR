package com.aurax.operator.operator

import android.os.Bundle
import android.text.InputType
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityOperator(private val service: AuraAccessibilityService) {
    fun root(): AccessibilityNodeInfo? = service.rootInActiveWindow

    fun findByText(text: String): AccessibilityNodeInfo? =
        root()?.findAccessibilityNodeInfosByText(text)?.firstOrNull { !isBlocked(it) }

    fun findByClass(className: String): AccessibilityNodeInfo? {
        val r = root() ?: return null
        var found: AccessibilityNodeInfo? = null
        fun walk(n: AccessibilityNodeInfo) {
            if (found == null && n.className?.toString() == className && !isBlocked(n)) found = n
            for (i in 0 until n.childCount) n.getChild(i)?.let(::walk)
        }
        walk(r)
        return found
    }

    fun findByResourceId(resourceId: String): AccessibilityNodeInfo? {
        val r = root() ?: return null
        var found: AccessibilityNodeInfo? = null
        fun walk(n: AccessibilityNodeInfo) {
            if (found == null && n.viewIdResourceName == resourceId && !isBlocked(n)) found = n
            for (i in 0 until n.childCount) n.getChild(i)?.let(::walk)
        }
        walk(r)
        return found
    }

    fun findByContentDesc(text: String): AccessibilityNodeInfo? {
        val r = root() ?: return null
        var found: AccessibilityNodeInfo? = null
        fun walk(n: AccessibilityNodeInfo) {
            if (found == null && n.contentDescription?.toString()?.contains(text, true) == true && !isBlocked(n)) found = n
            for (i in 0 until n.childCount) n.getChild(i)?.let(::walk)
        }
        walk(r)
        return found
    }

    fun extract(): ScreenContext? = root()?.let(ScreenContextExtractor::extract)

    suspend fun safeClick(node: AccessibilityNodeInfo, actionLabel: String = "Click"): Boolean {
        OperatorRuntime.ensureNotAborted()
        val context = extract()
        if (context?.let { it.hasPasswordField || it.hasSensitiveText || it.isPrivateBrowsing } == true || isBlocked(node)) {
            OperatorRuntime.blocked()
            OperatorAudit.safety("BLOCKED_CLICK", "Sensitive/private screen or blocked node", node.packageName?.toString(), actionLabel)
            return false
        }
        if (!OperatorRuntime.safetyCountdown(actionLabel)) {
            OperatorAudit.action(node.packageName?.toString(), actionLabel, node.text?.toString(), false)
            return false
        }
        val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK) || run {
            var parent = node.parent
            var success = false
            while (parent != null && !success) {
                if (!isBlocked(parent) && parent.isClickable) success = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                parent = parent.parent
            }
            success
        }
        OperatorAudit.action(node.packageName?.toString(), actionLabel, node.text?.toString(), clicked)
        return clicked
    }

    suspend fun safeType(node: AccessibilityNodeInfo, text: String): Boolean {
        OperatorRuntime.ensureNotAborted()
        if (isBlocked(node) || !node.isEditable) {
            OperatorAudit.safety("BLOCKED_TYPE", "Non-editable or password control", node.packageName?.toString(), "Type")
            return false
        }
        if ((node.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
            OperatorRuntime.blocked()
            OperatorAudit.safety("BLOCKED_TYPE", "Password field", node.packageName?.toString(), "Type")
            return false
        }
        val context = extract()
        if (context?.let { it.hasSensitiveText || it.isPrivateBrowsing } == true) {
            OperatorRuntime.blocked()
            OperatorAudit.safety("BLOCKED_TYPE", "Sensitive/private screen", node.packageName?.toString(), "Type")
            return false
        }
        OperatorRuntime.acting()
        val ok = node.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
        )
        // Deliberately never log the typed value.
        OperatorAudit.action(node.packageName?.toString(), "Type text", "[REDACTED]", ok)
        return ok
    }

    fun isBlocked(node: AccessibilityNodeInfo): Boolean {
        val pkg = node.packageName?.toString().orEmpty()
        if (pkg in AccessibilityGuardrails.BLOCKED_PACKAGES) return true
        val cls = node.className?.toString().orEmpty()
        return cls == "android.widget.EditText" &&
            (node.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0
    }
}
