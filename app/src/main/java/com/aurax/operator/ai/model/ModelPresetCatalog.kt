package com.aurax.operator.ai.model

/** User-facing descriptions for the runtime presets exposed by the control plane. */
data class ModelPresetInfo(
    val preset: ModelPreset,
    val title: String,
    val description: String,
    val recommendedRamMb: Int
)

object ModelPresetCatalog {
    val all: List<ModelPresetInfo> = listOf(
        ModelPresetInfo(ModelPreset.ULTRA_FAST, "Ultra fast", "Small context and output for responsive low-power devices.", 1200),
        ModelPresetInfo(ModelPreset.BALANCED, "Balanced", "Default profile for everyday local operator tasks.", 1800),
        ModelPresetInfo(ModelPreset.QUALITY, "Quality", "Larger context and output for analysis-heavy tasks.", 2600),
        ModelPresetInfo(ModelPreset.MAX_QUALITY, "Max quality", "Highest local inference budget; use while charging and cool.", 3600),
        ModelPresetInfo(ModelPreset.CUSTOM, "Custom", "Keep manually selected runtime controls.", 1600)
    )

    fun find(preset: ModelPreset): ModelPresetInfo = all.first { it.preset == preset }
}
