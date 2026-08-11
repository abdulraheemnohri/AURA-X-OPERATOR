package com.aurax.operator.operator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aurax.operator.operator.overlay.OperatorOverlayService

class AbortReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_ABORT) {
            OperatorRuntime.abort()
            context.stopService(Intent(context, OperatorOverlayService::class.java))
        }
    }

    companion object {
        const val ACTION_ABORT = "com.aurax.operator.action.ABORT"
    }
}
