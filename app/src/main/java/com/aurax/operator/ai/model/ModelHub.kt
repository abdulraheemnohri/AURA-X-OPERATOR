package com.aurax.operator.ai.model

import android.content.Context
import android.os.StatFs
import com.aurax.operator.data.database.AuraDao
import com.aurax.operator.data.entities.ModelEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Singleton
class ModelHub @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: AuraDao,
    private val compatibilityChecker: ModelCompatibilityChecker
) {
    val models: Flow<List<ModelEntity>> = dao.observeModels()

    suspend fun get(id: String): ModelEntity? = dao.getModel(id)

    suspend fun bestReadyLlm(): ModelEntity? = models.first()
        .asSequence()
        .filter { it.status == "READY" && it.format == "GGUF" }
        .filter { !it.localPath.isNullOrBlank() && File(it.localPath!!).isFile }
        .filter { it.category.equals("LLM", true) || it.category.equals("CUSTOM", true) }
        .sortedWith(compareByDescending<ModelEntity> { it.benchmarkTokensPerSec ?: 0f }.thenByDescending { it.lastUsed }.thenBy { it.sizeBytes })
        .firstOrNull()

    suspend fun seedBuiltIns() {
        for (model in BuiltInModels.all) if (dao.getModel(model.id) == null) dao.addModel(model)
    }

    /** Registers a public Hub asset. IDs are filesystem-safe because WorkManager uses them in filenames. */
    suspend fun registerHubFile(repo: HuggingFaceModel, file: HuggingFaceFile): ModelEntity {
        val format = when {
            file.path.endsWith(".gguf", true) -> "GGUF"
            file.path.endsWith(".safetensors", true) -> "SAFETENSORS"
            file.path.endsWith(".onnx", true) -> "ONNX"
            file.path.endsWith(".tflite", true) -> "TFLITE"
            else -> "CUSTOM"
        }
        val category = when {
            repo.pipelineTag.contains("text-to-speech", true) -> "TTS"
            repo.pipelineTag.contains("automatic-speech-recognition", true) -> "STT"
            repo.pipelineTag.contains("image", true) || repo.pipelineTag.contains("vision", true) -> "VISION"
            else -> "LLM"
        }
        val id = stableHubId(repo.id, file.path)
        val entity = ModelEntity(
            id = id,
            name = file.path.substringAfterLast('/').safeModelFileName(),
            displayName = "${repo.id} · ${file.path.substringAfterLast('/')}",
            category = category,
            format = format,
            quantization = extractQuantization(file.path),
            sourceUrl = file.downloadUrl,
            sha256 = file.sha256,
            sizeBytes = file.sizeBytes,
            parameters = "unknown",
            status = "AVAILABLE",
            tags = repo.tags.joinToString(","),
            description = "Hugging Face Hub asset from ${repo.id}. Downloads: ${repo.downloads}; likes: ${repo.likes}.",
            license = "See repository card"
        )
        dao.getModel(id)?.let { dao.updateModel(entity) } ?: dao.addModel(entity)
        return entity
    }

    suspend fun importModel(file: File, metadata: ImportedModelMetadata): ModelEntity {
        require(file.isFile) { "Model file does not exist" }
        require(file.length() > 0L) { "Model file is empty" }
        require(metadata.format in setOf("GGUF", "ONNX", "TFLITE", "SAFETENSORS")) { "Unsupported model format: ${metadata.format}" }
        require(availableStorageBytes() > file.length()) { "Not enough free storage" }
        val destination = File(modelDirectory(), file.name.safeModelFileName()).also { it.parentFile?.mkdirs() }
        file.copyTo(destination, overwrite = true)
        val hash = sha256(destination)
        if (metadata.format == "GGUF") {
            require(ModelStatus.validateFile(destination).isValid) { "Imported GGUF failed signature/integrity validation" }
        }
        val entity = ModelEntity(
            id = "imported:${destination.name.lowercase()}", name = destination.name,
            displayName = metadata.displayName.ifBlank { destination.nameWithoutExtension }, category = metadata.category,
            format = metadata.format, quantization = metadata.quantization, sourceUrl = "", localPath = destination.absolutePath,
            sizeBytes = destination.length(), downloadedBytes = destination.length(), sha256 = hash,
            contextLength = metadata.contextLength, parameters = metadata.parameters, status = "READY", isUserImported = true,
            importDate = System.currentTimeMillis(), tags = metadata.tags, description = metadata.description, license = metadata.license
        )
        dao.getModel(entity.id)?.let { dao.updateModel(entity) } ?: dao.addModel(entity)
        return entity
    }

    suspend fun markLoaded(id: String): ModelEntity? {
        val all = models.first()
        val selected = all.firstOrNull { it.id == id } ?: return null
        require(selected.status == "READY") { "Model is not ready" }
        require(!selected.localPath.isNullOrBlank() && File(selected.localPath!!).isFile) { "Model file is not installed" }
        val compatibility = compatibilityChecker.check(selected)
        require(compatibility.compatible) { compatibility.reason ?: "Model is not compatible with this device" }
        all.filter { it.isLoaded && it.id != id }.forEach { dao.updateModel(it.copy(isLoaded = false)) }
        val loaded = selected.copy(isLoaded = true, lastUsed = System.currentTimeMillis())
        dao.updateModel(loaded)
        return loaded
    }

    suspend fun markUnloaded(id: String) { dao.getModel(id)?.let { dao.updateModel(it.copy(isLoaded = false)) } }

    suspend fun recordBenchmark(id: String, tokensPerSec: Float) {
        dao.getModel(id)?.let { dao.updateModel(it.copy(benchmarkTokensPerSec = tokensPerSec, lastUsed = System.currentTimeMillis())) }
    }

    suspend fun remove(id: String) {
        val model = dao.getModel(id) ?: return
        require(!model.isBuiltIn) { "Built-in models cannot be removed" }
        require(!model.isLoaded) { "Unload the model before deleting it" }
        model.localPath?.let { File(it).delete() }
        dao.deleteModel(model)
    }

    fun availableStorageBytes(): Long = StatFs(context.filesDir.absolutePath).availableBytes
    private fun modelDirectory(): File = File(context.filesDir, "models").apply { mkdirs() }

    private fun stableHubId(repoId: String, path: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$repoId\n$path".toByteArray())
            .joinToString("") { "%02x".format(it) }
        return "hf_${digest.take(40)}"
    }

    private fun extractQuantization(path: String): String {
        val stem = path.substringAfterLast('/').substringBeforeLast('.', "")
        val match = Regex("(?i)(Q[0-9](?:_[A-Z0-9]+)+|IQ[0-9]_[A-Z0-9]+)").find(stem)
        return match?.value?.uppercase() ?: ""
    }

    private fun String.safeModelFileName(): String =
        substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "model.bin" }.take(180)

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(1024 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (count > 0) digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val DEFAULT_MODEL_ID = "qwen2.5-0.5b-instruct-q4km"
    }
}

