package com.aurax.operator.ai.model

import android.content.Context
import android.net.Uri
import java.io.File
import java.security.MessageDigest

/** Local-only model lifecycle and integrity checks. */
class ModelRepository(private val context: Context) {
    private val modelDir = File(context.filesDir, "models").apply { mkdirs() }
    val primaryModel: File get() = File(modelDir, RECOMMENDED_FILENAME)

    fun isInstalled(): Boolean = status().isValid

    fun status(): ModelStatus {
        val file = primaryModel
        if (!file.isFile) return ModelStatus.missing()
        val size = file.length()
        if (size <= MIN_MODEL_BYTES) return ModelStatus.invalid(size, "Model file is too small")
        if (!hasGgufMagic(file)) return ModelStatus.invalid(size, "File is not a GGUF model")
        return ModelStatus.valid(size, sha256(file))
    }

    fun importPrimaryModel(uri: Uri): File {
        val tmp = File(modelDir, "$RECOMMENDED_FILENAME.part")
        tmp.delete()
        try {
            context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "Unable to open selected model" }
                tmp.outputStream().use { output ->
                    input.copyTo(output, bufferSize = COPY_BUFFER_SIZE)
                }
            }
            val candidate = ModelStatus.validateFile(tmp)
            require(candidate.isValid) { candidate.error ?: "Invalid GGUF model" }
            require(tmp.renameTo(primaryModel)) { "Unable to finalize model file" }
            return primaryModel
        } catch (error: Throwable) {
            tmp.delete()
            throw error
        }
    }

    fun deletePrimaryModel() {
        primaryModel.delete()
        File(modelDir, "$RECOMMENDED_FILENAME.part").delete()
    }

    private fun hasGgufMagic(file: File): Boolean = runCatching {
        file.inputStream().use { input ->
            val magic = ByteArray(4)
            input.read(magic) == 4 && magic.contentEquals(GGUF_MAGIC)
        }
    }.getOrDefault(false)

    private fun sha256(file: File): String = MessageDigest.getInstance("SHA-256").let { digest ->
        file.inputStream().use { input ->
            val buffer = ByteArray(COPY_BUFFER_SIZE)
            var read = input.read(buffer)
            while (read >= 0) {
                if (read > 0) digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val HF_REPOSITORY = "Qwen/Qwen2.5-0.5B-Instruct-GGUF"
        const val RECOMMENDED_FILENAME = "qwen2.5-0.5b-instruct-q4_k_m.gguf"
        private const val MIN_MODEL_BYTES = 50L * 1024L * 1024L
        private const val COPY_BUFFER_SIZE = 1024 * 1024
        private val GGUF_MAGIC = byteArrayOf('G'.code.toByte(), 'G'.code.toByte(), 'U'.code.toByte(), 'F'.code.toByte())
    }
}

data class ModelStatus(
    val isValid: Boolean,
    val sizeBytes: Long,
    val sha256: String?,
    val error: String? = null
) {
    val sizeMb: Long get() = sizeBytes / (1024L * 1024L)

    companion object {
        fun missing() = ModelStatus(false, 0L, null, "Model not installed")
        fun invalid(size: Long, error: String) = ModelStatus(false, size, null, error)
        fun valid(size: Long, hash: String) = ModelStatus(true, size, hash)

        fun validateFile(file: File): ModelStatus {
            if (!file.isFile) return missing()
            if (file.length() <= 50L * 1024L * 1024L) return invalid(file.length(), "Model file is too small")
            val magicOk = runCatching {
                file.inputStream().use { input ->
                    val magic = ByteArray(4)
                    input.read(magic) == 4 && magic.contentEquals(byteArrayOf('G'.code.toByte(), 'G'.code.toByte(), 'U'.code.toByte(), 'F'.code.toByte()))
                }
            }.getOrDefault(false)
            if (!magicOk) return invalid(file.length(), "File is not a GGUF model")
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(1024 * 1024)
                var read = input.read(buffer)
                while (read >= 0) {
                    if (read > 0) digest.update(buffer, 0, read)
                    read = input.read(buffer)
                }
            }
            return valid(file.length(), digest.digest().joinToString("") { "%02x".format(it) })
        }
    }
}
