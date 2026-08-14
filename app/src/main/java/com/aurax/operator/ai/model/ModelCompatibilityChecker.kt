package com.aurax.operator.ai.model

import android.app.ActivityManager
import android.content.Context
import com.aurax.operator.data.entities.ModelEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runtime gate used before loading a model. It avoids predictable OOM/low-memory
 * failures by checking the actual device state instead of only trusting metadata.
 */
@Singleton
class ModelCompatibilityChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun check(model: ModelEntity): CompatibilityResult {
        if (!model.localPath.isNullOrBlank() && !File(model.localPath!!).isFile) {
            return CompatibilityResult(false, "Model file is missing")
        }

        val activityManager = context.getSystemService(ActivityManager::class.java)
            ?: return CompatibilityResult(false, "Android memory manager is unavailable")

        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val requiredBytes = model.minRamMB.coerceAtLeast(256).toLong() * 1024L * 1024L
        if (memoryInfo.lowMemory) {
            return CompatibilityResult(false, "Android reports low memory; close other apps and retry")
        }
        if (memoryInfo.availMem < requiredBytes) {
            val availableMb = memoryInfo.availMem / (1024L * 1024L)
            return CompatibilityResult(
                false,
                "Not enough available RAM: ${availableMb}MB available, ${model.minRamMB}MB required"
            )
        }

        val file = model.localPath?.let(::File)
        if (file != null && file.isFile && model.sizeBytes > 0L && file.length() != model.sizeBytes) {
            return CompatibilityResult(false, "Model file size is inconsistent with the registry")
        }

        return CompatibilityResult(true, null)
    }
}

data class CompatibilityResult(
    val compatible: Boolean,
    val reason: String?
)
