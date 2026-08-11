package com.aurax.operator.tools.system
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.aurax.operator.core.common.ToolResult
class SystemTool(private val context:Context){fun openAccessibilitySettings():ToolResult{context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return ToolResult.Success("Opened Accessibility settings")}}