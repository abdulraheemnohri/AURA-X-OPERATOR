package com.aurax.operator.analytics

import android.content.Context
import com.aurax.operator.data.database.AuraDatabase

class AnalyticsRepository(context: Context) {
    private val dao = AuraDatabase.get(context.applicationContext).dao()
    suspend fun snapshot(): AnalyticsSnapshot {
        val tasks = dao.tasks()
        val completed = tasks.count { it.status.equals("COMPLETED", true) || it.status.equals("SUCCESS", true) }
        val failed = tasks.count { it.status.equals("FAILED", true) || it.status.equals("ERROR", true) }
        return AnalyticsSnapshot(tasks.size, completed, failed, dao.memories().size, dao.safetyEvents().size)
    }
}
