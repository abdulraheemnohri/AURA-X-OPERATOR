package com.aurax.operator.core.capabilities

/** Runtime gate used so UI/orchestration never treats a gated capability as executable. */
data class CapabilityGate(val status: CapabilityStatus, val reason: String) {
    val usable: Boolean get() = status == CapabilityStatus.READY

    companion object {
        fun evaluate(
            capability: Capability,
            permissionGranted: Boolean = true,
            modelReady: Boolean = true,
            bundled: Boolean = true
        ): CapabilityGate {
            val status = when {
                !bundled || capability.status == CapabilityStatus.NOT_BUNDLED -> CapabilityStatus.NOT_BUNDLED
                capability.status == CapabilityStatus.REQUIRES_PERMISSION && !permissionGranted -> CapabilityStatus.REQUIRES_PERMISSION
                capability.status == CapabilityStatus.REQUIRES_MODEL && !modelReady -> CapabilityStatus.REQUIRES_MODEL
                else -> CapabilityStatus.READY
            }
            val reason = when (status) {
                CapabilityStatus.READY -> "Ready"
                CapabilityStatus.REQUIRES_PERMISSION -> "User permission is required"
                CapabilityStatus.REQUIRES_MODEL -> "A compatible local model/runtime is required"
                CapabilityStatus.NOT_BUNDLED -> "This capability is not bundled in the sideloaded build"
            }
            return CapabilityGate(status, reason)
        }
    }
}
