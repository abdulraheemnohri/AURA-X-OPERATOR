package com.aurax.operator.operator

import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.delay

/** High-level, policy-gated AccessibilityService actions.
 * All mutating actions pass through the existing operator/runtime guardrails.
 */
class AccessibilityAutomation(private val operator: AccessibilityOperator) {

    suspend fun click(text: String): Boolean =
        operator.findByText(text)?.let { operator.safeClick(it, "Click: $text") } ?: false

    suspend fun clickContentDescription(description: String): Boolean =
        operator.findByContentDesc(description)?.let { operator.safeClick(it, "Click: $description") } ?: false

    suspend fun typeInto(text: String, value: String): Boolean =
        operator.findByText(text)?.let { operator.safeType(it, value) } ?: false

    suspend fun typeIntoFocused(value: String): Boolean {
        val node = operator.root()?.let(::findFocusedEditable) ?: return false
        return operator.safeType(node, value)
    }

    suspend fun longClick(node: AccessibilityNodeInfo): Boolean {
        OperatorRuntime.ensureNotAborted()
        if (operator.isBlocked(node)) {
            OperatorRuntime.blocked()
            return false
        }
        if (!OperatorRuntime.safetyCountdown("Long click")) return false
        val ok = node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
        OperatorAudit.action(node.packageName?.toString(), "Long click", node.text?.toString(), ok)
        return ok
    }

    suspend fun scrollForward(): Boolean = scroll(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)

    suspend fun scrollBackward(): Boolean = scroll(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)

    suspend fun swipeLikeScroll(container: AccessibilityNodeInfo, forward: Boolean = true): Boolean =
        scroll(if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)

    suspend fun waitForScreenChange(delayMs: Long = 750): ScreenContext? {
        OperatorRuntime.ensureNotAborted()
        delay(delayMs.coerceIn(100, 5000))
        return operator.extract()
    }

    fun visibleText(): String = operator.extract()?.allText.orEmpty()

    fun visibleBounds(): List<Rect> = operator.extract()?.clickableElements?.map { it.bounds } ?: emptyList()

    private suspend fun scroll(action: Int): Boolean {
        OperatorRuntime.ensureNotAborted()
        val root = operator.root() ?: return false
        val target = findScrollable(root) ?: return false
        if (operator.isBlocked(target)) {
            OperatorRuntime.blocked()
            return false
        }
        OperatorRuntime.acting()
        val ok = target.performAction(action)
        OperatorAudit.action(target.packageName?.toString(), "Scroll", action.toString(), ok)
        return ok
    }

    private fun findScrollable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val found = findScrollable(child)
                if (found != null) return found
            }
        }
        return null
    }

    private fun findFocusedEditable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable && node.isFocused && !operator.isBlocked(node)) return node
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val found = findFocusedEditable(child)
                if (found != null) return found
            }
        }
        return null
    }
}
