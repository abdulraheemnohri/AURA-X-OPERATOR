package com.aurax.operator.ai.model

import android.content.Context
import android.net.Uri
import java.io.File
import java.security.MessageDigest

/** Local-only model lifecycle, validation and integrity diagnostics. */
class ModelRepository(private val context: Context) {
    private val modelDir = File(context.filesDir, "models").apply { mkdirs() }

    val primaryModel: File
        get() = File(modelDir, RECOMMENDED_FILENAME)

    fun isInstalled(): Boolean = status().isValid

    fun status(): ModelStatus = ModelStatus.validateFile(primaryModel)

    fun importPrimaryModel(uri: Uri): File {
        val tempFile = File(modelDir, "$RECOMMENDED_FILENAME.part")
        tempFile.delete()

        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: error("Unable to open selected model")

            input.use { source ->
                tempFile.outputStream().use { output ->
                    source.copyTo(output, bufferSize = COPY_BUFFER_SIZE)
                }
            }

            val candidate = ModelStatus.validateFile(tempFile)
            check(candidate.isValid) {
                candidate.error ?: "Selected file is not a valid GGUF model"
            }

            if (primaryModel.exists() && !primaryModel.delete()) {
                error("Unable to replace the existing primary model")
            }
            check(tempFile.renameTo(primaryModel)) {
                "Unable to finalize model file"
            }
            return primaryModel
        } catch (error: Throwable) {
            tempFile.delete()
            throw error
        }
    }

    fun deletePrimaryModel() {
        primaryModel.delete()
        File(modelDir, "$RECOMMENDED_FILENAME.part").delete()
    }

    companion object {
        const val HF_REPOSITORY = "Qwen/Qwen2.5-0.5B-Instruct-GGUF"
        const val RECOMMENDED_FILENAME = "qwen2.5-0.5b-instruct-q4_k_m.gguf"
        private const val COPY_BUFFER_SIZE = 1024 * 1024
    }
}

data class ModelStatus(
    val isValid: Boolean,
    val sizeBytes: Long,
    val sha256: String?,
    val error: String? = null
) {
    val sizeMb: Long
        get() = sizeBytes / (1024L * 1024L)

    companion object {
        private const val MIN_MODEL_BYTES = 50L * 1024L * 1024L
        private const val BUFFER_SIZE = 1024 * 1024
        private val GGUF_MAGIC = byteArrayOf(
            'G'.code.toByte(),
            'G'.code.toByte(),
            'U'.code.toByte(),
            'F'.code.toByte()
        )
        private val HEX = "0123456789abcdef".toCharArray()

        fun missing(): ModelStatus = ModelStatus(
            isValid = false,
            sizeBytes = 0L,
            sha256 = null,
            error = "Model not installed"
        )

        fun invalid(size: Long, error: String): ModelStatus = ModelStatus(
            isValid = false,
            sizeBytes = size,
            sha256 = null,
            error = error
        )

        fun valid(size: Long, hash: String): ModelStatus = ModelStatus(
            isValid = true,
            sizeBytes = size,
            sha256 = hash,
            error = null
        )

        fun validateFile(file: File): ModelStatus {
            if (!file.isFile) return missing()

            val size = file.length()
            if (size <= MIN_MODEL_BYTES) {
                return invalid(size, "Model file is too small")
            }

            val magicOk = runCatching {
                file.inputStream().use { input ->
                    val magic = ByteArray(GGUF_MAGIC.size)
                    input.read(magic) == magic.size && magic.contentEquals(GGUF_MAGIC)
                }
            }.getOrDefault(false)

            if (!magicOk) {
                return invalid(size, "File is not a GGUF model")
            }

            val digest = MessageDigest.getInstance("SHA-256")
            try {
                file.inputStream().use { input ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (count > 0) digest.update(buffer, 0, count)
                    }
                }
            } catch (_: Throwable) {
                return invalid(size, "Unable to read model for integrity check")
            }

            return valid(size, toHex(digest.digest()))
        }

        private fun toHex(bytes: ByteArray): String {
            val result = CharArray(bytes.size * 2)
            var index = 0
            for (value in bytes) {
                val unsigned = value.toInt() and 0xff
                result[index++] = HEX[unsigned ushr 4]
                result[index++] = HEX[unsigned and 0x0f]
            }
            return String(result)
        }
    }
}