data class ImportedModelMetadata(
    val displayName: String = "", val category: String = "CUSTOM", val format: String,
    val quantization: String = "", val contextLength: Int = 2048, val parameters: String = "0.5B",
    val tags: String = "custom", val description: String = "Imported local model", val license: String = "Unknown"
)

private object BuiltInModels {
    val all = listOf(
        ModelEntity(
            id = "qwen2.5-0.5b-instruct-q4km", name = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
            displayName = "Qwen 2.5 0.5B Instruct Q4_K_M", category = "LLM", format = "GGUF", quantization = "Q4_K_M",
            sourceUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf?download=true",
            sizeBytes = 514_850_816L,
            sha256 = "74a4da8c9fdbcd15bd1f6d01d621410d31c6fc00986f5eb687824e7b93d7a9db",
            parameters = "0.5B", minRamMB = 512, recommendedRamMB = 1024, isBuiltIn = true,
            tags = "chat,fast,urdu,english,gguf,huggingface",
            description = "Small local instruction model for operator planning on constrained devices.", license = "Apache-2.0"
        ),
        ModelEntity(
            id = "whisper-base", name = "Whisper Base", displayName = "Whisper Base STT", category = "STT", format = "CUSTOM",
            quantization = "", sourceUrl = "", parameters = "74M", minRamMB = 512, recommendedRamMB = 1024,
            isBuiltIn = true, tags = "speech,urdu,hindi,english",
            description = "Optional local speech recognition profile. Runtime assets are installed separately.", license = "MIT"
        )
    )
}
