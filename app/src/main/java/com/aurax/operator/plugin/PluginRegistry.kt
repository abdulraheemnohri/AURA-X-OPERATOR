package com.aurax.operator.plugin

import java.util.concurrent.ConcurrentHashMap

/**
 * Explicit registry for trusted, in-process plugins. No reflection, downloaded code,
 * or arbitrary third-party APK execution is performed here.
 */
class PluginRegistry {
    private val plugins = ConcurrentHashMap<String, OperatorPlugin>()

    fun register(plugin: OperatorPlugin): Result<Unit> = runCatching {
        require(plugin.descriptor.id.matches(Regex("[a-z0-9._-]{3,64}"))) { "Invalid plugin id" }
        require(plugin.descriptor.version.isNotBlank()) { "Plugin version is required" }
        require(plugin.tools().none { it.id.isBlank() }) { "Every tool needs an id" }
        check(plugins.putIfAbsent(plugin.descriptor.id, plugin) == null) { "Plugin already registered" }
    }

    fun unregister(id: String) { plugins.remove(id) }

    fun get(id: String): OperatorPlugin? = plugins[id]

    fun all(): List<OperatorPlugin> = plugins.values.sortedBy { it.descriptor.name }

    fun tools(): List<OperatorTool> = all().flatMap { it.tools() }
}
