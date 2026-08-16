package com.aurax.operator.ai.vision

import android.graphics.Bitmap
import android.util.Log

/**
 * JNI boundary for llava.cpp.
 *
 * The native bridge is intentionally conservative: until a real llava.cpp
 * runtime is linked, load() returns false and analyze() reports the capability
 * as unavailable. No fabricated labels or descriptions are ever returned.
 */
class LlavaVisionRuntime : VisionRuntime {
    private var isLoaded = false
    private var status = VisionRuntimeStatus.NOT_LOADED

    override suspend fun analyze(imageInput: ImageInput): VisionResult {
        if (!isLoaded) {
            return VisionResult(
                description = "",
                labels = emptyList(),
                confidence = 0f,
                error = "Vision runtime is not bundled; install a compatible vision runtime first"
            )
        }

        return try {
            nativeAnalyze(imageInput.bitmap, imageInput.prompt)
                ?: VisionResult(
                    description = "",
                    labels = emptyList(),
                    confidence = 0f,
                    error = "Vision runtime returned no result"
                )
        } catch (e: Exception) {
            Log.e("LlavaVisionRuntime", "Vision analysis failed", e)
            VisionResult(
                description = "",
                labels = emptyList(),
                confidence = 0f,
                error = e.message ?: "Vision analysis failed"
            )
        }
    }

    override fun isAvailable(): Boolean = isLoaded && getStatus() == VisionRuntimeStatus.READY

    override fun load(modelPath: String): Boolean {
        status = VisionRuntimeStatus.LOADING
        return try {
            isLoaded = nativeLoad(modelPath)
            status = if (isLoaded) VisionRuntimeStatus.READY else VisionRuntimeStatus.ERROR
            isLoaded
        } catch (e: UnsatisfiedLinkError) {
            Log.e("LlavaVisionRuntime", "Vision native runtime is unavailable", e)
            isLoaded = false
            status = VisionRuntimeStatus.ERROR
            false
        } catch (e: Exception) {
            Log.e("LlavaVisionRuntime", "Error loading vision model", e)
            isLoaded = false
            status = VisionRuntimeStatus.ERROR
            false
        }
    }

    override fun unload() {
        runCatching { nativeUnload() }
            .onFailure { Log.e("LlavaVisionRuntime", "Error unloading vision runtime", it) }
        isLoaded = false
        status = VisionRuntimeStatus.NOT_LOADED
    }

    override fun getStatus(): VisionRuntimeStatus {
        status = runCatching {
            when (nativeGetStatus()) {
                0 -> VisionRuntimeStatus.NOT_LOADED
                1 -> VisionRuntimeStatus.LOADING
                2 -> VisionRuntimeStatus.READY
                else -> VisionRuntimeStatus.ERROR
            }
        }.getOrElse {
            VisionRuntimeStatus.ERROR
        }
        if (status != VisionRuntimeStatus.READY) isLoaded = false
        return status
    }

    private external fun nativeLoad(modelPath: String): Boolean
    private external fun nativeAnalyze(bitmap: Bitmap, prompt: String?): VisionResult?
    private external fun nativeUnload()
    private external fun nativeGetStatus(): Int

    companion object {
        init {
            runCatching { System.loadLibrary("aurax_llava") }
                .onFailure { Log.w("LlavaVisionRuntime", "Vision native library not available") }
        }
    }
}
