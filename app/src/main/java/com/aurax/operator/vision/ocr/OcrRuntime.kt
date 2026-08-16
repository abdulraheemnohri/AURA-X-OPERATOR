package com.aurax.operator.vision.ocr

import android.graphics.Bitmap

/** Real OCR capability boundary. Implementations must report availability truthfully. */
interface OcrRuntime {
    suspend fun recognize(bitmap: Bitmap): OcrResult
    fun isAvailable(): Boolean
    fun getStatus(): OcrRuntimeStatus
}

data class OcrResult(
    val text: String,
    val blocks: List<OcrBlock>,
    val confidence: Float?,
    val error: String? = null
)

data class OcrBlock(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

enum class OcrRuntimeStatus {
    NOT_AVAILABLE,
    READY,
    ERROR
}
