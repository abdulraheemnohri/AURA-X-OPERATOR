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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: AuraDao
) {
    suspend fun download(model: ModelEntity, wifiOnly: Boolean = false): Result<ModelEntity> = runCatching {
        require(model.sourceUrl.isNotBlank()) { "Model has no download URL" }
        if (wifiOnly) require(isWifiConnected()) { "Wi-Fi-only download is enabled" }
        require(model.sizeBytes <= 0L || model.sizeBytes <= availableStorageBytes()) { "Not enough free storage for model" }

        val dir = File(context.filesDir, "models").apply { mkdirs() }
        val safeName = model.name.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "model.bin" }.take(180)
        val partial = File(dir, "${model.id}.part")
        val destination = File(dir, safeName)
        var start = partial.length()

        update(model.copy(status = "DOWNLOADING", downloadedBytes = start))

        var connection = openConnection(model.sourceUrl, start)
        try {
            var response = connection.responseCode
            // Some CDNs reject Range after an interrupted transfer. Restart cleanly instead of corrupting the file.
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
            if (!append) {
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

        require(partial.length() > 0L) { "Downloaded file is empty" }
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
        val ready = model.copy(
            name = safeName,
            localPath = destination.absolutePath,
            sizeBytes = destination.length(),
            downloadedBytes = destination.length(),
            status = "READY"
        )
        update(ready)
        ready
    }.onFailure {
        dao.getModel(model.id)?.let { dao.updateModel(it.copy(status = "ERROR")) }
    }

    private fun openConnection(url: String, start: Long): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
            if (start > 0L) setRequestProperty("Range", "bytes=$start-")
            setRequestProperty("Accept", "application/octet-stream")
            setRequestProperty("User-Agent", "AURA-X-Operator/3.3")
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

    private fun availableStorageBytes(): Long = File(context.filesDir, "models").let { StatFsCompat.availableBytes(it) }

    private fun isWifiConnected(): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private object StatFsCompat {
        fun availableBytes(file: File): Long = android.os.StatFs(file.absolutePath).availableBytes
    }
}
