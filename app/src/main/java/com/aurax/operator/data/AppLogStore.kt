package com.aurax.operator.data

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * App-owned diagnostic recorder. It intentionally records only AURA-X events and
 * uncaught failures; Android does not grant ordinary apps unrestricted access to
 * the device-wide logcat stream.
 */
object AppLogStore {
    private const val FILE_NAME = "aura-x-app.log"
    private const val MAX_BYTES = 8L * 1024L * 1024L
    private const val RETAIN_BYTES = 6L * 1024L * 1024L
    private val lock = Any()
    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)

    private fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    fun record(context: Context, level: String = "INFO", tag: String, message: String, throwable: Throwable? = null) {
        val safe = message.replace('\n', ' ').trim()
        val line = buildString {
            append(formatter.format(Date()))
            append(" [").append(level.uppercase(Locale.US)).append("] ")
            append(tag.replace('\n', '_')).append(": ").append(safe)
            if (throwable != null) {
                append(" | ").append(throwable::class.java.name)
                append(": ").append(throwable.message.orEmpty().replace('\n', ' '))
            }
            append('\n')
        }
        synchronized(lock) {
            val target = file(context)
            target.parentFile?.mkdirs()
            target.appendText(line, Charsets.UTF_8)
            trimIfNeeded(target)
        }
    }

    fun recordException(context: Context, tag: String, throwable: Throwable) =
        record(context, "ERROR", tag, throwable.message ?: "Unhandled exception", throwable)

    fun read(context: Context): String = synchronized(lock) {
        file(context).takeIf { it.exists() }?.readText(Charsets.UTF_8).orEmpty()
    }

    fun sizeBytes(context: Context): Long = synchronized(lock) { file(context).takeIf { it.exists() }?.length() ?: 0L }

    fun clear(context: Context) = synchronized(lock) { file(context).delete() }

    fun exportText(context: Context): String = read(context)

    fun exportJson(context: Context): String {
        val entries = read(context).lineSequence().filter { it.isNotBlank() }.map { line ->
            "\"${line.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n")}\""
        }.toList()
        return "[${entries.joinToString(",")}]\n"
    }

    fun exportCsv(context: Context): String {
        val rows = read(context).lineSequence().filter { it.isNotBlank() }
        return buildString {
            appendLine("log_line")
            rows.forEach { line ->
                append('"').append(line.replace("\"", "\"\"")).append('"').append('\n')
            }
        }
    }

    private fun trimIfNeeded(target: File) {
        if (target.length() <= MAX_BYTES) return
        val bytes = target.readBytes()
        val start = (bytes.size - RETAIN_BYTES.toInt()).coerceAtLeast(0)
        val kept = bytes.copyOfRange(start, bytes.size).toString(Charsets.UTF_8)
        target.writeText("[log trimmed at ${formatter.format(Date())}]\n$kept", Charsets.UTF_8)
    }
}
