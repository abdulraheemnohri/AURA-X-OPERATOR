package com.aurax.operator.nexus

/**
 * Single source of truth for the NEXUS capability surface.
 *
 * A capability is never reported READY unless the runtime that executes it is
 * actually present. This prevents UI/settings from advertising functionality
 * that is only a design contract or requires an optional native/model bundle.
 */
enum class CapabilityState {
    READY,
    PERMISSION_GATED,
    MODEL_GATED,
    OPTIONAL_RUNTIME_GATED,
    NOT_BUNDLED,
    DISABLED
}

enum class CapabilityDomain {
    OPERATOR,
    VOICE,
    VISION,
    MEMORY,
    RAG,
    AUTOMATION,
    TOOLS,
    PLUGINS,
    MODELS,
    SECURITY,
    SETTINGS,
    ANALYTICS,
    BACKUP,
    CONNECTIVITY,
    UI
}

data class NexusCapability(
    val id: String,
    val name: String,
    val domain: CapabilityDomain,
    val state: CapabilityState,
    val description: String,
    val requiredPermissions: Set<String> = emptySet(),
    val runtimeRequirement: String? = null
)

object NexusFeatureRegistry {
    val all: List<NexusCapability> = listOf(
        NexusCapability("operator.closed_loop", "Closed-loop operator", CapabilityDomain.OPERATOR, CapabilityState.READY, "Understand, plan, execute, verify and recover through the existing operator lifecycle."),
        NexusCapability("operator.abort", "Emergency abort", CapabilityDomain.OPERATOR, CapabilityState.READY, "Cancel active operator work through the safety controller."),
        NexusCapability("operator.countdown", "Action countdown", CapabilityDomain.OPERATOR, CapabilityState.READY, "Expose a confirmation window before consequential actions."),
        NexusCapability("operator.verification", "Post-action verification", CapabilityDomain.OPERATOR, CapabilityState.READY, "Track verification and recovery state after execution."),
        NexusCapability("voice.continuous", "Continuous conversation", CapabilityDomain.VOICE, CapabilityState.OPTIONAL_RUNTIME_GATED, "Keep the voice session alive between turns."),
        NexusCapability("voice.wake_word", "Wake word", CapabilityDomain.VOICE, CapabilityState.OPTIONAL_RUNTIME_GATED, "Hands-free activation through an installed wake-word runtime.", runtimeRequirement = "wake-word-engine"),
        NexusCapability("voice.whisper", "Whisper speech recognition", CapabilityDomain.VOICE, CapabilityState.MODEL_GATED, "On-device speech recognition when a compatible Whisper model is installed.", runtimeRequirement = "whisper-model"),
        NexusCapability("vision.ocr", "OCR", CapabilityDomain.VISION, CapabilityState.OPTIONAL_RUNTIME_GATED, "Extract text from camera or supplied images.", runtimeRequirement = "vision-runtime"),
        NexusCapability("vision.image_understanding", "Image understanding", CapabilityDomain.VISION, CapabilityState.MODEL_GATED, "Analyze images with a compatible local vision model.", runtimeRequirement = "vision-model"),
        NexusCapability("memory.local", "Persistent local memory", CapabilityDomain.MEMORY, CapabilityState.READY, "Store operator state and preferences locally."),
        NexusCapability("memory.graph", "Memory graph", CapabilityDomain.MEMORY, CapabilityState.OPTIONAL_RUNTIME_GATED, "Relate entities and memories into a queryable graph.", runtimeRequirement = "memory-graph"),
        NexusCapability("rag.local", "Local RAG", CapabilityDomain.RAG, CapabilityState.READY, "Retrieve relevant local documents for grounded responses."),
        NexusCapability("rag.embeddings", "Embedding index", CapabilityDomain.RAG, CapabilityState.MODEL_GATED, "Semantic indexing with an installed embedding model.", runtimeRequirement = "embedding-model"),
        NexusCapability("automation.workflows", "Automation workflows", CapabilityDomain.AUTOMATION, CapabilityState.READY, "Run deterministic workflows through the existing safety layer."),
        NexusCapability("automation.scheduled", "Scheduled automation", CapabilityDomain.AUTOMATION, CapabilityState.PERMISSION_GATED, "Run approved background workflows."),
        NexusCapability("tools.files", "File tools", CapabilityDomain.TOOLS, CapabilityState.PERMISSION_GATED, "Read and write user-selected files safely."),
        NexusCapability("tools.apps", "App control", CapabilityDomain.TOOLS, CapabilityState.PERMISSION_GATED, "Invoke supported Android intents and app actions."),
        NexusCapability("tools.network", "Network tools", CapabilityDomain.TOOLS, CapabilityState.PERMISSION_GATED, "Use network-connected tools when explicitly allowed."),
        NexusCapability("plugins.trusted_sdk", "Trusted plugin SDK", CapabilityDomain.PLUGINS, CapabilityState.READY, "Execute in-process trusted plugin contracts."),
        NexusCapability("plugins.downloaded_code", "Downloaded executable plugins", CapabilityDomain.PLUGINS, CapabilityState.DISABLED, "Arbitrary APK/Dex execution is intentionally disabled."),
        NexusCapability("models.hub", "Model Hub", CapabilityDomain.MODELS, CapabilityState.READY, "Discover, download and manage supported model assets."),
        NexusCapability("models.llama_cpp", "llama.cpp inference", CapabilityDomain.MODELS, CapabilityState.MODEL_GATED, "Local LLM inference when a compatible GGUF model is installed.", runtimeRequirement = "llama.cpp + GGUF"),
        NexusCapability("models.safetensors", "SafeTensors inference", CapabilityDomain.MODELS, CapabilityState.NOT_BUNDLED, "Requires a supported native inference backend."),
        NexusCapability("models.converter", "Model conversion", CapabilityDomain.MODELS, CapabilityState.NOT_BUNDLED, "Conversion requires a dedicated conversion runtime/toolchain."),
        NexusCapability("security.safety_controller", "Safety controller", CapabilityDomain.SECURITY, CapabilityState.READY, "Centralized abort, confirmation and risk controls."),
        NexusCapability("security.audit_log", "Audit log", CapabilityDomain.SECURITY, CapabilityState.READY, "Persist operator actions and important lifecycle events."),
        NexusCapability("settings.staged", "Staged settings", CapabilityDomain.SETTINGS, CapabilityState.READY, "Edit, validate and apply runtime settings safely."),
        NexusCapability("analytics.dashboard", "Analytics dashboard", CapabilityDomain.ANALYTICS, CapabilityState.READY, "Display local task, latency and success metrics."),
        NexusCapability("backup.encrypted", "Encrypted backup/restore", CapabilityDomain.BACKUP, CapabilityState.READY, "Protect supported settings and local state with encrypted backup."),
        NexusCapability("connectivity.loopback", "Loopback companion", CapabilityDomain.CONNECTIVITY, CapabilityState.READY, "Local companion communication without exposing an unauthenticated public endpoint."),
        NexusCapability("connectivity.discovery", "LAN discovery", CapabilityDomain.CONNECTIVITY, CapabilityState.PERMISSION_GATED, "Discover explicitly allowed local services."),
        NexusCapability("ui.home_widget", "Home-screen cockpit", CapabilityDomain.UI, CapabilityState.READY, "Quick operator controls from the Android launcher.")
    )

    fun byId(id: String): NexusCapability? = all.firstOrNull { it.id == id }

    fun available(runtime: NexusRuntimeAvailability, capability: NexusCapability): Boolean = when (capability.state) {
        CapabilityState.READY -> true
        CapabilityState.PERMISSION_GATED -> capability.requiredPermissions.all(runtime.grantedPermissions::contains)
        CapabilityState.MODEL_GATED -> runtime.installedModels.contains(capability.runtimeRequirement)
        CapabilityState.OPTIONAL_RUNTIME_GATED -> runtime.installedRuntimes.contains(capability.runtimeRequirement)
        CapabilityState.NOT_BUNDLED, CapabilityState.DISABLED -> false
    }
}

data class NexusRuntimeAvailability(
    val grantedPermissions: Set<String> = emptySet(),
    val installedModels: Set<String> = emptySet(),
    val installedRuntimes: Set<String> = emptySet()
)
