package com.aurax.operator.nexus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.aurax.operator.ai.model.ModelRepository
import com.aurax.operator.ai.runtime.LlamaCppRuntime
import java.io.File

/**
 * Bridges the static NEXUS capability catalog to real device state.
 *
 * A capability is reported as available only when its actual prerequisite is
 * present. In particular, a valid GGUF file alone is not enough for llama.cpp:
 * the packaged JNI runtime must also have loaded successfully.
 */
class NexusRuntimeAvailabilityProvider(context: Context) {
    private val appContext = context.applicationContext
    private val modelRepository = ModelRepository(appContext)
    private val llamaRuntime = LlamaCppRuntime(appContext)

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
            // MODEL_GATED capabilities must only become available when the model
            // asset is actually valid. llama.cpp additionally requires its JNI
            // runtime to have loaded successfully.
            if (llamaRuntime.isOperational()) add("llama.cpp + GGUF")
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
