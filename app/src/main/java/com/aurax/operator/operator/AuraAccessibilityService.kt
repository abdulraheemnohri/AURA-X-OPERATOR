package com.aurax.operator.operator

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuraAccessibilityService : AccessibilityService() {
    companion object { @Volatile var instance: AuraAccessibilityService? = null }

    private val _windowState = MutableStateFlow<WindowState>(WindowState.Idle)
    val windowState: StateFlow<WindowState> = _windowState.asStateFlow()
    lateinit var operator: AccessibilityOperator
        private set

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        operator = AccessibilityOperator(this)
        OperatorRuntime.begin()
        if (android.provider.Settings.canDrawOverlays(this)) {
            runCatching { startService(Intent(this, com.aurax.operator.operator.overlay.OperatorOverlayService::class.java)) }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val relevant = setOf(
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
        )
        if (event.eventType !in relevant) return
        val root = rootInActiveWindow ?: return
        _windowState.value = WindowState.Changed(
            event.packageName?.toString().orEmpty(),
            event.className?.toString().orEmpty(),
            ScreenContextExtractor.extract(root)
        )
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            OperatorRuntime.abort()
            return true
        }
        return false
    }

    override fun onInterrupt() { OperatorRuntime.abort() }

    override fun onDestroy() {
        instance = null
        OperatorRuntime.abort()
        runCatching { stopService(Intent(this, com.aurax.operator.operator.overlay.OperatorOverlayService::class.java)) }
        super.onDestroy()
    }
}

sealed interface WindowState {
    data object Idle : WindowState
    data class Changed(val packageName: String, val className: String, val context: ScreenContext) : WindowState
}
