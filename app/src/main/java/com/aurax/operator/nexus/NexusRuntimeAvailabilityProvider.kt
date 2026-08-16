package com.aurax.operator.nexus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.aurax.operator.ai.model.ModelRepository
import com.aurax.operator.ai.runtime.LlamaCppRuntime
import com.aurax.operator.ai.vision.LlavaVisionRuntime
import com.aurax.operator.voice.stt.WhisperRecognizer
import java.io.File

/** Bridges the static NEXUS capability catalog to actual device/runtime state. */
class NexusRuntimeAvailabilityProvider(context: Context) {
    private val appContext = context.applicationContext
    private val modelRepository = ModelRepository(appContext)
    private val llamaRuntime = LlamaCppRuntime(appContext)
    private val whisperRecognizer = WhisperRecognizer()
    private val visionRuntime = LlavaVisionRuntime()

    fun snapshot(): NexusRuntimeAvailability {
        val permissions = buildSet {
            if (appContext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) add(Manifest.permission.RECORD_AUDIO)
            if (appContext.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) add(Manifest.permission.CAMERA)
            if (appContext.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) add(Manifest.permission.ACCESS_NETWORK_STATE)
        }

        val whisperModel = File(appContext.filesDir, "models/whisper-base.bin")
        val visionModel = File(appContext.filesDir, "models/vision-model.bin")
        val embeddingModel = File(appContext.filesDir, "models/embedding-model.bin")

        val models = buildSet {
            if (llamaRuntime.isOperational()) add("llama.cpp + GGUF")
            if (whisperModel.isFile && whisperModel.length() > 0L) add("whisper-model")
            if (visionModel.isFile && visionModel.length() > 0L) add("vision-model")
            if (embeddingModel.isFile && embeddingModel.length() > 0L) add("embedding-model")
        }

        val runtimes = buildSet {
            if (llamaRuntime.isOperational()) add("llama.cpp")
            if (whisperRecognizer.isRuntimeAvailable() && whisperModel.isFile && whisperModel.length() > 0L) add("whisper-runtime")
            if (visionRuntime.isNativeRuntimeAvailable() && visionModel.isFile && visionModel.length() > 0L) add("vision-runtime")
            if (File(appContext.filesDir, "runtimes/wake-word-engine").isFile) add("wake-word-engine")
            if (File(appContext.filesDir, "runtimes/memory-graph").isFile) add("memory-graph")
        }

        return NexusRuntimeAvailability(
            grantedPermissions = permissions,
            installedModels = models,
            installedRuntimes = runtimes
        )
    }
}
