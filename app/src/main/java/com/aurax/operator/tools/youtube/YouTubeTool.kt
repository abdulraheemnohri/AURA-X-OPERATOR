package com.aurax.operator.tools.youtube

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.aurax.operator.core.common.ToolResult
import com.aurax.operator.operator.AccessibilityGuardrails
import com.aurax.operator.operator.AccessibilityOperator
import com.aurax.operator.tools.registry.AgentTool
import com.aurax.operator.tools.registry.RiskLevel
import kotlinx.coroutines.delay

class YouTubeTool(
    private val context: Context,
    private val operator: AccessibilityOperator
) : AgentTool {
    override val id = "youtube_automation"
    override val riskLevel = RiskLevel.LOW

    override suspend fun execute(args: Map<String, String>): ToolResult {
        return when (args["action"]?.lowercase()) {
            "play" -> playFirstResult()
            "pause", "toggle" -> togglePlayPause()
            "volume_up" -> changeVolume(AudioManager.ADJUST_RAISE)
            "volume_down" -> changeVolume(AudioManager.ADJUST_LOWER)
            else -> search(args["query"] ?: return ToolResult.Failure("Missing query"))
        }
    }

    suspend fun search(query: String): ToolResult {
        val i = Intent(Intent.ACTION_SEARCH).apply {
            setPackage("com.google.android.youtube")
            putExtra("query", query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(i)
        delay(1_200)
        val screen = operator.extract() ?: return ToolResult.Failure("YouTube screen unavailable")
        if (screen.hasPasswordField || screen.hasSensitiveText || screen.isPrivateBrowsing) {
            return ToolResult.Blocked("Sensitive/private screen detected")
        }
        return ToolResult.Success("Searching YouTube for $query")
    }

    suspend fun playFirstResult(): ToolResult {
        val screen = operator.extract() ?: return ToolResult.Failure("YouTube screen unavailable")
        if (screen.hasSensitiveText || screen.isPrivateBrowsing) return ToolResult.Blocked("Sensitive/private screen detected")
        val candidates = screen.clickableElements.filter { element ->
            val label = listOfNotNull(element.text, element.contentDesc).joinToString(" ")
            label.isNotBlank() && !AccessibilityGuardrails.isBlockedYouTubeAction(label) &&
                !label.contains("sponsored", true) && !label.contains("advertisement", true)
        }
        val first = candidates.firstOrNull() ?: return ToolResult.Failure("No safe video result found")
        val node = operator.findByText(first.text ?: first.contentDesc ?: "")
            ?: return ToolResult.Failure("Video node disappeared")
        return if (operator.safeClick(node, "Play first YouTube result")) {
            ToolResult.Success("Playing first safe video result")
        } else ToolResult.Failure("Could not play video")
    }

    suspend fun togglePlayPause(): ToolResult {
        val n = operator.findByContentDesc("Play video")
            ?: operator.findByContentDesc("Pause video")
            ?: return ToolResult.Failure("Play/Pause button not found")
        return if (operator.safeClick(n, "Toggle YouTube playback")) {
            ToolResult.Success("Toggled playback")
        } else ToolResult.Failure("Toggle failed")
    }

    fun changeVolume(direction: Int): ToolResult {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0)
        return ToolResult.Success("Adjusted media volume")
    }
}
