package com.aurax.operator.ai.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

/** Lightweight Hugging Face Hub client using the public REST API; no extra dependency required. */
class HuggingFaceModelClient {
    suspend fun searchModels(query: String, limit: Int = 20): List<RemoteModel> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        val url = "https://huggingface.co/api/models?search=$encoded&limit=${limit.coerceIn(1, 100)}&full=false"
        val root = JSONArray(get(url))
        buildList {
            for (i in 0 until root.length()) {
                val item = root.getJSONObject(i)
                add(
                    RemoteModel(
                        repoId = item.optString("id"),
                        author = item.optString("author"),
                        downloads = item.optLong("downloads", 0L),
                        likes = item.optInt("likes", 0),
                        pipelineTag = item.optString("pipeline_tag"),
                        library = item.optString("library_name"),
                        lastModified = item.optString("lastModified")
                    )
                )
            }
        }
    }

    suspend fun files(repoId: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        val encoded = repoId.split('/').joinToString("/") { URLEncoder.encode(it, "UTF-8") }
        val root = JSONArray(get("https://huggingface.co/api/models/$encoded/tree/main?recursive=true&expand=false"))
        buildList {
            for (i in 0 until root.length()) {
                val item = root.getJSONObject(i)
                val path = item.optString("path")
                val type = item.optString("type")
                if (type == "file" && path.isNotBlank()) {
                    add(RemoteFile(repoId, path, item.optLong("size", 0L)))
                }
            }
        }
    }

    fun resolve(repoId: String, path: String): String =
        "https://huggingface.co/$repoId/resolve/main/${path.split('/').joinToString("/") { URLEncoder.encode(it, "UTF-8") }}?download=true"

    private fun get(urlString: String): String {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "AURA-X-Operator/1.0")
        }
        try {
            val code = connection.responseCode
            require(code in 200..299) { "Hugging Face API returned HTTP $code" }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}

data class RemoteModel(
    val repoId: String,
    val author: String,
    val downloads: Long,
    val likes: Int,
    val pipelineTag: String,
    val library: String,
    val lastModified: String
)

data class RemoteFile(val repoId: String, val path: String, val sizeBytes: Long)
