package com.aurax.operator.nexus

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NexusFeatureRegistryTest {
    @Test fun coreCapabilitiesExist() {
        assertNotNull(NexusFeatureRegistry.byId("operator.closed_loop"))
        assertNotNull(NexusFeatureRegistry.byId("rag.local"))
        assertNotNull(NexusFeatureRegistry.byId("plugins.trusted_sdk"))
        assertNotNull(NexusFeatureRegistry.byId("backup.encrypted"))
    }
    @Test fun downloadedCodeIsAlwaysDisabled() {
        val c = NexusFeatureRegistry.byId("plugins.downloaded_code")!!
        assertFalse(NexusFeatureRegistry.available(NexusRuntimeAvailability(), c))
    }
    @Test fun modelGateRequiresInstalledAsset() {
        val c = NexusFeatureRegistry.byId("models.llama_cpp")!!
        assertFalse(NexusFeatureRegistry.available(NexusRuntimeAvailability(), c))
        assertTrue(NexusFeatureRegistry.available(NexusRuntimeAvailability(installedModels = setOf("llama.cpp + GGUF")), c))
    }
}
