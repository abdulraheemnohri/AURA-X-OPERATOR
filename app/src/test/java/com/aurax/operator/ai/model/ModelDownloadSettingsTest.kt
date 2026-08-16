package com.aurax.operator.ai.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelDownloadSettingsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun defaultsAreSafe() {
        val settings = ModelDownloadSettings(context)
        assertTrue(settings.automaticDownload)
        assertTrue(settings.unmeteredOnly)
        assertFalse(settings.chargingOnly)
        assertEquals(20, settings.pauseBelowBatteryPercent)
        assertEquals(1, settings.maximumParallelDownloads)
        assertEquals(0, settings.speedLimitKbps)
        assertTrue(settings.automaticRetry)
        assertEquals(3, settings.retryCount)
    }

    @Test
    fun valuesAreClampedToSafeRanges() {
        val settings = ModelDownloadSettings(context)
        settings.pauseBelowBatteryPercent = 100
        settings.maximumParallelDownloads = 50
        settings.speedLimitKbps = 999999
        settings.retryCount = 99

        assertEquals(80, settings.pauseBelowBatteryPercent)
        assertEquals(2, settings.maximumParallelDownloads)
        assertEquals(102400, settings.speedLimitKbps)
        assertEquals(10, settings.retryCount)
    }
}
