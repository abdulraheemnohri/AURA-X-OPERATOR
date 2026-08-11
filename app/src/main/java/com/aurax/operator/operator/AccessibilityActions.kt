package com.aurax.operator.operator

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityActions {
    fun click(service: AuraAccessibilityService, node: AccessibilityNodeInfo) = service.operator.safeClick(node)
    fun longClick(node: AccessibilityNodeInfo) = node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    fun focus(node: AccessibilityNodeInfo) = node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
    fun type(service: AuraAccessibilityService, node: AccessibilityNodeInfo, text: String) = service.operator.safeTypeText(node, text)
    fun clearText(node: AccessibilityNodeInfo) = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
        putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
    })
    fun scrollForward(root: AccessibilityNodeInfo) = root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    fun scrollBackward(root: AccessibilityNodeInfo) = root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    fun back(service: AuraAccessibilityService) = service.performGlobalAction(1)
    fun home(service: AuraAccessibilityService) = service.performGlobalAction(2)
}
