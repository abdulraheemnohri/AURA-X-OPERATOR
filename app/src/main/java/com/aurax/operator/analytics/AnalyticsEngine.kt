package com.aurax.operator.analytics

import com.aurax.operator.data.database.AuraDao

class AnalyticsEngine(private val dao: AuraDao) {
    suspend fun snapshot(): AnalyticsSnapshot {
        val tasks = dao.tasks()
        val safety = dao.safetyEvents()
        val memories = dao.memories()
        val models = dao.getReadyModels()
        val successful = tasks.count { it.status.equals("SUCCESS", ignoreCase = true) }
        val finished = tasks.count { it.status.equals("SUCCESS", true) || it.status.equals("FAILED", true) || it.status.equals("ABORTED", true) }
        return AnalyticsSnapshot(
            totalConversations = dao.getRecentMessages(10_000).count(),
            memoryCount = memories.size,
            safetyEventCount = safety.size,
            readyModelCount = models.size,
            taskCount = tasks.size,
            taskSuccessRate = if (finished == 0) 0f else successful.toFloat() / finished.toFloat()
        )
    }
}
