package com.aurax.operator.operator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class AbortReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){if(intent.action==ACTION_ABORT){OperatorRuntime.abort();context.stopService(Intent(context,overlay.OperatorOverlayService::class.java))}}companion object{const val ACTION_ABORT="com.aurax.operator.action.ABORT"}}