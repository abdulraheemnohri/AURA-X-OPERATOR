package com.aurax.operator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aurax.operator.R
import com.aurax.operator.app.MainActivity
import com.aurax.operator.core.app.AppState
import com.aurax.operator.core.security.SafetyController

/**
 * Small home-screen cockpit for the two actions that must stay reachable:
 * opening AURA-X and requesting an emergency abort.
 */
class AuraOperatorWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_ABORT) {
            SafetyController.requestAbort("Home-screen emergency abort")
            updateAll(context)
        }
    }

    companion object {
        const val ACTION_ABORT = "com.aurax.operator.widget.ACTION_ABORT"

        private fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_aura_operator)
            val phase = AppState.operator.value.phase.name.replace('_', ' ')
            views.setTextViewText(R.id.widget_status, "Status: ${phase.lowercase().replaceFirstChar { it.uppercase() }}")

            val openIntent = Intent(context, MainActivity::class.java)
            val openPending = PendingIntent.getActivity(
                context,
                id,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_open, openPending)

            val abortIntent = Intent(context, AuraOperatorWidget::class.java).setAction(ACTION_ABORT)
            val abortPending = PendingIntent.getBroadcast(
                context,
                id + 10000,
                abortIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_abort, abortPending)
            manager.updateAppWidget(id, views)
        }

        private fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, AuraOperatorWidget::class.java)
            manager.getAppWidgetIds(component).forEach { update(context, manager, it) }
        }
    }
}
