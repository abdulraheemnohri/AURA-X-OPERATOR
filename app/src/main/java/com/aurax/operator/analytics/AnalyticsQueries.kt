package com.aurax.operator.analytics

import com.aurax.operator.data.database.AuraDao

/** Database-backed metrics used by the dashboard without introducing another persistence layer. */
class AnalyticsQueries(private val dao: AuraDao) {
    suspend fun snapshot(): AnalyticsSnapshot {
        val tasks = dao.tasks()
        val completed = tasks.count { it.status.equals("COMPLETED", ignoreCase = true) }
        val failed = tasks.count { it.status.equals("FAILED", ignoreCase = true) }
        return AnalyticsSnapshot(
            tasks = tasks.size,
            completed = completed,
            failed = failed,
            memories = dao.memories().size,
            safetyEvents = dao.safetyEvents().size
        )
    }

    suspend fun recentActivity(limit: Int = 20) = dao.getRecentMessages(limit.coerceIn(1, 100))
}
