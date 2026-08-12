package com.aurax.operator.voice.tts

/** Local Piper backend facade. A compatible Piper runtime and voice model must be supplied by the user. */
class PiperTTS {
    fun isAvailable(): Boolean = false
    fun speak(modelPath: String, text: String): Boolean = false
    fun stop() = Unit
}
