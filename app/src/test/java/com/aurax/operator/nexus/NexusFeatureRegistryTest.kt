package com.aurax.operator.nexus

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NexusFeatureRegistryTest {
    @Test
    fun registryContainsCoreNexusCapabilities() {
        assertNotNull(NexusFeatureRegistry.byId("operator.closed_loop"))
        assertNotNull(NexusFeatureRegistry.byId("rag.local"))
        assertNotNull(NexusFeatureRegistry.byId("plugins.trusted_sdk"))
        assertNotNull(NexusFeatureRegistry.byId("backup.encrypted"))
    }

    @Test
    fun disabledDownloadedCodeCanNeverBecomeAvailable() {
        val capability = NexusFeatureRegistry.byId("plugins.downloaded_code")!!
        assertFalse(NexusFeatureRegistry.available(capability = capability, runtime = NexusRuntimeAvailability()))
    }

    @Test
    fun modelGatedCapabilityRequiresItsRuntimeAsset() {
        val capability = NexusFeatureRegistry.byId("models.llama_cpp")!!
        assertFalse(NexusFeatureRegistry.available(capability, NexusRuntimeAvailability()))
        assertTrue(
            NexusFeatureRegistry.available(
                capability,
                NexusRuntimeAvailability(installedModels = setOf("llama.cpp + GGUF"))
            )
        )
    }
}
