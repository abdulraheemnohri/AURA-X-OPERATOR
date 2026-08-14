package com.aurax.operator.ai.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.aurax.operator.data.database.AuraDao
import com.aurax.operator.data.entities.ModelEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: AuraDao
) {
    suspend fun download(model: ModelEntity, wifiOnly: Boolean = false): Result<ModelEntity> {
        return try {
            val result = performDownload(model, wifiOnly)
            Result.success(result)
        } catch (cancelled: CancellationException) {
            dao.getModel(model.id)?.let {
                dao.updateModel(it.copy(status = "AVAILABLE", downloadedBytes = File(it.localPath ?: "").length()))
            }
            throw cancelled
        } catch (error: Throwable) {
            dao.getModel(model.id)?.let { dao.updateModel(it.copy(status = "ERROR")) }
            Result.failure(error)
        }
    }

    private suspend fun performDownload(model: ModelEntity, wifiOnly: Boolean): ModelEntity {
        require(model.sourceUrl.isNotBlank()) { "Model has no download URL" }
        if (wifiOnly) require(isWifiConnected()) { "Wi-Fi-only download is enabled" }

        val dir = File(context.filesDir, "models").apply { mkdirs() }
        val safeName = model.name.substringAfterLast('/')
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "model.bin" }
            .take(180)
        val partial = File(dir, "${model.id}.part")
        val destination = File(dir, safeName)
        var start = partial.length()

        if (model.sizeBytes > 0L && start > model.sizeBytes) {
            partial.delete()
            start = 0L
        }

        if (start == model.sizeBytes && model.sizeBytes > 0L && partial.isFile) {
            finalizeDownloadedModel(model, partial, destination)
            return dao.getModel(model.id) ?: model.copy(status = "READY", localPath = destination.absolutePath)
        }

        val safetyMargin = STORAGE_SAFETY_MARGIN_BYTES
        val required = (model.sizeBytes.takeIf { it > 0L } ?: 1L) + safetyMargin
        require(availableStorageBytes() >= required) { "Not enough free storage for model" }

        if (model.sizeBytes > 0L) {
            val remaining = (model.sizeBytes - start).coerceAtLeast(0L)
            require(availableStorageBytes() >= remaining + safetyMargin) {
                "Not enough free storage for model download"
            }
        }

        update(model.copy(status = "DOWNLOADING", downloadedBytes = start))

        var connection = openConnection(model.sourceUrl, start)
        try {
            var response = connection.responseCode
            if (start > 0L && response == HttpURLConnection.HTTP_REQUESTED_RANGE_NOT_SATISFIABLE) {
                connection.disconnect()
                partial.delete()
                start = 0L
                update(model.copy(status = "DOWNLOADING", downloadedBytes = 0L))
                connection = openConnection(model.sourceUrl, 0L)
                response = connection.responseCode
            }

            require(response == HttpURLConnection.HTTP_OK || response == HttpURLConnection.HTTP_PARTIAL) {
                "Download failed with HTTP $response"
            }

            val append = start > 0L && response == HttpURLConnection.HTTP_PARTIAL
            if (append) {
                val range = parseContentRange(connection.getHeaderField("Content-Range"))
                require(range.start == start) {
                    "Server resumed at byte ${range.start} instead of requested byte $start"
                }
                val contentLength = connection.contentLengthLong
                if (contentLength >= 0L) {
                    require(range.end - range.start + 1L == contentLength) {
                        "Content-Range length does not match response length"
                    }
                }
                if (model.sizeBytes > 0L && range.total != null) {
                    require(range.total == model.sizeBytes) {
                        "Server total ${range.total} does not match expected model size ${model.sizeBytes}"
                    }
                }
            } else {
                start = 0L
                RandomAccessFile(partial, "rw").use { it.setLength(0L) }
            }

            connection.inputStream.use { input ->
                RandomAccessFile(partial, "rw").use { output ->
                    output.seek(if (append) start else 0L)
                    val buffer = ByteArray(1024 * 1024)
                    var downloaded = if (append) start else 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (count == 0) continue
                        output.write(buffer, 0, count)
                        downloaded += count
                        update(model.copy(status = "DOWNLOADING", downloadedBytes = downloaded))
                    }
                }
            }
        } finally {
            connection.disconnect()
        }

        require(partial.isFile && partial.length() > 0L) { "Downloaded file is empty" }
        if (model.sizeBytes > 0L) require(partial.length() == model.sizeBytes) {
            "Downloaded size ${partial.length()} does not match expected ${model.sizeBytes}"
        }

        finalizeDownloadedModel(model, partial, destination)
        return dao.getModel(model.id) ?: model.copy(
            name = safeName,
            localPath = destination.absolutePath,
            sizeBytes = destination.length(),
            downloadedBytes = destination.length(),
            status = "READY"
        )
    }

    private suspend fun finalizeDownloadedModel(model: ModelEntity, partial: File, destination: File) {
        require(partial.isFile && partial.length() > 0L) { "Downloaded file is empty" }
        if (model.sizeBytes > 0L) require(partial.length() == model.sizeBytes) {
            "Downloaded size ${partial.length()} does not match expected ${model.sizeBytes}"
        }
        if (model.format.equals("GGUF", true)) {
            require(ModelStatus.validateFile(partial).isValid) { "Downloaded GGUF failed format validation" }
        }
        if (model.sha256.isNotBlank()) {
            val actual = sha256(partial)
            require(actual.equals(model.sha256, ignoreCase = true)) { "SHA-256 verification failed" }
        }
        if (destination.exists()) destination.delete()
        require(partial.renameTo(destination)) { "Unable to finalize downloaded model" }
        update(model.copy(
            name = destination.name,
            localPath = destination.absolutePath,
            sizeBytes = destination.length(),
            downloadedBytes = destination.length(),
            status = "READY"
        ))
    }

    private fun openConnection(url: String, start: Long): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
            if (start > 0L) setRequestProperty("Range", "bytes=$start-")
            setRequestProperty("Accept", "application/octet-stream")
            setRequestProperty("User-Agent", "AURA-X-Operator/3.4")
        }

    private data class ContentRange(val start: Long, val end: Long, val total: Long?)

    private fun parseContentRange(value: String?): ContentRange {
        require(!value.isNullOrBlank()) { "Server returned 206 without Content-Range" }
        val match = Regex("^bytes\\s+(\\d+)-(\\d+)/(\\d+|\\*)$", RegexOption.IGNORE_CASE).matchEntire(value.trim())
            ?: error("Invalid Content-Range header")
        val start = match.groupValues[1].toLongOrNull() ?: error("Invalid Content-Range start")
        val end = match.groupValues[2].toLongOrNull() ?: error("Invalid Content-Range end")
        require(end >= start) { "Invalid Content-Range interval" }
        val total = match.groupValues[3].takeIf { it != "*" }?.toLongOrNull()
        if (total != null) require(total > end) { "Invalid Content-Range total" }
        return ContentRange(start, end, total)
    }

    private suspend fun update(model: ModelEntity) = withContext(Dispatchers.IO) {
        dao.getModel(model.id)?.let { dao.updateModel(model) }
    }

    private fun sha256(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
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

    private fun availableStorageBytes(): Long = android.os.StatFs(
        File(context.filesDir, "models").apply { mkdirs() }.absolutePath
    ).availableBytes

    private fun isWifiConnected(): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private companion object {
        const val STORAGE_SAFETY_MARGIN_BYTES = 256L * 1024L * 1024L
    }
}