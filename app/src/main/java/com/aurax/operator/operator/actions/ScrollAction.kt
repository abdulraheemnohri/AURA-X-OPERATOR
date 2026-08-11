package com.aurax.operator.operator.actions

import android.view.accessibility.AccessibilityNodeInfo
import com.aurax.operator.operator.OperatorAudit
import com.aurax.operator.operator.OperatorRuntime

suspend fun scrollForward(node: AccessibilityNodeInfo): Boolean {
    OperatorRuntime.ensureNotAborted()
    val ok = node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    OperatorAudit.action(node.packageName?.toString(), "Scroll forward", null, ok)
    return ok
}

suspend fun scrollBackward(node: AccessibilityNodeInfo): Boolean {
    OperatorRuntime.ensureNotAborted()
    val ok = node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    OperatorAudit.action(node.packageName?.toString(), "Scroll backward", null, ok)
    return ok
}
