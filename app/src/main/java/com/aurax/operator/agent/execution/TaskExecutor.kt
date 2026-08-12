package com.aurax.operator.agent.execution

import android.content.Context
import com.aurax.operator.agent.planner.LocalModelPlanner
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
import com.aurax.operator.tools.registry.RiskLevel
import com.aurax.operator.tools.registry.ToolRegistry
import com.aurax.operator.tools.youtube.YouTubeTool
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

class TaskExecutor(private val context: Context) {
    private val db = AuraDatabase.get(context)
    private val planner = OperatorPlanner()
    private val localModelPlanner = LocalModelPlanner(context)
    private val policyRuntime = PolicyRuntime(context)
    private val pendingActions = PendingActionStore()
    private val confirmation = ConfirmationCoordinator(pendingActions)

    suspend fun confirmPending(): Boolean = confirmation.confirm() != null
    suspend fun abortPending(): Boolean = confirmation.abort() != null

    suspend fun execute(input: String): String {
        val taskId = db.dao().addTask(TaskEntity(input = input, status = "RUNNING"))
        val startedAt = System.currentTimeMillis()
        var actionCount = 0

        fun enforceRuntimeLimits() {
            if (System.currentTimeMillis() - startedAt > policyRuntime.maxTaskSeconds() * 1_000L) {
                throw SecurityException("Task time limit reached")
            }
            if (actionCount >= policyRuntime.maxActionsPerTask()) {
                throw SecurityException("Task action limit reached")
            }
            OperatorRuntime.ensureNotAborted()
        }

        return try {
            if (input.isBlank()) throw IllegalArgumentException("Task input is empty")

            if (policyRuntime.current() == AutomationPolicy.OBSERVE_ONLY || policyRuntime.current() == AutomationPolicy.SUGGEST_ONLY) {
                db.dao().updateTask(TaskEntity(taskId, input, "SUGGESTED", "Policy prevents automation."))
                db.dao().addSafety(SafetyEventEntity(type = "POLICY_BLOCK", reason = policyRuntime.current().name, packageName = null, action = input))
                return "I prepared the task, but the current policy prevents execution."
            }

            enforceRuntimeLimits()
            val service = AuraAccessibilityService.instance ?: throw IllegalStateException("AccessibilityService is not enabled")
            val operator = service.operator
            val registry = ToolRegistry(listOf(ChromeTool(context, operator), YouTubeTool(context, operator)))
            val android = AndroidTool(context)
            val steps = localModelPlanner.plan(input) ?: planner.plan(input)
            val logs = mutableListOf<String>()

            for ((index, step) in steps.withIndex()) {
                enforceRuntimeLimits()
                AppState.setStep(step.description, if (steps.isEmpty()) 1f else index.toFloat() / steps.size)

                val packageName = step.args["package"]
                    ?: step.args["packageName"]
                    ?: when (step.tool) {
                        "chrome_automation" -> "com.android.chrome"
                        "youtube_automation" -> "com.google.android.youtube"
                        else -> null
                    }

                val packageBlocked = AccessibilityGuardrails.isBlockedPackage(packageName)
                val sensitive = AccessibilityGuardrails.isSensitiveText(step.description) ||
                    step.args.values.any { AccessibilityGuardrails.isSensitiveText(it) }
                val risk = AutomationPolicyEngine.classify(step.description, packageName, packageBlocked || sensitive)

                if (risk == ActionRisk.BLOCKED) {
                    audit("BLOCKED_ACTION", "Sensitive or protected target", step.description)
                    throw SecurityException("Blocked sensitive/protected action")
                }
                if (!policyRuntime.canExecute(risk)) {
                    audit("POLICY_BLOCK", risk.name, step.description)
                    throw SecurityException("Policy ${policyRuntime.current()} refused ${risk.name} action")
                }

                if (policyRuntime.shouldConfirm(risk)) {
                    AppState.setPhase(OperatorPhase.CONFIRMING, "Confirm: ${step.description}")
                    audit("CONFIRMATION_REQUIRED", risk.name, step.description)
                    pendingActions.set(PendingActionStore.PendingAction(taskId, step.description, step.tool))
                    OperatorSafety.beginConfirmation(policyRuntime.confirmationSeconds())
                    val approved = withTimeoutOrNull(policyRuntime.maxTaskSeconds() * 1_000L) {
                        while (AppState.operator.value.phase == OperatorPhase.CONFIRMING && !AppState.operator.value.abortRequested) {
                            delay(100)
                        }
                        AppState.operator.value.phase == OperatorPhase.EXECUTING
                    } ?: false
                    if (!approved) {
                        pendingActions.clear()
                        audit("ACTION_ABORTED", "User did not confirm", step.description)
                        throw SecurityException("Action not confirmed")
                    }
                }

                enforceRuntimeLimits()

                if (step.tool == "none") {
                    logs += step.description
                    continue
                }
                if (step.tool == "android_open") {
                    val targetPackage = step.args["package"] ?: throw IllegalArgumentException("Missing package")
                    if (AccessibilityGuardrails.isBlockedPackage(targetPackage)) {
                        audit("BLOCKED_ACTION", "Protected package", step.description)
                        throw SecurityException("Opening this protected package is blocked")
                    }
                    val result = android.openPackage(targetPackage)
                    logs += result.message()
                    actionCount++
                    db.dao().addAction(
                        OperatorActionEntity(
                            taskId = taskId,
                            packageName = targetPackage,
                            action = step.description,
                            target = step.args.toString(),
                            allowed = true
                        )
                    )
                    audit("ACTION_ALLOWED", risk.name, step.description)
                    continue
                }

                val tool = registry.get(step.tool) ?: error("Unsupported tool: ${step.tool}")
                if (tool.riskLevel == RiskLevel.HIGH) throw SecurityException("High-risk automation is disabled")
                when (val result = tool.execute(step.args)) {
                    is ToolResult.Success -> logs += result.message
                    is ToolResult.Failure -> throw IllegalStateException(result.message)
                    is ToolResult.Blocked -> throw SecurityException(result.reason)
                }
                actionCount++
                db.dao().addAction(
                    OperatorActionEntity(
                        taskId = taskId,
                        packageName = packageName ?: step.tool,
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
            pendingActions.clear()
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
