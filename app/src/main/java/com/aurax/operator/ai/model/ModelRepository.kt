package com.aurax.operator.ai.model

import android.content.Context
import android.net.Uri
import java.io.File

class ModelRepository(private val context: Context) {
    private val modelDir = File(context.filesDir, "models").apply { mkdirs() }
    val primaryModel: File get() = File(modelDir, "qwen2.5-0.5b-instruct-q4_k_m.gguf")

    fun isInstalled(): Boolean = primaryModel.isFile && primaryModel.length() > 50L * 1024L * 1024L

    fun importPrimaryModel(uri: Uri): File {
        val tmp = File(modelDir, "qwen2.5-0.5b-instruct-q4_k_m.gguf.part")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open selected model" }
            tmp.outputStream().use { output -> input.copyTo(output, bufferSize = 1024 * 1024) }
        }
        require(tmp.length() > 50L * 1024L * 1024L) { "Selected file is too small to be a Qwen GGUF model" }
        check(tmp.renameTo(primaryModel)) { "Unable to finalize model file" }
        return primaryModel
    }

    fun deletePrimaryModel() {
        primaryModel.delete()
    }

    companion object {
        const val HF_REPOSITORY = "Qwen/Qwen2.5-0.5B-Instruct-GGUF"
        const val RECOMMENDED_FILENAME = "qwen2.5-0.5b-instruct-q4_k_m.gguf"
    }
}
