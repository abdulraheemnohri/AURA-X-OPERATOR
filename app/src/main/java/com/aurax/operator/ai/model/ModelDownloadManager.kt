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

        val dir = File(context.filesDir, "models").apply { mkdirs() }
        val partial = File(dir, "${model.id}.part")
        val destination = File(dir, model.name.substringAfterLast('/'))
        val start = partial.length()

        update(model.copy(status = "DOWNLOADING", downloadedBytes = start))

        val connection = (URL(model.sourceUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            if (start > 0) setRequestProperty("Range", "bytes=$start-")
            instanceFollowRedirects = true
        }

        try {
            val response = connection.responseCode
            require(response == HttpURLConnection.HTTP_OK || response == HttpURLConnection.HTTP_PARTIAL) {
                "Download failed with HTTP $response"
            }
            val append = start > 0 && response == HttpURLConnection.HTTP_PARTIAL
            if (!append) partial.outputStream().use { it.flush() }

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
        if (model.sha256.isNotBlank()) {
            val actual = sha256(partial)
            require(actual.equals(model.sha256, ignoreCase = true)) { "SHA-256 verification failed" }
        }

        if (destination.exists()) destination.delete()
        require(partial.renameTo(destination)) { "Unable to finalize downloaded model" }
        val ready = model.copy(
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

    private fun isWifiConnected(): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}
