package com.aurax.operator.operator

import android.app.Notification
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory notification view used by the operator. Notification text is never
 * persisted by this component; callers decide whether an action should be audited.
 */
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

    @Synchronized
    fun replace(items: List<StatusBarNotification>) {
        _notifications.value = items
            .asSequence()
            .filterNot { AccessibilityGuardrails.isBlockedPackage(it.packageName) }
            .map { sbn ->
                val extras = sbn.notification.extras
                NotificationSnapshot(
                    key = sbn.key,
                    packageName = sbn.packageName,
                    title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),
                    text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),
                    timestamp = sbn.postTime
                )
            }
            .sortedByDescending { it.timestamp }
            .take(100)
            .toList()
    }

    @Synchronized
    fun upsert(sbn: StatusBarNotification) {
        val current = _notifications.value.filterNot { it.key == sbn.key }
        replace(current.toStatusBarNotificationsFallback())
    }

    @Synchronized
    fun remove(key: String) {
        _notifications.value = _notifications.value.filterNot { it.key == key }
    }

    fun clear() {
        _notifications.value = emptyList()
    }

    private fun List<NotificationSnapshot>.toStatusBarNotificationsFallback(): List<StatusBarNotification> = emptyList()
}
