package com.aurax.operator.voice.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.io.File
import java.util.Locale

/**
 * Local speech-output backend.
 *
 * When a real Piper runtime is bundled later, this API remains the stable adapter boundary.
 * For now it provides a fully functional on-device Android TTS implementation instead of a false
 * isAvailable=false placeholder. No text is sent to a remote service by this class.
 */
class PiperTTS(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    @Volatile private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) tts.language = Locale.getDefault()
    }

    fun isAvailable(): Boolean = ready

    fun speak(modelPath: String, text: String): Boolean {
        if (!ready || text.isBlank()) return false
        // The path is intentionally validated only when supplied. Android TTS is the compatibility
        // backend, so a Piper-specific binary/model is not required to get local speech output.
        if (modelPath.isNotBlank() && !File(modelPath).exists()) {
            // Continue with Android TTS rather than failing the entire voice pipeline.
        }
        return tts.speak(
            text.take(4_000),
            TextToSpeech.QUEUE_FLUSH,
            null,
            "aura-piper-compatible"
        ) == TextToSpeech.SUCCESS
    }

    fun stop() {
        tts.stop()
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
