package com.aurax.operator.memory

import android.content.Context
import android.net.Uri
import com.aurax.operator.ai.runtime.EmbeddingRuntime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * File-backed local RAG index.
 *
 * Semantic embeddings are optional and are used only when a real
 * EmbeddingRuntime is operational. The lexical path remains deterministic and
 * offline when no embedding runtime/model is available.
 */
class KnowledgeBaseManager(context: Context) {
    data class Chunk(
        val id: String,
        val source: String,
        val text: String,
        val embedding: FloatArray? = null,
        val embeddingModel: String? = null
    )

    data class Match(
        val chunk: Chunk,
        val score: Float,
        val lexicalScore: Float = score,
        val semanticScore: Float = 0f
    )

    private val indexFile = File(context.filesDir, "knowledge/rag_index.jsonl")

    init { indexFile.parentFile?.mkdirs() }

    suspend fun ingestText(
        source: String,
        text: String,
        chunkSize: Int = 500,
        embeddingRuntime: EmbeddingRuntime? = null,
        embeddingModel: String? = null
    ): Int = withContext(Dispatchers.Default) {
        if (text.isBlank()) return@withContext 0
        val normalized = text.replace("\r\n", "\n").trim()
        val chunks = normalized.chunked(chunkSize.coerceIn(200, 2000))
        val runtimeReady = embeddingRuntime?.isOperational() == true

        chunks.forEachIndexed { index, chunk ->
            val vector = if (runtimeReady) {
                runCatching { embeddingRuntime!!.embed(chunk) }
                    .getOrNull()
                    ?.takeIf { it.isNotEmpty() && it.all(Float::isFinite) }
            } else null

            val obj = JSONObject()
                .put("version", 3)
                .put("id", sha256("$source#$index#$chunk"))
                .put("source", source)
                .put("text", chunk)

            if (vector != null) {
                obj.put("embedding", JSONArray(vector.map { it.toDouble() }))
                if (!embeddingModel.isNullOrBlank()) obj.put("embeddingModel", embeddingModel)
            }
            indexFile.appendText(obj.toString() + "\n")
        }
        chunks.size
    }

    fun ingestTextLegacy(source: String, text: String, chunkSize: Int = 500): Int {
        if (text.isBlank()) return 0
        val normalized = text.replace("\r\n", "\n").trim()
        val chunks = normalized.chunked(chunkSize.coerceIn(200, 2000))
        chunks.forEachIndexed { index, chunk ->
            val obj = JSONObject()
                .put("version", 3)
                .put("id", sha256("$source#$index#$chunk"))
                .put("source", source)
                .put("text", chunk)
            indexFile.appendText(obj.toString() + "\n")
        }
        return chunks.size
    }

    fun ingestUri(context: Context, uri: Uri): Int {
        val name = uri.lastPathSegment ?: uri.toString()
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: return 0
        return ingestTextLegacy(name, text)
    }

    fun search(query: String, topK: Int = 5): List<Match> {
        if (!indexFile.exists() || query.isBlank()) return emptyList()
        val queryTerms = tokenize(query).toSet()
        if (queryTerms.isEmpty()) return emptyList()
        val chunks = readChunks()
        if (chunks.isEmpty()) return emptyList()

        val documentFrequency = queryTerms.associateWith { term ->
            chunks.count { tokenize(it.text).contains(term) }
        }
        val documentCount = chunks.size.coerceAtLeast(1)

        return chunks.map { chunk ->
            val terms = tokenize(chunk.text)
            val termCounts = terms.groupingBy { it }.eachCount()
            val score = queryTerms.sumOf { term ->
                val tf = termCounts[term] ?: 0
                if (tf == 0) 0.0 else {
                    val idf = ln((documentCount + 1.0) / (documentFrequency[term]!! + 1.0)) + 1.0
                    val normalizedTf = tf.toDouble() / (tf + 1.0)
                    idf * normalizedTf
                }
            }.toFloat() / queryTerms.size
            val phraseBonus = if (chunk.text.contains(query.trim(), ignoreCase = true)) 0.15f else 0f
            val finalScore = (score + phraseBonus).coerceIn(0f, 1f)
            Match(chunk, finalScore, finalScore, 0f)
        }.filter { it.score > 0f }
            .sortedByDescending { it.score }
            .take(topK.coerceIn(1, 20))
    }

    suspend fun searchHybrid(
        query: String,
        embeddingRuntime: EmbeddingRuntime?,
        topK: Int = 5
    ): List<Match> = withContext(Dispatchers.Default) {
        if (!embeddingRuntime?.isOperational().orFalse()) return@withContext search(query, topK)
        val queryVector = runCatching { embeddingRuntime!!.embed(query) }.getOrNull()
            ?.takeIf { it.isNotEmpty() && it.all(Float::isFinite) }
            ?: return@withContext search(query, topK)

        val lexical = search(query, 20).associateBy { it.chunk.id }
        val all = readChunks()
        val semantic = mutableMapOf<String, Float>()
        all.forEach { chunk ->
            val vector = chunk.embedding ?: return@forEach
            if (vector.size != queryVector.size) return@forEach
            val cosine = cosine(queryVector, vector)
            if (cosine > 0f) semantic[chunk.id] = cosine
        }

        if (semantic.isEmpty()) return@withContext lexical.values.take(topK)

        all.map { chunk ->
            val lexicalScore = lexical[chunk.id]?.lexicalScore ?: 0f
            val semanticScore = semantic[chunk.id] ?: 0f
            val score = (lexicalScore * 0.35f + semanticScore * 0.65f).coerceIn(0f, 1f)
            Match(chunk, score, lexicalScore, semanticScore)
        }.filter { it.score > 0f }
            .sortedWith(compareByDescending<Match> { it.score }.thenByDescending { it.semanticScore })
            .take(topK.coerceIn(1, 20))
    }

    fun clear() { if (indexFile.exists()) indexFile.delete() }

    fun chunkCount(): Int = readChunks().size

    private fun readChunks(): List<Chunk> = if (!indexFile.exists()) emptyList() else indexFile.useLines { lines ->
        lines.filter(String::isNotBlank).mapNotNull(::parseChunk).toList()
    }

    private fun parseChunk(line: String): Chunk? = runCatching {
        val obj = JSONObject(line)
        val embedding = obj.optJSONArray("embedding")?.let { array ->
            FloatArray(array.length()) { index -> array.optDouble(index).toFloat() }
        }
        Chunk(
            id = obj.getString("id"),
            source = obj.getString("source"),
            text = obj.getString("text"),
            embedding = embedding,
            embeddingModel = obj.optString("embeddingModel").takeIf { it.isNotBlank() }
        )
    }.getOrNull()

    private fun tokenize(text: String): List<String> = text.lowercase()
        .split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.length >= 2 }

    private fun cosine(a: FloatArray, b: FloatArray): Float {
        var dot = 0.0
        var aa = 0.0
        var bb = 0.0
        for (i in a.indices) {
            dot += a[i] * b[i]
            aa += a[i] * a[i]
            bb += b[i] * b[i]
        }
        val denominator = sqrt(aa * bb)
        return if (denominator <= 0.0) 0f else (dot / denominator).toFloat().coerceIn(-1f, 1f)
    }

    private fun Boolean?.orFalse(): Boolean = this == true

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
}
