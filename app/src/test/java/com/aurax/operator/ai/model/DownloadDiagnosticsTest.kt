package com.aurax.operator.ai.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadDiagnosticsTest {
    @Test fun classifiesHttp416() {
        assertEquals("RANGE_INVALID", DownloadDiagnostics.from(IllegalStateException("Download failed with HTTP 416")).code)
    }

    @Test fun classifiesIntegrityFailure() {
        assertEquals("INTEGRITY", DownloadDiagnostics.from(IllegalStateException("SHA-256 verification failed")).code)
    }

    @Test fun classifiesStorageFailure() {
        assertEquals("STORAGE", DownloadDiagnostics.from(IllegalArgumentException("Not enough free storage for model")).code)
    }
}
