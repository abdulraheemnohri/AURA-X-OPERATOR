package com.aurax.operator.core.capabilities

/** Single source of truth for the product capability surface. */
enum class CapabilityStatus { READY, REQUIRES_PERMISSION, REQUIRES_MODEL, NOT_BUNDLED }

data class Capability(
    val id: String,
    val title: String,
    val description: String,
    val status: CapabilityStatus
)

object FeatureCatalog {
    val all = listOf(
        Capability("accessibility", "Accessibility Operator", "Read UI nodes and perform guarded actions across the device after explicit enablement.", CapabilityStatus.REQUIRES_PERMISSION),
        Capability("screen_context", "Screen Understanding", "Extract visible text, clickable controls, sensitive fields and private browsing state.", CapabilityStatus.READY),
        Capability("abort", "Emergency Abort", "Volume Down, notification action, floating indicator, Quick Settings and home-screen widget can stop automation.", CapabilityStatus.READY),
        Capability("policy", "Automation Policy", "Observe, suggest, confirm or low-risk automatic execution.", CapabilityStatus.READY),
        Capability("chrome", "Chrome Safe Automation", "Open/search/navigate Chrome while refusing sensitive flows.", CapabilityStatus.READY),
        Capability("youtube", "YouTube Safe Automation", "Search/play/navigation without likes, subscriptions, comments or ads.", CapabilityStatus.READY),
        Capability("system", "System Navigation", "Open installed applications and supported Android settings screens.", CapabilityStatus.READY),
        Capability("notifications", "Notification Reader", "Optional user-granted in-memory notification summaries with protected-package filtering.", CapabilityStatus.REQUIRES_PERMISSION),
        Capability("audit", "Safety Audit Trail", "Persist task/action/safety events locally and export CSV.", CapabilityStatus.READY),
        Capability("model", "Local GGUF AI", "Import and run the configured local llama.cpp model when a compatible GGUF is installed.", CapabilityStatus.REQUIRES_MODEL),
        Capability("model_planning", "Local AI Planning", "Use the local GGUF model for constrained plan proposals with deterministic fallback.", CapabilityStatus.REQUIRES_MODEL),
        Capability("vision", "Vision Understanding", "Screenshot capture is available; multimodal interpretation requires a compatible vision model.", CapabilityStatus.REQUIRES_MODEL),
        Capability("stt", "Whisper STT", "Native Whisper bridge is exposed; a Whisper model must be supplied in app-private storage.", CapabilityStatus.REQUIRES_MODEL),
        Capability("wake_gate", "Wake Phrase Gate", "Engine-neutral wake/continuous-conversation state is available; detector inference remains model-gated.", CapabilityStatus.REQUIRES_MODEL),
        Capability("tts", "Local TTS", "On-device Android TTS compatibility is available; native Piper remains optional.", CapabilityStatus.READY),
        Capability("biometric", "Operator Unlock", "BiometricPrompt can protect operator sessions.", CapabilityStatus.REQUIRES_PERMISSION),
        Capability("memory", "Local Memory", "Persist and retrieve operator memories locally with retention controls.", CapabilityStatus.READY),
        Capability("rag", "Local RAG", "File-backed chunking, deterministic local embeddings and top-K semantic retrieval.", CapabilityStatus.READY),
        Capability("analytics", "Local Analytics", "Room-backed task, memory, safety-event and model lifecycle aggregation.", CapabilityStatus.READY),
        Capability("backup", "Encrypted Backup", "Password-protected local backup export and non-destructive restore/merge.", CapabilityStatus.READY),
        Capability("lan_loopback", "Loopback Companion", "Authenticated companion endpoint bound only to 127.0.0.1.", CapabilityStatus.READY),
        Capability("quick_settings", "Quick Settings Tile", "Emergency abort and cockpit access from Android Quick Settings.", CapabilityStatus.READY),
        Capability("home_widget", "Home Screen Cockpit", "Home-screen Open and Emergency Abort controls with operator status.", CapabilityStatus.READY),
        Capability("plugin_sdk", "Trusted Plugin SDK", "In-process plugin contract and deterministic registry; arbitrary downloaded code remains disabled.", CapabilityStatus.READY),
        Capability("local_only", "Local-first Privacy", "No cloud automation endpoint is required by the operator core.", CapabilityStatus.READY),
        Capability("safetensors", "SafeTensors Runtime", "Direct on-device SafeTensors execution is not bundled.", CapabilityStatus.NOT_BUNDLED),
        Capability("conversion", "Model Conversion", "On-device model conversion/quantization pipeline is not bundled.", CapabilityStatus.NOT_BUNDLED)
    )

    fun byId(id: String): Capability? = all.firstOrNull { it.id == id }
}
