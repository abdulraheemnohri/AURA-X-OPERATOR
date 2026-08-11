package com.aurax.operator.operator

import android.content.Context
import com.aurax.operator.data.database.AuraDatabase
import com.aurax.operator.data.entities.OperatorActionEntity
import com.aurax.operator.data.entities.SafetyEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object OperatorAudit {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var db: AuraDatabase? = null

    fun init(context: Context) { db = AuraDatabase.get(context.applicationContext) }

    fun action(packageName: String?, action: String, target: String?, allowed: Boolean) {
        val database = db ?: return
        scope.launch {
            database.dao().addAction(
                OperatorActionEntity(
                    taskId = null,
                    packageName = packageName.orEmpty(),
                    action = action,
                    target = target,
                    allowed = allowed
                )
            )
        }
    }

    fun safety(type: String, reason: String, packageName: String?, action: String?) {
        val database = db ?: return
        scope.launch { database.dao().addSafety(SafetyEventEntity(type, reason, packageName, action)) }
    }
}
