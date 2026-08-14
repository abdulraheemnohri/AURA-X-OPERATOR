package com.aurax.operator.memory

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import kotlin.math.sqrt

/** File-backed local RAG index. No cloud service or vector database dependency required. */
class KnowledgeBaseManager(context: Context) {
    data class Chunk(
        val id: String,
        val source: String,
        val text: String,
        val vector: FloatArray
    )

    data class Match(val chunk: Chunk, val score: Float)

    private val indexFile = File(context.filesDir, "knowledge/rag_index.jsonl")

    init { indexFile.parentFile?.mkdirs() }

    fun ingestText(source: String, text: String, chunkSize: Int = 500): Int {
        if (text.isBlank()) return 0
        val normalized = text.replace("\r\n", "\n").trim()
        val chunks = normalized.chunked(chunkSize.coerceIn(200, 2000))
        chunks.forEachIndexed { index, chunk ->
            val obj = JSONObject()
                .put("id", sha256("$source#$index#$chunk"))
                .put("source", source)
                .put("text", chunk)
                .put("vector", vector(chunk).joinToString(","))
            indexFile.appendText(obj.toString() + "\n")
        }
        return chunks.size
    }

    fun ingestUri(context: Context, uri: Uri): Int {
        val name = uri.lastPathSegment ?: uri.toString()
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: return 0
        return ingestText(name, text)
    }

    fun search(query: String, topK: Int = 5): List<Match> {
        if (!indexFile.exists()) return emptyList()
        val q = vector(query)
        return indexFile.readLines()
            .asSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                runCatching {
                    val obj = JSONObject(line)
                    val values = obj.getString("vector").split(',').mapNotNull { it.toFloatOrNull() }.toFloatArray()
                    Chunk(obj.getString("id"), obj.getString("source"), obj.getString("text"), values)
                }.getOrNull()
            }
            .map { Match(it, cosine(q, it.vector)) }
            .sortedByDescending { it.score }
            .take(topK.coerceIn(1, 20))
            .toList()
    }

    fun clear() {
        if (indexFile.exists()) indexFile.delete()
    }

    fun chunkCount(): Int = if (!indexFile.exists()) 0 else indexFile.useLines { it.count { line -> line.isNotBlank() } }

    private fun vector(text: String): FloatArray {
        val result = FloatArray(96)
        text.lowercase().split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 2 }
            .forEach { token ->
                var h = 17
                token.forEach { h = h * 31 + it.code }
                result[(h and Int.MAX_VALUE) % result.size] += 1f
            }
        return result
    }

    private fun cosine(a: FloatArray, b: FloatArray): Float {
        val size = minOf(a.size, b.size)
        var dot = 0f; var aa = 0f; var bb = 0f
        for (i in 0 until size) { dot += a[i] * b[i]; aa += a[i] * a[i]; bb += b[i] * b[i] }
        return if (aa == 0f || bb == 0f) 0f else dot / (sqrt(aa) * sqrt(bb))
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
}
