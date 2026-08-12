package com.aurax.operator.voice.stt

/** Local Whisper backend facade. A compatible whisper.cpp model must be supplied by the user. */
class WhisperRecognizer {
    fun isAvailable(): Boolean = false
    fun start(modelPath: String, language: String = "auto", onText: (String) -> Unit): Boolean = false
    fun stop() = Unit
}
