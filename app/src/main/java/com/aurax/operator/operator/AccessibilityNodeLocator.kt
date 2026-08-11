package com.aurax.operator.operator

import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityNodeLocator {
    fun byText(root: AccessibilityNodeInfo, text: String, exact: Boolean = false): AccessibilityNodeInfo? =
        root.findAccessibilityNodeInfosByText(text).firstOrNull { node ->
            val value = node.text?.toString().orEmpty()
            if (exact) value.equals(text, true) else value.contains(text, true)
        }

    fun byContentDescription(root: AccessibilityNodeInfo, description: String): AccessibilityNodeInfo? {
        var result: AccessibilityNodeInfo? = null
        walk(root) { node ->
            if (result == null && node.contentDescription?.toString()?.contains(description, true) == true) result = node
        }
        return result
    }

    fun byResourceId(root: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? =
        root.findAccessibilityNodeInfosByViewId(resourceId).firstOrNull()

    fun firstEditable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var result: AccessibilityNodeInfo? = null
        walk(root) { if (result == null && it.isEditable) result = it }
        return result
    }

    fun firstClickable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var result: AccessibilityNodeInfo? = null
        walk(root) { if (result == null && it.isClickable) result = it }
        return result
    }

    fun walk(root: AccessibilityNodeInfo, visitor: (AccessibilityNodeInfo) -> Unit) {
        visitor(root)
        for (i in 0 until root.childCount) root.getChild(i)?.let { child ->
            walk(child, visitor)
            child.recycle()
        }
    }
}
