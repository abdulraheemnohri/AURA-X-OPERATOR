package com.aurax.operator.nexus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.aurax.operator.ai.model.ModelRepository
import java.io.File

/**
 * Bridges the static NEXUS capability catalog to real device state.
 *
 * This deliberately uses conservative checks: a missing runtime/model is never
 * reported as available merely because the corresponding setting is enabled.
 */
class NexusRuntimeAvailabilityProvider(context: Context) {
    private val appContext = context.applicationContext
    private val modelRepository = ModelRepository(appContext)

    fun snapshot(): NexusRuntimeAvailability {
        val permissions = buildSet {
            if (appContext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.RECORD_AUDIO)
            }
            if (appContext.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.CAMERA)
            }
            if (appContext.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.ACCESS_NETWORK_STATE)
            }
        }

        val models = buildSet {
            if (modelRepository.isInstalled()) add("llama.cpp + GGUF")
            if (File(appContext.filesDir, "models/whisper-base.bin").isFile) add("whisper-model")
            if (File(appContext.filesDir, "models/vision-model.bin").isFile) add("vision-model")
            if (File(appContext.filesDir, "models/embedding-model.bin").isFile) add("embedding-model")
        }

        val runtimes = buildSet {
            if (File(appContext.filesDir, "runtimes/wake-word-engine").isFile) add("wake-word-engine")
            if (File(appContext.filesDir, "runtimes/vision-runtime").isFile) add("vision-runtime")
            if (File(appContext.filesDir, "runtimes/memory-graph").isFile) add("memory-graph")
        }

        return NexusRuntimeAvailability(
            grantedPermissions = permissions,
            installedModels = models,
            installedRuntimes = runtimes
        )
    }
}
