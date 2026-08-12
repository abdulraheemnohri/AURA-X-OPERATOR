package com.aurax.operator.operator

import android.app.Notification
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory notification view. Notification contents are not persisted here. */
data class NotificationSnapshot(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)

object NotificationSnapshotStore {
    private val _notifications = MutableStateFlow<List<NotificationSnapshot>>(emptyList())
    val notifications: StateFlow<List<NotificationSnapshot>> = _notifications.asStateFlow()

    private fun snapshot(sbn: StatusBarNotification): NotificationSnapshot {
        val extras = sbn.notification.extras
        return NotificationSnapshot(
            key = sbn.key,
            packageName = sbn.packageName,
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),
            timestamp = sbn.postTime
        )
    }

    @Synchronized
    fun replace(items: List<StatusBarNotification>) {
        _notifications.value = items
            .asSequence()
            .filterNot { AccessibilityGuardrails.isBlockedPackage(it.packageName) }
            .map(::snapshot)
            .sortedByDescending { it.timestamp }
            .take(100)
            .toList()
    }

    @Synchronized
    fun upsert(sbn: StatusBarNotification) {
        if (AccessibilityGuardrails.isBlockedPackage(sbn.packageName)) return
        val next = _notifications.value.filterNot { it.key == sbn.key } + snapshot(sbn)
        _notifications.value = next.sortedByDescending { it.timestamp }.take(100)
    }

    @Synchronized
    fun remove(key: String) {
        _notifications.value = _notifications.value.filterNot { it.key == key }
    }

    fun clear() {
        _notifications.value = emptyList()
    }
}
