package com.aurax.operator.agent.planner

import android.content.Context
import com.aurax.operator.ai.inference.GenerationRequest
import com.aurax.operator.ai.runtime.LlamaCppRuntime
import com.aurax.operator.core.security.AccessibilityGuardrails
import org.json.JSONArray

/**
 * Optional local planner. It is deliberately constrained: the model can only
 * propose the same allow-listed tools exposed by the deterministic planner.
 * Invalid JSON, unknown tools or sensitive arguments are rejected and the
 * caller falls back to the deterministic planner.
 */
class LocalModelPlanner(context: Context) {
    private val runtime = LlamaCppRuntime(context.applicationContext)

    suspend fun plan(input: String): List<PlanStep>? {
        if (!runtime.isReady()) return null
        val prompt = buildPrompt(input)
        val raw = runCatching {
            runtime.generate(
                GenerationRequest(
                    prompt = prompt,
                    maxTokens = 512,
                    temperature = 0.1f,
                    contextTokens = 2048
                )
            )
        }.getOrNull() ?: return null

        return parse(raw)
    }

    private fun buildPrompt(input: String): String = """
        You are AURA-X Operator's local safety planner.
        Return ONLY a JSON array. No markdown and no prose.
        Allowed tools: chrome_automation, youtube_automation, android_open.
        Allowed arguments:
        chrome_automation: query, url, action
        youtube_automation: query, action
        android_open: package
        Never plan passwords, OTP, payment, banking, authentication, private/incognito,
        like/subscribe/comment/ad actions, or any destructive/high-risk action.
        Example: [{"tool":"chrome_automation","description":"Search Chrome for weather","args":{"query":"weather"}}]
        User request: ${input.trim()}
    """.trimIndent()

    private fun parse(raw: String): List<PlanStep>? {
        val start = raw.indexOf('[')
        val end = raw.lastIndexOf(']')
        if (start < 0 || end <= start) return null
        val array = runCatching { JSONArray(raw.substring(start, end + 1)) }.getOrNull() ?: return null
        if (array.length() == 0 || array.length() > 8) return null

        val result = buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: return null
                val tool = item.optString("tool")
                if (tool !in ALLOWED_TOOLS) return null
                val description = item.optString("description").trim()
                if (description.isBlank() || AccessibilityGuardrails.isSensitiveText(description)) return null
                val argsObject = item.optJSONObject("args") ?: return null
                val args = buildMap {
                    val keys = argsObject.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = argsObject.optString(key).trim()
                        if (value.isBlank() || value.length > 500 || AccessibilityGuardrails.isSensitiveText(value)) return null
                        put(key, value)
                    }
                }
                when (tool) {
                    "chrome_automation" -> if (args.keys.none { it in setOf("query", "url", "action") }) return null
                    "youtube_automation" -> if (args.keys.none { it in setOf("query", "action") }) return null
                    "android_open" -> {
                        val pkg = args["package"] ?: return null
                        if (!PACKAGE_PATTERN.matches(pkg) || AccessibilityGuardrails.isBlockedPackage(pkg)) return null
                    }
                }
                add(PlanStep("model-${index + 1}", description, tool, args))
            }
        }
        return result.ifEmpty { null }
    }

    companion object {
        private val ALLOWED_TOOLS = setOf("chrome_automation", "youtube_automation", "android_open")
        private val PACKAGE_PATTERN = Regex("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+")
    }
}
