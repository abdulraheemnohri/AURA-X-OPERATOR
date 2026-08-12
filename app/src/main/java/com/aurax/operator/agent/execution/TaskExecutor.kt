package com.aurax.operator.agent.execution

import android.content.Context
import com.aurax.operator.agent.planner.OperatorPlanner
import com.aurax.operator.core.common.ToolResult
import com.aurax.operator.core.security.*
import com.aurax.operator.data.database.AuraDatabase
import com.aurax.operator.data.entities.*
import com.aurax.operator.operator.*
import com.aurax.operator.tools.android.AndroidTool
import com.aurax.operator.tools.chrome.ChromeTool
import com.aurax.operator.tools.registry.ToolRegistry
import com.aurax.operator.tools.youtube.YouTubeTool
import kotlinx.coroutines.delay

class TaskExecutor(private val context: Context) {
    private val db = AuraDatabase.get(context)
    private val planner = OperatorPlanner()
    private val policyRuntime = PolicyRuntime(context)

    suspend fun execute(input: String): String {
        val taskId = db.dao().addTask(TaskEntity(input = input, status = "RUNNING"))
        return try {
            if (policyRuntime.current() == AutomationPolicy.OBSERVE_ONLY || policyRuntime.current() == AutomationPolicy.SUGGEST_ONLY) {
                db.dao().updateTask(TaskEntity(taskId, input, "SUGGESTED", "Policy prevents automation."))
                db.dao().addSafety(SafetyEventEntity(type = "POLICY_BLOCK", reason = policyRuntime.current().name, packageName = null, action = input))
                return "I prepared the task, but the current policy prevents execution."
            }

            OperatorRuntime.ensureNotAborted()
            val service = AuraAccessibilityService.instance ?: throw IllegalStateException("AccessibilityService is not enabled")
            val operator = service.operator
            val chrome = ChromeTool(context, operator)
            val youtube = YouTubeTool(context, operator)
            val android = AndroidTool(context)
            val registry = ToolRegistry(listOf(chrome, youtube))
            val steps = planner.plan(input)
            val logs = mutableListOf<String>()

            for (step in steps) {
                OperatorRuntime.ensureNotAborted()
                val risk = AutomationPolicyEngine.classify(step.description, null, false)
                if (!policyRuntime.canExecute(risk)) {
                    db.dao().addSafety(SafetyEventEntity(type = "POLICY_BLOCK", reason = "${policyRuntime.current()} refused $risk", packageName = null, action = step.description))
                    throw SecurityException("Policy ${policyRuntime.current()} refused ${risk.name} action")
                }
                if (policyRuntime.shouldConfirm(risk)) {
                    OperatorSafety.beginConfirmation(3)
                    db.dao().addSafety(SafetyEventEntity(type = "CONFIRMATION_REQUIRED", reason = "${risk.name} action", packageName = null, action = step.description))
                    throw SecurityException("User confirmation required before: ${step.description}")
                }

                if (step.tool == "none") { logs += step.description; continue }
                if (step.tool == "android_open") {
                    val packageName = step.args.getValue("package")
                    val result = android.openPackage(packageName)
                    logs += result.message()
                    db.dao().addAction(OperatorActionEntity(taskId, packageName, step.description, step.args.toString(), true))
                    continue
                }

                val tool = registry.get(step.tool) ?: error("Unsupported tool: ${step.tool}")
                if (tool.riskLevel.name == "HIGH") throw SecurityException("High-risk automation is disabled")
                val result = tool.execute(step.args)
                when (result) {
                    is ToolResult.Success -> logs += result.message
                    is ToolResult.Failure -> throw IllegalStateException(result.message)
                    is ToolResult.Blocked -> throw SecurityException(result.reason)
                }
                db.dao().addAction(OperatorActionEntity(taskId, step.tool, step.description, step.args.toString(), true))
                db.dao().addSafety(SafetyEventEntity(type = "ACTION_ALLOWED", reason = "${risk.name} action executed", packageName = step.tool, action = step.description))
                delay(50)
            }

            val output = logs.joinToString(" ")
            db.dao().updateTask(TaskEntity(taskId, input, "COMPLETED", output))
            db.dao().addMemory(MemoryEntity(key = "last_task", value = input))
            output.ifBlank { "Task completed." }
        } catch (e: Throwable) {
            val reason = e.message ?: e.javaClass.simpleName
            db.dao().updateTask(TaskEntity(taskId, input, "FAILED", reason))
            db.dao().addSafety(SafetyEventEntity(type = if (reason.contains("abort", true)) "TASK_ABORTED" else "TASK_FAILED", reason = reason, packageName = null, action = input))
            "Task stopped safely: $reason"
        }
    }

    private fun ToolResult.message(): String = when (this) {
        is ToolResult.Success -> message
        is ToolResult.Failure -> message
        is ToolResult.Blocked -> reason
    }
}
