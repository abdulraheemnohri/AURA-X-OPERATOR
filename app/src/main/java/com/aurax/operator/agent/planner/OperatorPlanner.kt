package com.aurax.operator.agent.planner

data class PlanStep(
    val id: String,
    val description: String,
    val tool: String,
    val args: Map<String, String> = emptyMap()
)

class OperatorPlanner {
    fun plan(input: String): List<PlanStep> {
        val q = input.trim()
        if (q.isBlank()) return emptyList()

        // Match YouTube-specific commands before the generic search command.
        // Otherwise "search youtube cats" is incorrectly planned as a Chrome search.
        val youtube = Regex("(?:search youtube|youtube search)\\s+(?:for\\s+)?(.+)", RegexOption.IGNORE_CASE).find(q)
        if (youtube != null) {
            return listOf(
                PlanStep(
                    "1",
                    "Search YouTube for ${youtube.groupValues[1]}",
                    "youtube_automation",
                    mapOf("query" to youtube.groupValues[1])
                )
            )
        }

        val search = Regex("(?:open chrome and )?search\\s+(?:for\\s+)?(.+)", RegexOption.IGNORE_CASE).find(q)
        if (search != null) {
            return listOf(
                PlanStep(
                    "1",
                    "Open Chrome and search for ${search.groupValues[1]}",
                    "chrome_automation",
                    mapOf("query" to search.groupValues[1])
                )
            )
        }

        val openPackage = Regex("(?:open|launch) package ([A-Za-z0-9_.]+)", RegexOption.IGNORE_CASE).find(q)
        if (openPackage != null) {
            return listOf(
                PlanStep(
                    "1",
                    "Open ${openPackage.groupValues[1]}",
                    "android_open",
                    mapOf("package" to openPackage.groupValues[1])
                )
            )
        }

        if (q.contains("play first", true) && q.contains("youtube", true)) {
            return listOf(
                PlanStep(
                    "1",
                    "Play the first safe YouTube result",
                    "youtube_automation",
                    mapOf("action" to "play")
                )
            )
        }

        return listOf(PlanStep("1", "No supported safe action was recognized", "none"))
    }
}
