package com.aurax.operator.operator.overlay

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import com.aurax.operator.operator.AbortReceiver
import com.aurax.operator.operator.OperatorIndicatorState
import com.aurax.operator.operator.OperatorRuntime
import kotlinx.coroutines.*

class OperatorOverlayService : Service() {
    private var wm: WindowManager? = null
    private var view: TextView? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) startForeground(NOTIFICATION_ID, notification())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        showIndicator()
        scope.launch {
            OperatorRuntime.indicator.collect { updateIndicator(it) }
        }
        scope.launch {
            OperatorRuntime.countdown.collect { state ->
                if (state != null) view?.text = "●\n${state.remainingSeconds}"
            }
        }
    }

    private fun notification(): Notification {
        val channelId = "aura_operator"
        if (Build.VERSION.SDK_INT >= 26) {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(NotificationChannel(channelId, "AURA-X Operator", NotificationManager.IMPORTANCE_LOW))
        }
        val abortIntent = PendingIntent.getBroadcast(
            this, 11,
            Intent(this, AbortReceiver::class.java).setAction(AbortReceiver.ACTION_ABORT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, channelId)
            .setContentTitle("AURA-X Operator active")
            .setContentText("Visible automation indicator is active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .addAction(Notification.Action.Builder(null, "Stop AURA-X Operator", abortIntent).build())
            .setOngoing(true)
            .build()
    }

    private fun showIndicator() {
        view = TextView(this).apply {
            text = "●"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(52, 211, 153))
            setBackgroundColor(Color.argb(80, 0, 0, 0))
            setOnClickListener { OperatorRuntime.abort() }
            contentDescription = "AURA-X Operator status. Tap to abort."
        }
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            64, 64, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 48
        }
        runCatching { wm?.addView(view, params) }
    }

    private fun updateIndicator(state: OperatorIndicatorState) {
        val color = when (state) {
            OperatorIndicatorState.OBSERVING -> Color.rgb(52, 211, 153)
            OperatorIndicatorState.COUNTDOWN, OperatorIndicatorState.ACTING -> Color.rgb(251, 191, 36)
            OperatorIndicatorState.BLOCKED, OperatorIndicatorState.ABORTED -> Color.rgb(251, 113, 133)
        }
        view?.setTextColor(color)
        if (OperatorRuntime.countdown.value == null) view?.text = "●"
    }

    override fun onDestroy() {
        scope.cancel()
        view?.let { runCatching { wm?.removeView(it) } }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object { const val NOTIFICATION_ID = 7 }
}
