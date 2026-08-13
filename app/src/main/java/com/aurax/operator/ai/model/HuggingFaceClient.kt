package com.aurax.operator.ai.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

/** Lightweight Hugging Face Hub REST client; no API key is required for public repositories. */
class HuggingFaceClient {
    suspend fun searchModels(query: String, limit: Int = 20): List<HuggingFaceModel> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        val url = "https://huggingface.co/api/models?search=$encoded&limit=${limit.coerceIn(1, 100)}&full=false"
        val json = request(url)
        val array = JSONArray(json)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                add(
                    HuggingFaceModel(
                        id = item.optString("id"),
                        author = item.optString("author"),
                        downloads = item.optLong("downloads", 0),
                        likes = item.optInt("likes", 0),
                        pipelineTag = item.optString("pipeline_tag"),
                        libraryName = item.optString("library_name"),
                        tags = item.optJSONArray("tags")?.toStringList().orEmpty()
                    )
                )
            }
        }
    }

    suspend fun listFiles(repoId: String): List<HuggingFaceFile> = withContext(Dispatchers.IO) {
        val encoded = repoId.split('/').joinToString("/") { URLEncoder.encode(it, "UTF-8") }
        val json = request("https://huggingface.co/api/models/$encoded/tree/main?recursive=true&expand=false")
        val array = JSONArray(json)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val path = item.optString("path")
                if (path.isBlank() || item.optString("type") != "file") continue
                val lfs = item.optJSONObject("lfs")
                add(
                    HuggingFaceFile(
                        path = path,
                        sizeBytes = item.optLong("size", lfs?.optLong("size", 0L) ?: 0L),
                        sha256 = lfs?.optString("sha256", "") ?: "",
                        downloadUrl = "https://huggingface.co/$repoId/resolve/main/${path.split('/').joinToString("/") { URLEncoder.encode(it, "UTF-8") }}?download=true"
                    )
                )
            }
        }
    }

    suspend fun modelInfo(repoId: String): HuggingFaceModel = withContext(Dispatchers.IO) {
        val encoded = repoId.split('/').joinToString("/") { URLEncoder.encode(it, "UTF-8") }
        val item = JSONObject(request("https://huggingface.co/api/models/$encoded"))
        HuggingFaceModel(
            id = item.optString("id"), author = item.optString("author"), downloads = item.optLong("downloads", 0),
            likes = item.optInt("likes", 0), pipelineTag = item.optString("pipeline_tag"),
            libraryName = item.optString("library_name"), tags = item.optJSONArray("tags")?.toStringList().orEmpty()
        )
    }

    private fun request(urlString: String): String {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "AURA-X-Operator/2.1")
            instanceFollowRedirects = true
        }
        try {
            val code = connection.responseCode
            require(code in 200..299) { "Hugging Face request failed: HTTP $code" }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (i in 0 until length()) add(optString(i))
    }
}

data class HuggingFaceModel(
    val id: String,
    val author: String,
    val downloads: Long,
    val likes: Int,
    val pipelineTag: String,
    val libraryName: String,
    val tags: List<String>
)

data class HuggingFaceFile(
    val path: String,
    val sizeBytes: Long,
    val sha256: String,
    val downloadUrl: String
)
