package com.aurax.operator.vision.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * On-device bundled ML Kit OCR implementation.
 *
 * This implementation is intentionally limited to the bundled Latin recognizer.
 * Language/script expansion must use a compatible ML Kit recognizer or another
 * real local OCR runtime; it must not be represented as supported by inference.
 */
class MlKitOcrRuntime : OcrRuntime {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    @Volatile private var status: OcrRuntimeStatus = OcrRuntimeStatus.READY

    override fun isAvailable(): Boolean = status == OcrRuntimeStatus.READY

    override fun getStatus(): OcrRuntimeStatus = status

    override suspend fun recognize(bitmap: Bitmap): OcrResult {
        if (!isAvailable()) {
            return OcrResult("", emptyList(), null, "OCR runtime is unavailable")
        }

        return try {
            val result = suspendCancellableCoroutine { continuation ->
                recognizer.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener { visionText ->
                        val blocks = visionText.textBlocks.mapNotNull { block ->
                            val box = block.boundingBox ?: return@mapNotNull null
                            OcrBlock(
                                text = block.text,
                                left = box.left,
                                top = box.top,
                                right = box.right,
                                bottom = box.bottom
                            )
                        }
                        if (continuation.isActive) {
                            continuation.resume(OcrResult(
                                text = visionText.text,
                                blocks = blocks,
                                confidence = null
                            ))
                        }
                    }
                    .addOnFailureListener { error ->
                        if (continuation.isActive) {
                            continuation.resume(OcrResult(
                                text = "",
                                blocks = emptyList(),
                                confidence = null,
                                error = error.message ?: "OCR recognition failed"
                            ))
                        }
                    }
            }
            if (result.error != null) status = OcrRuntimeStatus.ERROR
            result
        } catch (error: Exception) {
            status = OcrRuntimeStatus.ERROR
            OcrResult("", emptyList(), null, error.message ?: "OCR recognition failed")
        }
    }
}
