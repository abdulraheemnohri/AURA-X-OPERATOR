package com.aurax.operator.plugin

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PluginRegistryTest {
    @Test
    fun registersTrustedPluginAndExposesTools() = runBlocking {
        val registry = PluginRegistry()
        val plugin = object : OperatorPlugin {
            override val descriptor = PluginDescriptor("test.plugin", "Test", "1.0.0", "AURA-X")
            override fun tools() = listOf(
                OperatorTool("test.echo", "Echo input", ToolRisk.LOW) { ToolResult(true, "ok") }
            )
        }

        assertTrue(registry.register(plugin).isSuccess)
        assertEquals(1, registry.tools().size)
        assertEquals("test.echo", registry.tools().first().id)
    }

    @Test
    fun rejectsDuplicatePluginIds() {
        val registry = PluginRegistry()
        val plugin = object : OperatorPlugin {
            override val descriptor = PluginDescriptor("duplicate.plugin", "Test", "1.0.0", "AURA-X")
            override fun tools() = emptyList<OperatorTool>()
        }

        assertTrue(registry.register(plugin).isSuccess)
        assertTrue(registry.register(plugin).isFailure)
    }
}
