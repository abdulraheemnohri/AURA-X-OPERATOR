package com.aurax.operator.data

import android.content.Context
import com.aurax.operator.app.AuraApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LogExporter {
    suspend fun csv(context: Context): String = withContext(Dispatchers.IO) {
        val db = (context.applicationContext as AuraApplication).db
        val actions = db.dao().safetyEvents()
        buildString {
            appendLine("timestamp,type,reason,package,action")
            actions.forEach {
                appendLine(listOf(it.timestamp, it.type, it.reason, it.packageName.orEmpty(), it.action.orEmpty()).joinToString(",") { value ->
                    "\"${value.toString().replace("\"", "\"\"")}\""
                })
            }
        }
    }
}
