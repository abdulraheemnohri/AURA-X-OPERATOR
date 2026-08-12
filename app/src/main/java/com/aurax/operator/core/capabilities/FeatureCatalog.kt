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
        Capability("accessibility", "Accessibility Operator", "Read UI nodes and perform guarded actions.", CapabilityStatus.REQUIRES_PERMISSION),
        Capability("screen_context", "Screen Understanding", "Extract visible text, clickable controls, sensitive fields and private browsing state.", CapabilityStatus.READY),
        Capability("abort", "Emergency Abort", "Volume Down, notification action and floating indicator stop automation.", CapabilityStatus.READY),
        Capability("policy", "Automation Policy", "Observe, suggest, confirm or low-risk automatic execution.", CapabilityStatus.READY),
        Capability("chrome", "Chrome Safe Automation", "Open/search/navigate Chrome while refusing sensitive flows.", CapabilityStatus.READY),
        Capability("youtube", "YouTube Safe Automation", "Search/play/navigation without likes, subscriptions, comments or ads.", CapabilityStatus.READY),
        Capability("system", "System Navigation", "Open installed applications and supported Android settings screens.", CapabilityStatus.READY),
        Capability("audit", "Safety Audit Trail", "Persist task/action/safety events locally and export CSV.", CapabilityStatus.READY),
        Capability("model", "Local GGUF AI", "Import and run the configured local llama.cpp model when a compatible GGUF is installed.", CapabilityStatus.REQUIRES_MODEL),
        Capability("vision", "Vision Understanding", "Screenshot capture is available; multimodal interpretation requires a compatible vision model.", CapabilityStatus.REQUIRES_MODEL),
        Capability("stt", "Whisper STT", "Native Whisper bridge is exposed; a Whisper model must be supplied in app-private storage.", CapabilityStatus.REQUIRES_MODEL),
        Capability("tts", "Piper TTS", "Piper integration point is exposed; a compatible voice model/runtime must be supplied.", CapabilityStatus.REQUIRES_MODEL),
        Capability("biometric", "Operator Unlock", "BiometricPrompt can protect operator sessions.", CapabilityStatus.REQUIRES_PERMISSION),
        Capability("local_only", "Local-first Privacy", "No analytics or cloud automation endpoint is required by the operator core.", CapabilityStatus.READY)
    )

    fun byId(id: String): Capability? = all.firstOrNull { it.id == id }
}
