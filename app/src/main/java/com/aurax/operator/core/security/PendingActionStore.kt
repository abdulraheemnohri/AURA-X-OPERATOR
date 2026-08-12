package com.aurax.operator.core.security

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PendingActionStore {
    data class PendingAction(
        val taskId: Long,
        val description: String,
        val packageName: String?,
        val createdAtMillis: Long = System.currentTimeMillis()
    )

    private val mutex = Mutex()
    private var pending: PendingAction? = null

    suspend fun set(action: PendingAction) = mutex.withLock { pending = action }
    suspend fun get(): PendingAction? = mutex.withLock { pending }
    suspend fun consume(): PendingAction? = mutex.withLock { pending.also { pending = null } }
    suspend fun clear() = mutex.withLock { pending = null }
}
