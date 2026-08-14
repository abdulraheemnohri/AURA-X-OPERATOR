package com.aurax.operator.analytics

import com.aurax.operator.data.database.AuraDao

/** Read-only local aggregation; no analytics telemetry leaves the device. */
class AnalyticsEngine(private val dao: AuraDao) {
    suspend fun snapshot(): AnalyticsSnapshot {
        val tasks = dao.tasks()
        val safety = dao.safetyEvents()
        val memories = dao.memories()
        val completed = tasks.count { it.status.equals("COMPLETED", ignoreCase = true) || it.status.equals("SUCCESS", ignoreCase = true) }
        val failed = tasks.count { it.status.equals("FAILED", ignoreCase = true) || it.status.equals("ABORTED", ignoreCase = true) }
        return AnalyticsSnapshot(
            tasks = tasks.size,
            completed = completed,
            failed = failed,
            memories = memories.size,
            safetyEvents = safety.size
        )
    }
}
