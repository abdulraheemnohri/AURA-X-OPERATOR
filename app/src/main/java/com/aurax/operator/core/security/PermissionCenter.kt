package com.aurax.operator.core.security

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.aurax.operator.operator.AuraAccessibilityService
import com.aurax.operator.operator.AuraNotificationListenerService

object PermissionCenter {
    fun hasMicrophone(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun hasNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun hasOverlay(context: Context): Boolean =
        Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context)

    fun isAccessibilityEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        val expected = ComponentName(context, AuraAccessibilityService::class.java).flattenToString()
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    fun isNotificationAccessEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
        val expected = ComponentName(context, AuraNotificationListenerService::class.java).flattenToString()
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    fun accessibilitySettingsIntent(): Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    fun notificationAccessSettingsIntent(): Intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")

    fun overlaySettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
}
