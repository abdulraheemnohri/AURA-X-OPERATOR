package com.aurax.operator.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Safe voice facade. It uses Android's installed TTS engine as a compatibility fallback.
 * It never records audio or sends text to a remote service itself.
 * Piper remains an optional native backend when a Piper runtime/model is installed.
 */
class VoiceOutput(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    @Volatile private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) tts.language = Locale.getDefault()
    }

    fun speak(text: String): Boolean {
        if (!ready || text.isBlank()) return false
        return tts.speak(text.take(4000), TextToSpeech.QUEUE_FLUSH, null, "aura-response") == TextToSpeech.SUCCESS
    }

    fun stop() { tts.stop() }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
