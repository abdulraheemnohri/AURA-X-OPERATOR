package com.aurax.operator.operator

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityOperator(private val service: AuraAccessibilityService) {
    fun root(): AccessibilityNodeInfo? = service.rootInActiveWindow

    fun findByText(text: String): AccessibilityNodeInfo? =
        root()?.findAccessibilityNodeInfosByText(text)?.firstOrNull { !isBlocked(it) && it.isVisibleToUser }

    fun findByClass(className: String): AccessibilityNodeInfo? {
        val r = root() ?: return null
        var found: AccessibilityNodeInfo? = null
        fun walk(n: AccessibilityNodeInfo) {
            if (found == null && n.className?.toString() == className && !isBlocked(n) && n.isVisibleToUser) found = n
            for (i in 0 until n.childCount) n.getChild(i)?.let(::walk)
        }
        walk(r)
        return found
    }

    fun findByResourceId(resourceId: String): AccessibilityNodeInfo? {
        val r = root() ?: return null
        var found: AccessibilityNodeInfo? = null
        fun walk(n: AccessibilityNodeInfo) {
            if (found == null && n.viewIdResourceName == resourceId && !isBlocked(n) && n.isVisibleToUser) found = n
            for (i in 0 until n.childCount) n.getChild(i)?.let(::walk)
        }
        walk(r)
        return found
    }

    fun findByContentDesc(text: String): AccessibilityNodeInfo? {
        val r = root() ?: return null
        var found: AccessibilityNodeInfo? = null
        fun walk(n: AccessibilityNodeInfo) {
            if (found == null && n.contentDescription?.toString()?.contains(text, true) == true && !isBlocked(n) && n.isVisibleToUser) found = n
            for (i in 0 until n.childCount) n.getChild(i)?.let(::walk)
        }
        walk(r)
        return found
    }

    fun extract(): ScreenContext? = root()?.let(ScreenContextExtractor::extract)

    suspend fun safeClick(node: AccessibilityNodeInfo, actionLabel: String = "Click"): Boolean {
        OperatorRuntime.ensureNotAborted()
        if (!isActionable(node)) {
            block("BLOCKED_CLICK", "Node is not visible, enabled, actionable, or is sensitive", node, actionLabel)
            return false
        }

        val initialContext = extract()
        if (initialContext?.let { it.hasPasswordField || it.hasSensitiveText || it.isPrivateBrowsing } == true) {
            block("BLOCKED_CLICK", "Sensitive/private screen", node, actionLabel)
            return false
        }

        if (!OperatorRuntime.safetyCountdown(actionLabel)) {
            OperatorAudit.action(node.packageName?.toString(), actionLabel, safeLabel(node), false)
            return false
        }

        // Countdown creates a deliberate race boundary. Revalidate the live screen before acting.
        OperatorRuntime.ensureNotAborted()
        val currentRoot = root()
        if (currentRoot == null || AccessibilityGuardrails.isBlockedPackage(currentRoot.packageName?.toString())) {
            block("BLOCKED_CLICK", "Active window changed to a blocked package", node, actionLabel)
            return false
        }
        val currentContext = extract()
        if (currentContext?.let { it.hasPasswordField || it.hasSensitiveText || it.isPrivateBrowsing } == true) {
            block("BLOCKED_CLICK", "Screen became sensitive/private during countdown", node, actionLabel)
            return false
        }

        if (!node.refresh() || !isActionable(node)) {
            block("BLOCKED_CLICK", "Target became stale before execution", node, actionLabel)
            return false
        }

        val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK) || clickClickableParent(node)
        OperatorAudit.action(node.packageName?.toString(), actionLabel, safeLabel(node), clicked)
        return clicked
    }

    suspend fun safeType(node: AccessibilityNodeInfo, text: String): Boolean {
        OperatorRuntime.ensureNotAborted()
        if (!node.isEditable || !node.isVisibleToUser || !node.isEnabled || AccessibilityGuardrails.isSensitiveNode(node)) {
            block("BLOCKED_TYPE", "Non-editable, disabled, password, sensitive, or blocked control", node, "Type")
            return false
        }
        val context = extract()
        if (context?.let { it.hasSensitiveText || it.isPrivateBrowsing || it.hasPasswordField } == true) {
            block("BLOCKED_TYPE", "Sensitive/private screen", node, "Type")
            return false
        }

        if (!node.refresh() || !node.isEditable || !node.isVisibleToUser || !node.isEnabled || AccessibilityGuardrails.isSensitiveNode(node)) {
            block("BLOCKED_TYPE", "Target became stale or sensitive", node, "Type")
            return false
        }

        OperatorRuntime.acting()
        val ok = node.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
        )
        // Never persist the actual typed value.
        OperatorAudit.action(node.packageName?.toString(), "Type text", "[REDACTED]", ok)
        return ok
    }

    private fun clickClickableParent(node: AccessibilityNodeInfo): Boolean {
        var parent = node.parent
        while (parent != null) {
            if (!isBlocked(parent) && parent.isVisibleToUser && parent.isEnabled && parent.isClickable) {
                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            parent = parent.parent
        }
        return false
    }

    private fun isActionable(node: AccessibilityNodeInfo): Boolean =
        !isBlocked(node) && node.isVisibleToUser && node.isEnabled && (node.isClickable || node.isFocusable)

    private fun isBlocked(node: AccessibilityNodeInfo): Boolean =
        AccessibilityGuardrails.isSensitiveNode(node)

    private fun safeLabel(node: AccessibilityNodeInfo): String =
        node.contentDescription?.toString()?.take(120)
            ?: node.text?.toString()?.take(120)
            ?: node.viewIdResourceName?.take(120)
            ?: "[unlabeled]"

    private fun block(code: String, reason: String, node: AccessibilityNodeInfo, action: String) {
        OperatorRuntime.blocked()
        OperatorAudit.safety(code, reason, node.packageName?.toString(), action)
    }
}
