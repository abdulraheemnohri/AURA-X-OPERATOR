package com.aurax.operator.nexus

/**
 * Single entry point for runtime capability checks.
 * UI, settings and automation callers should consult this gate before starting
 * a capability so configuration cannot bypass runtime availability checks.
 */
class NexusCapabilityGate(
    private val availabilityProvider: () -> NexusRuntimeAvailability
) {
    fun isAvailable(capabilityId: String): Boolean {
        val capability = NexusFeatureRegistry.byId(capabilityId) ?: return false
        return NexusFeatureRegistry.available(availabilityProvider(), capability)
    }

    fun requireAvailable(capabilityId: String) {
        val capability = NexusFeatureRegistry.byId(capabilityId)
            ?: error("Unknown NEXUS capability: $capabilityId")
        check(NexusFeatureRegistry.available(availabilityProvider(), capability)) {
            "NEXUS capability is unavailable: ${capability.id} (${capability.state})"
        }
    }

    fun availableCapabilities(): List<NexusCapability> = NexusFeatureRegistry.all.filter {
        NexusFeatureRegistry.available(availabilityProvider(), it)
    }
}
