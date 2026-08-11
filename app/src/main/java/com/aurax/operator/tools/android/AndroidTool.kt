package com.aurax.operator.tools.android
import android.content.Context
import android.content.Intent
import com.aurax.operator.core.common.ToolResult
class AndroidTool(private val context:Context){fun openPackage(pkg:String):ToolResult{val i=context.packageManager.getLaunchIntentForPackage(pkg)?:return ToolResult.Failure("App not installed");i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return ToolResult.Success("Opened $pkg")}}