package com.aurax.operator.operator

import android.graphics.Bitmap
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class ScreenshotCapture(private val service: AuraAccessibilityService) {
    private val executor = Executors.newSingleThreadExecutor()

    fun capture(): Result<File> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return Result.failure(UnsupportedOperationException("Accessibility screenshots require Android 11+"))
        }
        val context = service.operator.extract()
            ?: return Result.failure(IllegalStateException("No active screen"))
        if (context.hasPasswordField || context.hasSensitiveText || context.isPrivateBrowsing) {
            OperatorAudit.safety("BLOCKED_SCREENSHOT", "Sensitive/private screen", context.packageName, "Screenshot")
            return Result.failure(SecurityException("Screenshot blocked on sensitive/private screen"))
        }

        var result: Result<File> = Result.failure(IllegalStateException("Screenshot callback not completed"))
        val lock = Object()
        service.takeScreenshot(
            android.view.Display.DEFAULT_DISPLAY,
            executor,
            object : android.accessibilityservice.AccessibilityService.TakeScreenshotCallback {
                override fun onSuccess(screenshot: android.accessibilityservice.AccessibilityService.ScreenshotResult) {
                    val buffer = screenshot.hardwareBuffer
                    val bitmap = Bitmap.wrapHardwareBuffer(buffer, screenshot.colorSpace)
                    buffer.close()
                    result = if (bitmap == null) {
                        Result.failure(IllegalStateException("Unable to decode screenshot"))
                    } else {
                        val file = File(service.filesDir, "screenshots/screen-${System.currentTimeMillis()}.png").apply { parentFile?.mkdirs() }
                        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
                        bitmap.recycle()
                        OperatorAudit.action(context.packageName, "Screenshot", file.name, true)
                        Result.success(file)
                    }
                    synchronized(lock) { lock.notifyAll() }
                }

                override fun onFailure(errorCode: Int) {
                    result = Result.failure(IllegalStateException("Screenshot failed: $errorCode"))
                    synchronized(lock) { lock.notifyAll() }
                }
            }
        )
        synchronized(lock) {
            if (result.isFailure) lock.wait(5_000)
        }
        return result
    }
}
