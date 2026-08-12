package com.aurax.operator.agent.execution

import android.content.Context
import com.aurax.operator.agent.planner.OperatorPlanner
import com.aurax.operator.core.common.ToolResult
import com.aurax.operator.core.security.SecurePrefs
import com.aurax.operator.data.database.AuraDatabase
import com.aurax.operator.data.entities.*
import com.aurax.operator.operator.AuraAccessibilityService
import com.aurax.operator.operator.OperatorRuntime
import com.aurax.operator.tools.android.AndroidTool
import com.aurax.operator.tools.chrome.ChromeTool
import com.aurax.operator.tools.registry.ToolRegistry
import com.aurax.operator.tools.youtube.YouTubeTool

class TaskExecutor(private val context: Context) {
    private val db = AuraDatabase.get(context)
    private val prefs = SecurePrefs(context)
    private val planner = OperatorPlanner()

    suspend fun execute(input: String): String {
        val taskId = db.dao().addTask(TaskEntity(input = input, status = "RUNNING"))
        return try {
            val policy = prefs.policy
            if (policy == "OBSERVE_ONLY" || policy == "SUGGEST_ONLY") {
                db.dao().updateTask(TaskEntity(taskId, input, "SUGGESTED", "Policy $policy: no automation executed."))
                db.dao().addSafety(SafetyEventEntity(type = "POLICY_BLOCK", reason = policy, packageName = null, action = input))
                return "I prepared the task, but policy $policy prevents execution."
            }

            OperatorRuntime.ensureNotAborted()
            val service = AuraAccessibilityService.instance
                ?: throw IllegalStateException("AccessibilityService is not enabled")
            val operator = service.operator
            val chrome = ChromeTool(context, operator)
            val youtube = YouTubeTool(context, operator)
            val android = AndroidTool(context)
            val registry = ToolRegistry(listOf(chrome, youtube))
            val steps = planner.plan(input)
            val logs = mutableListOf<String>()

            for (step in steps) {
                OperatorRuntime.ensureNotAborted()
                if (step.tool == "none") {
                    logs += step.description
                    continue
                }

                if (step.tool == "android_open") {
                    val result = android.openPackage(step.args.getValue("package"))
                    logs += result.message()
                    continue
                }

                val tool = registry.get(step.tool) ?: error("Unsupported tool: ${step.tool}")
                if (tool.riskLevel.name == "HIGH") {
                    db.dao().addSafety(SafetyEventEntity(type = "HIGH_RISK", reason = "High-risk tool refused", packageName = null, action = step.description))
                    throw SecurityException("High-risk automation is disabled")
                }
                val result = tool.execute(step.args)
                when (result) {
                    is ToolResult.Success -> logs += result.message
                    is ToolResult.Failure -> throw IllegalStateException(result.message)
                    is ToolResult.Blocked -> {
                        db.dao().addSafety(SafetyEventEntity(type = "BLOCKED", reason = result.reason, packageName = null, action = step.description))
                        throw SecurityException(result.reason)
                    }
                }
                db.dao().addAction(
                    OperatorActionEntity(
                        taskId = taskId,
                        packageName = step.tool,
                        action = step.description,
                        target = step.args.toString(),
                        allowed = true
                    )
                )
            }

            val output = logs.joinToString(" ")
            db.dao().updateTask(TaskEntity(taskId, input, "COMPLETED", output))
            db.dao().addMemory(MemoryEntity(key = "last_task", value = input))
            output.ifBlank { "Task completed." }
        } catch (e: Throwable) {
            val reason = e.message ?: e.javaClass.simpleName
            db.dao().updateTask(TaskEntity(taskId, input, "FAILED", reason))
            db.dao().addSafety(SafetyEventEntity(type = "TASK_FAILED", reason = reason, packageName = null, action = input))
            "Task stopped safely: $reason"
        }
    }

    private fun ToolResult.message(): String = when (this) {
        is ToolResult.Success -> message
        is ToolResult.Failure -> message
        is ToolResult.Blocked -> reason
    }
}
