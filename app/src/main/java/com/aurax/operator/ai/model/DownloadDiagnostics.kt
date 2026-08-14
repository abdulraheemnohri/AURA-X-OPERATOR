package com.aurax.operator.ai.model

/** User-facing download failure classification for logs and diagnostics. */
data class DownloadDiagnostics(
    val code: String,
    val message: String
) {
    companion object {
        fun from(error: Throwable): DownloadDiagnostics {
            val message = error.message?.takeIf { it.isNotBlank() } ?: "Download failed"
            val code = when {
                message.contains("HTTP 401") || message.contains("HTTP 403") -> "AUTH_REQUIRED"
                message.contains("HTTP 404") -> "NOT_FOUND"
                message.contains("HTTP 416") -> "RANGE_INVALID"
                message.contains("HTTP 429") -> "RATE_LIMITED"
                message.contains("HTTP 5") -> "SERVER_ERROR"
                message.contains("Not enough free storage", true) -> "STORAGE"
                message.contains("Wi-Fi-only", true) -> "NETWORK_POLICY"
                message.contains("SHA-256", true) -> "INTEGRITY"
                message.contains("GGUF", true) -> "FORMAT"
                message.contains("Content-Range", true) -> "RANGE_INVALID"
                else -> "NETWORK_OR_IO"
            }
            return DownloadDiagnostics(code, message)
        }
    }
}
