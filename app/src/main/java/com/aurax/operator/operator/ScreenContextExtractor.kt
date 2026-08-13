package com.aurax.operator.operator

import android.graphics.Rect
import android.text.InputType
import android.view.accessibility.AccessibilityNodeInfo

data class ScreenContext(
    val packageName: String,
    val windowTitle: String,
    val allText: String,
    val clickableElements: List<UiElement>,
    val hasPasswordField: Boolean,
    val hasSensitiveText: Boolean,
    val isPrivateBrowsing: Boolean
)

object ScreenContextExtractor {
    fun extract(root: AccessibilityNodeInfo): ScreenContext {
        val texts = mutableListOf<String>()
        val elements = mutableListOf<UiElement>()
        var hasPasswordField = false

        fun walk(node: AccessibilityNodeInfo) {
            node.text?.toString()?.takeIf { it.isNotBlank() }?.let(texts::add)
            node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let(texts::add)

            if ((node.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0 ||
                (node.inputType and InputType.TYPE_NUMBER_VARIATION_PASSWORD) != 0
            ) {
                hasPasswordField = true
            }

            if (node.isClickable || node.isEditable) {
                val bounds = Rect().also { node.getBoundsInScreen(it) }
                elements += UiElement(
                    text = node.text?.toString(),
                    contentDesc = node.contentDescription?.toString(),
                    resourceId = node.viewIdResourceName,
                    className = node.className?.toString(),
                    bounds = bounds,
                    isEditable = node.isEditable,
                    inputType = node.inputType,
                    isClickable = node.isClickable
                )
            }

            for (index in 0 until node.childCount) {
                node.getChild(index)?.let(::walk)
            }
        }

        walk(root)

        val joinedText = texts.joinToString(" | ")
        val isPrivateBrowsing = joinedText.contains("incognito", ignoreCase = true) ||
            joinedText.contains("private browsing", ignoreCase = true) ||
            joinedText.contains("private tab", ignoreCase = true)

        return ScreenContext(
            packageName = root.packageName?.toString().orEmpty(),
            windowTitle = texts.firstOrNull().orEmpty(),
            allText = joinedText,
            clickableElements = elements,
            hasPasswordField = hasPasswordField,
            hasSensitiveText = AccessibilityGuardrails.isSensitiveText(joinedText),
            isPrivateBrowsing = isPrivateBrowsing
        )
    }
}
