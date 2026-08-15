package com.aurax.operator.nexus

enum class CapabilityState { READY, PERMISSION_GATED, MODEL_GATED, OPTIONAL_RUNTIME_GATED, NOT_BUNDLED, DISABLED }
enum class CapabilityDomain { OPERATOR, VOICE, VISION, MEMORY, RAG, AUTOMATION, TOOLS, PLUGINS, MODELS, SECURITY, SETTINGS, ANALYTICS, BACKUP, CONNECTIVITY, UI }
data class NexusCapability(val id: String, val name: String, val domain: CapabilityDomain, val state: CapabilityState, val description: String, val requiredPermissions: Set<String> = emptySet(), val runtimeRequirement: String? = null)
data class NexusRuntimeAvailability(val grantedPermissions: Set<String> = emptySet(), val installedModels: Set<String> = emptySet(), val installedRuntimes: Set<String> = emptySet())

object NexusFeatureRegistry {
    val all = listOf(
        NexusCapability("operator.closed_loop", "Closed-loop operator", CapabilityDomain.OPERATOR, CapabilityState.READY, "Understand, plan, execute, verify and recover."),
        NexusCapability("operator.abort", "Emergency abort", CapabilityDomain.OPERATOR, CapabilityState.READY, "Cancel active operator work through the safety controller."),
        NexusCapability("operator.countdown", "Action countdown", CapabilityDomain.OPERATOR, CapabilityState.READY, "Confirmation window before consequential actions."),
        NexusCapability("operator.verification", "Post-action verification", CapabilityDomain.OPERATOR, CapabilityState.READY, "Verify outcomes and enter recovery when needed."),
        NexusCapability("voice.continuous", "Continuous conversation", CapabilityDomain.VOICE, CapabilityState.OPTIONAL_RUNTIME_GATED, "Keep voice sessions alive."),
        NexusCapability("voice.wake_word", "Wake word", CapabilityDomain.VOICE, CapabilityState.OPTIONAL_RUNTIME_GATED, "Hands-free activation.", runtimeRequirement = "wake-word-engine"),
        NexusCapability("voice.whisper", "Whisper speech recognition", CapabilityDomain.VOICE, CapabilityState.MODEL_GATED, "Local speech recognition.", runtimeRequirement = "whisper-model"),
        NexusCapability("vision.ocr", "OCR", CapabilityDomain.VISION, CapabilityState.OPTIONAL_RUNTIME_GATED, "Extract text from images.", runtimeRequirement = "vision-runtime"),
        NexusCapability("vision.image_understanding", "Image understanding", CapabilityDomain.VISION, CapabilityState.MODEL_GATED, "Analyze images with a local vision model.", runtimeRequirement = "vision-model"),
        NexusCapability("memory.local", "Persistent local memory", CapabilityDomain.MEMORY, CapabilityState.READY, "Store local state and preferences."),
        NexusCapability("memory.graph", "Memory graph", CapabilityDomain.MEMORY, CapabilityState.OPTIONAL_RUNTIME_GATED, "Relate memories and entities.", runtimeRequirement = "memory-graph"),
        NexusCapability("rag.local", "Local RAG", CapabilityDomain.RAG, CapabilityState.READY, "Retrieve relevant local documents."),
        NexusCapability("rag.embeddings", "Embedding index", CapabilityDomain.RAG, CapabilityState.MODEL_GATED, "Semantic indexing.", runtimeRequirement = "embedding-model"),
        NexusCapability("automation.workflows", "Automation workflows", CapabilityDomain.AUTOMATION, CapabilityState.READY, "Run deterministic workflows."),
        NexusCapability("automation.scheduled", "Scheduled automation", CapabilityDomain.AUTOMATION, CapabilityState.PERMISSION_GATED, "Run approved background workflows."),
        NexusCapability("tools.files", "File tools", CapabilityDomain.TOOLS, CapabilityState.PERMISSION_GATED, "Safely access user-selected files."),
        NexusCapability("tools.apps", "App control", CapabilityDomain.TOOLS, CapabilityState.PERMISSION_GATED, "Invoke supported Android intents."),
        NexusCapability("tools.network", "Network tools", CapabilityDomain.TOOLS, CapabilityState.PERMISSION_GATED, "Use explicitly allowed network tools."),
        NexusCapability("plugins.trusted_sdk", "Trusted plugin SDK", CapabilityDomain.PLUGINS, CapabilityState.READY, "Execute trusted in-process plugin contracts."),
        NexusCapability("plugins.downloaded_code", "Downloaded executable plugins", CapabilityDomain.PLUGINS, CapabilityState.DISABLED, "Arbitrary APK/Dex execution remains disabled."),
        NexusCapability("models.hub", "Model Hub", CapabilityDomain.MODELS, CapabilityState.READY, "Manage supported model assets."),
        NexusCapability("models.llama_cpp", "llama.cpp inference", CapabilityDomain.MODELS, CapabilityState.MODEL_GATED, "Local GGUF inference.", runtimeRequirement = "llama.cpp + GGUF"),
        NexusCapability("models.safetensors", "SafeTensors inference", CapabilityDomain.MODELS, CapabilityState.NOT_BUNDLED, "Requires a supported native inference backend."),
        NexusCapability("models.converter", "Model conversion", CapabilityDomain.MODELS, CapabilityState.NOT_BUNDLED, "Requires a dedicated conversion toolchain."),
        NexusCapability("security.safety_controller", "Safety controller", CapabilityDomain.SECURITY, CapabilityState.READY, "Centralized abort and confirmation controls."),
        NexusCapability("security.audit_log", "Audit log", CapabilityDomain.SECURITY, CapabilityState.READY, "Persist important lifecycle events."),
        NexusCapability("settings.staged", "Staged settings", CapabilityDomain.SETTINGS, CapabilityState.READY, "Validate and apply settings safely."),
        NexusCapability("analytics.dashboard", "Analytics dashboard", CapabilityDomain.ANALYTICS, CapabilityState.READY, "Display local operator metrics."),
        NexusCapability("backup.encrypted", "Encrypted backup/restore", CapabilityDomain.BACKUP, CapabilityState.READY, "Protect supported local state."),
        NexusCapability("connectivity.loopback", "Loopback companion", CapabilityDomain.CONNECTIVITY, CapabilityState.READY, "Local companion communication."),
        NexusCapability("connectivity.discovery", "LAN discovery", CapabilityDomain.CONNECTIVITY, CapabilityState.PERMISSION_GATED, "Discover explicitly allowed local services."),
        NexusCapability("ui.home_widget", "Home-screen cockpit", CapabilityDomain.UI, CapabilityState.READY, "Quick operator controls from the launcher.")
    )
    fun byId(id: String) = all.firstOrNull { it.id == id }
    fun available(runtime: NexusRuntimeAvailability, capability: NexusCapability): Boolean = when (capability.state) {
        CapabilityState.READY -> true
        CapabilityState.PERMISSION_GATED -> capability.requiredPermissions.all(runtime.grantedPermissions::contains)
        CapabilityState.MODEL_GATED -> runtime.installedModels.contains(capability.runtimeRequirement)
        CapabilityState.OPTIONAL_RUNTIME_GATED -> runtime.installedRuntimes.contains(capability.runtimeRequirement)
        CapabilityState.NOT_BUNDLED, CapabilityState.DISABLED -> false
    }
}
