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

    suspend fun safeClick(node: AccessibilityNodeInfo, actionLabel: String = "Click") : Boolean {
        OperatorRuntime.ensureNotAborted()
        val context = extract()
        if (context?.hasPasswordField == true || context.hasSensitiveText || isBlocked(node)) {
            OperatorRuntime.blocked()
            return false
        }
        if (!OperatorRuntime.safetyCountdown(actionLabel)) return false
        if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        var parent = node.parent
        while (parent != null) {
            if (!isBlocked(parent) && parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            parent = parent.parent
        }
        return false
    }

    suspend fun safeType(node: AccessibilityNodeInfo, text: String): Boolean {
        OperatorRuntime.ensureNotAborted()
        if (isBlocked(node) || !node.isEditable) return false
        if ((node.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
            OperatorRuntime.blocked()
            return false
        }
        if (extract()?.hasSensitiveText == true) {
            OperatorRuntime.blocked()
            return false
        }
        OperatorRuntime.acting()
        return node.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
        )
    }

    fun isBlocked(node: AccessibilityNodeInfo): Boolean {
        val pkg = node.packageName?.toString().orEmpty()
        if (pkg in AccessibilityGuardrails.BLOCKED_PACKAGES) return true
        val cls = node.className?.toString().orEmpty()
        return cls == "android.widget.EditText" &&
            (node.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0
    }
}
