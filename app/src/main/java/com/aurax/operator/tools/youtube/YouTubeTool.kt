package com.aurax.operator.tools.youtube
import android.content.Context
import android.content.Intent
import com.aurax.operator.core.common.ToolResult
import com.aurax.operator.operator.AccessibilityOperator
import com.aurax.operator.tools.registry.*
class YouTubeTool(private val context:Context,private val operator:AccessibilityOperator):AgentTool{override val id="youtube_automation";override val riskLevel=RiskLevel.LOW;override suspend fun execute(args:Map<String,String>):ToolResult{val q=args["query"]?:return ToolResult.Failure("Missing query");return search(q)};suspend fun search(query:String):ToolResult{val i=Intent(Intent.ACTION_SEARCH).apply{setPackage("com.google.android.youtube");putExtra("query",query);addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)};context.startActivity(i);return ToolResult.Success("Searching YouTube for $query")};fun togglePlayPause():ToolResult{val n=operator.findByContentDesc("Play video")?:operator.findByContentDesc("Pause video")?:return ToolResult.Failure("Play/Pause button not found");return if(operator.safeClick(n))ToolResult.Success("Toggled playback")else ToolResult.Failure("Toggle failed")}}