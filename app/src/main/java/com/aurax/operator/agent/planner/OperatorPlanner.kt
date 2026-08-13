package com.aurax.operator.agent.planner

/** Deterministic fallback planner. It understands common English, Urdu and Roman Urdu phrases. */
class OperatorPlanner {
    fun plan(input: String, memoryContext: String = ""): List<PlanStep> {
        val q = input.trim()
        if (q.isBlank()) return emptyList()

        val youtube = Regex(
            "(?:search youtube|youtube search|yt search|youtube pe|youtube mein|youtube par)\\s+(?:for\\s+)?(.+)",
            RegexOption.IGNORE_CASE
        ).find(q)
        if (youtube != null) {
            return step("Search YouTube for ${youtube.groupValues[1]}", "youtube_automation", "query" to youtube.groupValues[1])
        }

        val chrome = Regex(
            "(?:open chrome and )?(?:search|find)\\s+(?:for\\s+)?(.+)",
            RegexOption.IGNORE_CASE
        ).find(q)
        if (chrome != null) {
            return step("Open Chrome and search for ${chrome.groupValues[1]}", "chrome_automation", "query" to chrome.groupValues[1])
        }

        val romanChrome = Regex(
            "(?:chrome|browser)\\s+(?:mein|pe|par)\\s+(.+?)\\s+(?:search|dhundo|dhoondo)",
            RegexOption.IGNORE_CASE
        ).find(q)
        if (romanChrome != null) {
            return step("Search Chrome for ${romanChrome.groupValues[1]}", "chrome_automation", "query" to romanChrome.groupValues[1])
        }

        val open = Regex(
            "(?:open|launch|start|kholo|khol do|chalao)\\s+(?:app\\s+)?(.+)",
            RegexOption.IGNORE_CASE
        ).find(q) ?: Regex(
            "(.+)\\s+(?:kholo|open karo|khol do)",
            RegexOption.IGNORE_CASE
        ).find(q)
        if (open != null) {
            val target = resolveAppName(open.groupValues[1].trim())
            return step("Open $target", "android_open", "package" to target)
        }

        val openPackage = Regex(
            "(?:open|launch) package ([A-Za-z0-9_.]+)",
            RegexOption.IGNORE_CASE
        ).find(q)
        if (openPackage != null) {
            return step("Open ${openPackage.groupValues[1]}", "android_open", "package" to openPackage.groupValues[1])
        }

        if (q.contains("play first", true) && q.contains("youtube", true)) {
            return step("Play the first safe YouTube result", "youtube_automation", "action" to "play")
        }

        return listOf(
            PlanStep(
                "1",
                if (memoryContext.isBlank()) "No supported safe action was recognized" else "No supported safe action was recognized; memory context was available to the local planner",
                "none"
            )
        )
    }

    private fun step(description: String, tool: String, vararg args: Pair<String, String>): List<PlanStep> =
        listOf(PlanStep("1", description, tool, mapOf(*args)))

    private fun resolveAppName(name: String): String = when (name.lowercase().trim()) {
        "chrome", "google chrome", "browser" -> "com.android.chrome"
        "youtube", "yt" -> "com.google.android.youtube"
        "settings", "android settings" -> "com.android.settings"
        "calculator", "calc" -> "com.google.android.calculator"
        "maps", "google maps" -> "com.google.android.apps.maps"
        "dialer", "phone" -> "com.google.android.dialer"
        else -> name
    }
}
