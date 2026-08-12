package com.aurax.operator.operator

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityActions {
    suspend fun click(service: AuraAccessibilityService, node: AccessibilityNodeInfo): Boolean =
        service.operator.safeClick(node)

    fun longClick(node: AccessibilityNodeInfo): Boolean =
        node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)

    fun focus(node: AccessibilityNodeInfo): Boolean =
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)

    suspend fun type(service: AuraAccessibilityService, node: AccessibilityNodeInfo, text: String): Boolean =
        service.operator.safeType(node, text)

    fun clearText(node: AccessibilityNodeInfo): Boolean = node.performAction(
        AccessibilityNodeInfo.ACTION_SET_TEXT,
        Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
        }
    )

    fun scrollForward(root: AccessibilityNodeInfo): Boolean =
        root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)

    fun scrollBackward(root: AccessibilityNodeInfo): Boolean =
        root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)

    fun back(service: AuraAccessibilityService): Boolean =
        service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)

    fun home(service: AuraAccessibilityService): Boolean =
        service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
}
