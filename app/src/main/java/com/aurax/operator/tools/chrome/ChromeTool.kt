package com.aurax.operator.tools.chrome
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.aurax.operator.core.common.ToolResult
import com.aurax.operator.operator.AccessibilityOperator
import com.aurax.operator.tools.registry.*
import kotlinx.coroutines.delay
class ChromeTool(private val context:Context,private val operator:AccessibilityOperator):AgentTool{override val id="chrome_automation";override val riskLevel=RiskLevel.MEDIUM;override suspend fun execute(args:Map<String,String>):ToolResult{val q=args["query"]?:return ToolResult.Failure("Missing query");return search(q)};suspend fun openUrl(url:String):ToolResult{if(!url.startsWith("https://"))return ToolResult.Blocked("Only HTTPS URLs are allowed");context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)).apply{setPackage("com.android.chrome");addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)});return ToolResult.Success("Opened $url")};suspend fun search(query:String):ToolResult{val opened=openUrl("https://www.google.com/search?q="+Uri.encode(query));if(opened is ToolResult.Blocked)return opened;delay(1500);val c=operator.extract()?:return ToolResult.Failure("Chrome screen unavailable");if(c.hasPasswordField||c.hasSensitiveText||c.isPrivateBrowsing)return ToolResult.Blocked("Sensitive or private Chrome screen detected; manual navigation required");return ToolResult.Success("Search opened for: $query")}}