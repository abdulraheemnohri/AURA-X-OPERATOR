package com.aurax.operator.agent.verification

import com.aurax.operator.agent.planner.PlanStep
import com.aurax.operator.operator.AccessibilityOperator
import com.aurax.operator.operator.ScreenContext

/**
 * Evidence-based post-action verification. A step is only strongly verified when
 * the resulting accessibility state contains an explicit signal for the action.
 */
class ActionVerifier {
    data class Result(
        val verified: Boolean,
        val evidence: String,
        val confidence: Float
    )

    fun verify(step: PlanStep, operator: AccessibilityOperator): Result {
        val context = operator.extract()
            ?: return Result(false, "Accessibility state is unavailable after action", 0f)

        if (context.hasPasswordField || context.hasSensitiveText || context.isPrivateBrowsing) {
            return Result(false, "Sensitive/private screen detected during verification", 0f)
        }

        val expectedText = step.args["verifyText"]?.trim().orEmpty()
        if (expectedText.isNotEmpty() && containsText(context, expectedText)) {
            return Result(true, "Expected text is visible: ${expectedText.take(100)}", 1f)
        }

        val expectedPackage = step.args["verifyPackage"]?.trim()
            ?: step.args["package"]?.trim()
            ?: when (step.tool) {
                "android_settings" -> "com.android.settings"
                "chrome_automation" -> "com.android.chrome"
                "youtube_automation" -> "com.google.android.youtube"
                else -> null
            }

        if (!expectedPackage.isNullOrBlank() && context.packageName == expectedPackage) {
            return Result(true, "Expected package is active: $expectedPackage", 0.9f)
        }

        if (step.tool == "open_url" && context.packageName.isNotBlank()) {
            return Result(true, "Browser window is active after URL action", 0.65f)
        }

        return Result(
            false,
            "No explicit verification signal matched the expected result",
            0.1f
        )
    }

    private fun containsText(context: ScreenContext, expected: String): Boolean =
        context.allText.contains(expected, ignoreCase = true) ||
            context.windowTitle.contains(expected, ignoreCase = true)
}
