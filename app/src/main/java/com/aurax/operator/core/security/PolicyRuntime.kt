package com.aurax.operator.core.security

import android.content.Context
import com.aurax.operator.operator.ActionRisk
import com.aurax.operator.operator.AutomationPolicy
import com.aurax.operator.operator.AutomationPolicyEngine

class PolicyRuntime(context: Context) {
    private val store = AutomationPolicyStore(context)
    private val prefs = SecurePrefs(context.applicationContext)

    fun current(): AutomationPolicy = store.policy

    fun canExecute(risk: ActionRisk): Boolean = AutomationPolicyEngine.canExecute(current(), risk)

    fun shouldConfirm(risk: ActionRisk): Boolean = AutomationPolicyEngine.requiresConfirmation(current(), risk)

    fun maxActionsPerTask(): Int = prefs.maxActionsPerTask

    fun maxTaskSeconds(): Int = prefs.maxTaskSeconds
}
