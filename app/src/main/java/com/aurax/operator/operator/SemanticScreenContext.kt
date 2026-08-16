package com.aurax.operator.operator

/**
 * Compact, non-sensitive semantic representation of the currently observed UI.
 * It is derived only from the accessibility tree; it does not invent visual facts.
 */
data class SemanticScreenContext(
    val packageName: String,
    val windowTitle: String,
    val visibleText: List<String>,
    val actionableElements: List<SemanticElement>,
    val flags: Set<String>
) {
    fun asPlannerContext(maxChars: Int = 3000): String {
        if (flags.any { it == "PASSWORD_FIELD" || it == "SENSITIVE_TEXT" || it == "PRIVATE_BROWSING" }) {
            return "SCREEN_CONTEXT_BLOCKED: sensitive or private state detected"
        }

        val builder = StringBuilder()
        builder.append("package=").append(packageName).append('\n')
        if (windowTitle.isNotBlank()) builder.append("title=").append(windowTitle.take(200)).append('\n')
        if (flags.isNotEmpty()) builder.append("flags=").append(flags.joinToString(",")).append('\n')
        if (visibleText.isNotEmpty()) {
            builder.append("text=").append(visibleText.take(30).joinToString(" | ").take(1200)).append('\n')
        }
        if (actionableElements.isNotEmpty()) {
            builder.append("actions=\n")
            actionableElements.take(30).forEachIndexed { index, element ->
                builder.append(index + 1).append('.').append(' ')
                    .append(element.role).append(' ')
                    .append(element.label.take(160))
                if (element.resourceId.isNotBlank()) {
                    builder.append(" id=").append(element.resourceId.take(120))
                }
                builder.append('\n')
            }
        }
        return builder.toString().take(maxChars)
    }
}

data class SemanticElement(
    val role: String,
    val label: String,
    val resourceId: String
)

object SemanticScreenContextBuilder {
    fun build(context: ScreenContext): SemanticScreenContext {
        val flags = buildSet {
            if (context.hasPasswordField) add("PASSWORD_FIELD")
            if (context.hasSensitiveText) add("SENSITIVE_TEXT")
            if (context.isPrivateBrowsing) add("PRIVATE_BROWSING")
            if (context.clickableElements.any { it.isEditable }) add("EDITABLE")
            if (context.clickableElements.any { it.isClickable }) add("CLICKABLE")
        }

        val visibleText = context.allText
            .split('|')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(40)

        val actionable = context.clickableElements.mapNotNull { element ->
            val label = listOfNotNull(element.text, element.contentDesc)
                .firstOrNull { it.isNotBlank() }
                ?: return@mapNotNull null
            val role = when {
                element.isEditable -> "EDITABLE"
                element.isClickable -> "CLICKABLE"
                else -> element.className?.substringAfterLast('.') ?: "ELEMENT"
            }
            SemanticElement(role, label, element.resourceId.orEmpty())
        }.distinctBy { Triple(it.role, it.label, it.resourceId) }

        return SemanticScreenContext(
            packageName = context.packageName,
            windowTitle = context.windowTitle,
            visibleText = visibleText,
            actionableElements = actionable,
            flags = flags
        )
    }
}
