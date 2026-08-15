package com.aurax.operator.plugin

/**
 * Safe in-process plugin contract. Plugins are metadata + deterministic tool handlers;
 * arbitrary APK/dex loading is deliberately outside this API.
 */
interface OperatorPlugin {
    val descriptor: PluginDescriptor
    fun tools(): List<OperatorTool>
}

data class PluginDescriptor(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val permissions: Set<PluginPermission> = emptySet()
)

enum class PluginPermission {
    SCREEN_READ,
    ACCESSIBILITY_ACTION,
    NETWORK,
    NOTIFICATIONS,
    MICROPHONE,
    STORAGE
}

data class OperatorTool(
    val id: String,
    val description: String,
    val risk: ToolRisk,
    val handler: suspend (Map<String, String>) -> ToolResult
)

enum class ToolRisk { LOW, MEDIUM, HIGH, BLOCKED }

data class ToolResult(val success: Boolean, val message: String)
