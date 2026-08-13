package com.aurax.operator.operator

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Optional, user-enabled notification reader. It keeps only an in-memory,
 * safety-filtered summary and never posts notification contents to a server.
 */
class AuraNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationSnapshotStore.replace(activeNotifications?.toList().orEmpty())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationSnapshotStore.upsert(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationSnapshotStore.remove(sbn.key)
    }

    override fun onDestroy() {
        NotificationSnapshotStore.clear()
        super.onDestroy()
    }
}
