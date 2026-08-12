package com.aurax.operator.core.security

import android.content.Context
import com.aurax.operator.operator.ActionRisk
import com.aurax.operator.operator.AutomationPolicy
import com.aurax.operator.operator.AutomationPolicyEngine

class PolicyRuntime(context: Context) {
    private val store = AutomationPolicyStore(context)

    fun current(): AutomationPolicy = store.policy

    fun canExecute(risk: ActionRisk): Boolean = AutomationPolicyEngine.canExecute(current(), risk)

    fun shouldConfirm(risk: ActionRisk): Boolean = when (current()) {
        AutomationPolicy.OBSERVE_ONLY, AutomationPolicy.SUGGEST_ONLY -> false
        AutomationPolicy.CONFIRM_ACTIONS -> risk != ActionRisk.LOW
        AutomationPolicy.FULL_AUTO_LOW_RISK -> risk != ActionRisk.LOW
    }
}
