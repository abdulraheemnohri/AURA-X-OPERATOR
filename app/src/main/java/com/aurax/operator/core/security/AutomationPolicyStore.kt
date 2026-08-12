package com.aurax.operator.core.security

import android.content.Context
import com.aurax.operator.core.app.AutomationPolicy

class AutomationPolicyStore(context: Context) {
    private val prefs = SecurePrefs(context)

    var policy: AutomationPolicy
        get() = runCatching { AutomationPolicy.valueOf(prefs.policy) }.getOrDefault(AutomationPolicy.CONFIRM_ACTIONS)
        set(value) { prefs.policy = value.name }
}
