package com.aurax.operator.agent.execution

import android.content.Context
import com.aurax.operator.agent.planner.OperatorPlanner
import com.aurax.operator.core.app.AppState
import com.aurax.operator.core.app.OperatorPhase
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
import kotlinx.coroutines.withTimeoutOrNull

class TaskExecutor(private val context: Context) {
    private val db = AuraDatabase.get(context)
    private val planner = OperatorPlanner()
    private val policyRuntime = PolicyRuntime(context)
    private val pendingActions = PendingActionStore()
    private val confirmation = ConfirmationCoordinator(pendingActions)

    suspend fun confirmPending(): Boolean = confirmation.confirm() != null
    suspend fun abortPending(): Boolean = confirmation.abort() != null

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
            val registry = ToolRegistry(listOf(ChromeTool(context, operator), YouTubeTool(context, operator)))
            val android = AndroidTool(context)
            val steps = planner.plan(input)
            val logs = mutableListOf<String>()

            for ((index, step) in steps.withIndex()) {
                OperatorRuntime.ensureNotAborted()
                AppState.setStep(step.description, if (steps.isEmpty()) 1f else index.toFloat() / steps.size)
                val risk = AutomationPolicyEngine.classify(step.description, null, false)
                if (!policyRuntime.canExecute(risk)) {
                    audit("POLICY_BLOCK", risk.name, step.description)
                    throw SecurityException("Policy ${policyRuntime.current()} refused ${risk.name} action")
                }

                if (policyRuntime.shouldConfirm(risk)) {
                    AppState.setPhase(OperatorPhase.CONFIRMING, "Confirm: ${step.description}")
                    audit("CONFIRMATION_REQUIRED", risk.name, step.description)
                    pendingActions.set(PendingActionStore.PendingAction(taskId, step.description, step.tool))
                    OperatorSafety.beginConfirmation(3)
                    val approved = withTimeoutOrNull(3_000L) {
                        while (AppState.operator.value.phase == OperatorPhase.CONFIRMING && !AppState.operator.value.abortRequested) delay(100)
                        AppState.operator.value.phase == OperatorPhase.EXECUTING
                    } ?: false
                    if (!approved) {
                        pendingActions.clear()
                        audit("ACTION_ABORTED", "User did not confirm", step.description)
                        throw SecurityException("Action not confirmed")
                    }
                }

                if (step.tool == "none") {
                    logs += step.description
                    continue
                }
                if (step.tool == "android_open") {
                    val packageName = step.args.getValue("package")
                    val result = android.openPackage(packageName)
                    logs += result.message()
                    db.dao().addAction(
                        OperatorActionEntity(
                            taskId = taskId,
                            packageName = packageName,
                            action = step.description,
                            target = step.args.toString(),
                            allowed = true
                        )
                    )
                    audit("ACTION_ALLOWED", risk.name, step.description)
                    continue
                }

                val tool = registry.get(step.tool) ?: error("Unsupported tool: ${step.tool}")
                if (tool.riskLevel.name == "HIGH") throw SecurityException("High-risk automation is disabled")
                when (val result = tool.execute(step.args)) {
                    is ToolResult.Success -> logs += result.message
                    is ToolResult.Failure -> throw IllegalStateException(result.message)
                    is ToolResult.Blocked -> throw SecurityException(result.reason)
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
                audit("ACTION_ALLOWED", risk.name, step.description)
                delay(50)
            }

            AppState.setPhase(OperatorPhase.COMPLETED, "Task completed")
            val output = logs.joinToString(" ")
            db.dao().updateTask(TaskEntity(taskId, input, "COMPLETED", output))
            db.dao().addMemory(MemoryEntity(key = "last_task", value = input))
            output.ifBlank { "Task completed." }
        } catch (e: Throwable) {
            val reason = e.message ?: e.javaClass.simpleName
            val aborted = reason.contains("abort", true) || AppState.operator.value.phase == OperatorPhase.ABORTED
            AppState.setPhase(if (aborted) OperatorPhase.ABORTED else OperatorPhase.ERROR, reason)
            db.dao().updateTask(TaskEntity(taskId, input, if (aborted) "ABORTED" else "FAILED", reason))
            db.dao().addSafety(SafetyEventEntity(type = if (aborted) "TASK_ABORTED" else "TASK_FAILED", reason = reason, packageName = null, action = input))
            "Task stopped safely: $reason"
        }
    }

    private suspend fun audit(type: String, reason: String, action: String) {
        db.dao().addSafety(SafetyEventEntity(type = type, reason = reason, packageName = null, action = action))
    }

    private fun ToolResult.message(): String = when (this) {
        is ToolResult.Success -> message
        is ToolResult.Failure -> message
        is ToolResult.Blocked -> reason
    }
}
