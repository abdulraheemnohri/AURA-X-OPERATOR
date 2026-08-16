package com.aurax.operator.memory

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import kotlin.math.ln

/**
 * File-backed local RAG index.
 *
 * Semantic embeddings are intentionally not fabricated here. Until a real
 * EmbeddingRuntime is installed, retrieval uses a transparent lexical scorer.
 * Existing indexes containing the legacy pseudo-vector field remain readable;
 * that field is ignored and will be replaced on re-index.
 */
class KnowledgeBaseManager(context: Context) {
    data class Chunk(
        val id: String,
        val source: String,
        val text: String
    )

    data class Match(
        val chunk: Chunk,
        val score: Float,
        val lexicalScore: Float = score,
        val semanticScore: Float = 0f
    )

    private val indexFile = File(context.filesDir, "knowledge/rag_index.jsonl")

    init { indexFile.parentFile?.mkdirs() }

    fun ingestText(source: String, text: String, chunkSize: Int = 500): Int {
        if (text.isBlank()) return 0
        val normalized = text.replace("\r\n", "\n").trim()
        val chunks = normalized.chunked(chunkSize.coerceIn(200, 2000))
        chunks.forEachIndexed { index, chunk ->
            val obj = JSONObject()
                .put("version", 2)
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
        return ingestText(name, text)
    }

    /**
     * Deterministic lexical retrieval used when a real embedding runtime is unavailable.
     * Scores combine term coverage, term frequency and a small phrase bonus.
     */
    fun search(query: String, topK: Int = 5): List<Match> {
        if (!indexFile.exists() || query.isBlank()) return emptyList()
        val queryTerms = tokenize(query).toSet()
        if (queryTerms.isEmpty()) return emptyList()

        val chunks = indexFile.readLines()
            .asSequence()
            .filter(String::isNotBlank)
            .mapNotNull(::parseChunk)
            .toList()

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
            Match(chunk = chunk, score = finalScore, lexicalScore = finalScore)
        }
            .filter { it.score > 0f }
            .sortedByDescending { it.score }
            .take(topK.coerceIn(1, 20))
    }

    fun clear() {
        if (indexFile.exists()) indexFile.delete()
    }

    fun chunkCount(): Int = if (!indexFile.exists()) 0 else indexFile.useLines { lines ->
        lines.count { it.isNotBlank() && parseChunk(it) != null }
    }

    private fun parseChunk(line: String): Chunk? = runCatching {
        val obj = JSONObject(line)
        Chunk(
            id = obj.getString("id"),
            source = obj.getString("source"),
            text = obj.getString("text")
        )
    }.getOrNull()

    private fun tokenize(text: String): List<String> = text.lowercase()
        .split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.length >= 2 }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
}
