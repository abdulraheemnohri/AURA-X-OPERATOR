package com.aurax.operator.operator.ocr

import android.graphics.Bitmap

/** Compatibility contract for legacy screen-perception consumers. */
interface OcrEngine {
    fun extractText(bitmap: Bitmap): String
}
