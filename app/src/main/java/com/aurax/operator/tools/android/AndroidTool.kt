package com.aurax.operator.tools.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.aurax.operator.core.common.ToolResult

/** Safe, explicit Android intents used by the operator planner. */
class AndroidTool(private val context: Context) {
    fun openPackage(pkg: String): ToolResult {
        if (!isPackageName(pkg)) return ToolResult.Blocked("Invalid Android package name")
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            ?: return ToolResult.Failure("App not installed")
        return start(intent, "Opened $pkg")
    }

    fun openSettings(action: String): ToolResult {
        val intentAction = when (action.uppercase()) {
            "WIFI" -> Settings.ACTION_WIFI_SETTINGS
            "BLUETOOTH" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "SOUND" -> Settings.ACTION_SOUND_SETTINGS
            "DISPLAY" -> Settings.ACTION_DISPLAY_SETTINGS
            "BATTERY" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "ACCESSIBILITY" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "APPS" -> Settings.ACTION_APPLICATION_SETTINGS
            "NOTIFICATIONS" -> if (android.os.Build.VERSION.SDK_INT >= 26) Settings.ACTION_APP_NOTIFICATION_SETTINGS else Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            "PRIVACY" -> if (android.os.Build.VERSION.SDK_INT >= 29) Settings.ACTION_PRIVACY_SETTINGS else Settings.ACTION_SECURITY_SETTINGS
            else -> return ToolResult.Failure("Unsupported Android settings target")
        }
        val intent = Intent(intentAction).apply {
            if (intentAction == Settings.ACTION_APP_NOTIFICATION_SETTINGS) {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        }
        return start(intent, "Opened Android $action settings")
    }

    fun openUrl(url: String): ToolResult {
        val normalized = url.trim()
        val uri = runCatching { Uri.parse(normalized) }.getOrNull()
            ?: return ToolResult.Failure("Invalid URL")
        if (uri.scheme !in setOf("https", "http")) {
            return ToolResult.Blocked("Only HTTP(S) URLs are allowed")
        }
        if (uri.host.isNullOrBlank()) return ToolResult.Failure("URL host is missing")
        return start(Intent(Intent.ACTION_VIEW, uri), "Opened $normalized")
    }

    private fun start(intent: Intent, message: String): ToolResult {
        return runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult.Success(message)
        }.getOrElse { ToolResult.Failure(it.message ?: "Unable to start Android activity") }
    }

    private fun isPackageName(value: String): Boolean =
        value.length in 3..255 && value.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))
}
