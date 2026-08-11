package com.aurax.operator.tools.chrome

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.aurax.operator.core.common.ToolResult
import com.aurax.operator.operator.AccessibilityOperator
import com.aurax.operator.operator.AccessibilityGuardrails
import com.aurax.operator.operator.OperatorAudit
import com.aurax.operator.operator.OperatorRuntime
import com.aurax.operator.tools.registry.AgentTool
import com.aurax.operator.tools.registry.RiskLevel
import kotlinx.coroutines.delay

class ChromeTool(private val context: Context, private val operator: AccessibilityOperator) : AgentTool {
    override val id = "chrome_automation"
    override val riskLevel = RiskLevel.MEDIUM

    override suspend fun execute(args: Map<String, String>): ToolResult {
        return when (args["action"]?.lowercase()) {
            "first_result" -> clickFirstSafeResult()
            "scroll" -> scroll()
            else -> search(args["query"] ?: return ToolResult.Failure("Missing query"))
        }
    }

    suspend fun openUrl(url: String): ToolResult {
        if (!url.startsWith("https://", true)) return ToolResult.Blocked("Only HTTPS URLs are allowed")
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return ToolResult.Failure("Invalid URL")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.android.chrome")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        OperatorAudit.action("com.android.chrome", "Open HTTPS URL", "[URL]", true)
        return ToolResult.Success("Opened HTTPS URL in Chrome")
    }

    suspend fun search(query: String): ToolResult {
        val opened = openUrl("https://www.google.com/search?q=${Uri.encode(query)}")
        if (opened is ToolResult.Blocked) return opened
        delay(1_500)
        val c = operator.extract() ?: return ToolResult.Failure("Chrome screen unavailable")
        if (c.hasPasswordField || c.hasSensitiveText || c.isPrivateBrowsing) {
            OperatorRuntime.blocked()
            return ToolResult.Blocked("Sensitive or private Chrome screen detected; manual navigation required")
        }
        return ToolResult.Success("Search opened for: $query")
    }

    suspend fun clickFirstSafeResult(): ToolResult {
        val c = operator.extract() ?: return ToolResult.Failure("Chrome screen unavailable")
        if (c.hasPasswordField || c.hasSensitiveText || c.isPrivateBrowsing) return ToolResult.Blocked("Sensitive/private screen")
        val candidate = c.clickableElements.firstOrNull { element ->
            val label = listOfNotNull(element.text, element.contentDesc).joinToString(" ")
            label.isNotBlank() &&
                !AccessibilityGuardrails.isSensitiveText(label) &&
                !label.contains("sponsored", true) &&
                !label.contains("advertisement", true) &&
                !label.contains("sign in", true) &&
                element.className?.contains("TextView", true) == true
        } ?: return ToolResult.Failure("No safe result found")
        val node = operator.findByText(candidate.text ?: candidate.contentDesc ?: "")
            ?: return ToolResult.Failure("Result disappeared")
        return if (operator.safeClick(node, "Open first safe Chrome result")) {
            ToolResult.Success("Opened first safe result")
        } else ToolResult.Failure("Result click failed")
    }

    suspend fun scroll(): ToolResult {
        OperatorRuntime.ensureNotAborted()
        val root = operator.root() ?: return ToolResult.Failure("Chrome screen unavailable")
        val ok = root.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        OperatorAudit.action("com.android.chrome", "Scroll page", null, ok)
        return if (ok) ToolResult.Success("Scrolled Chrome page") else ToolResult.Failure("Chrome page did not scroll")
    }
}
