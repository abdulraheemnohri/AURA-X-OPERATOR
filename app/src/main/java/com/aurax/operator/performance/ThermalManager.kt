package com.aurax.operator.performance

import android.content.Context
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThermalManager(private val context: Context) {
    enum class State { NORMAL, LIGHT, MODERATE, SEVERE, CRITICAL, EMERGENCY, UNKNOWN }
    private val _state=MutableStateFlow(State.UNKNOWN); val state:StateFlow<State> = _state.asStateFlow()
    fun refresh(){ if(Build.VERSION.SDK_INT<29){_state.value=State.UNKNOWN;return}; val status=context.getSystemService(PowerManager::class.java)?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE; _state.value=when(status){PowerManager.THERMAL_STATUS_NONE->State.NORMAL;PowerManager.THERMAL_STATUS_LIGHT->State.LIGHT;PowerManager.THERMAL_STATUS_MODERATE->State.MODERATE;PowerManager.THERMAL_STATUS_SEVERE->State.SEVERE;PowerManager.THERMAL_STATUS_CRITICAL->State.CRITICAL;PowerManager.THERMAL_STATUS_EMERGENCY->State.EMERGENCY;else->State.UNKNOWN} }
    fun canRunHeavyInference()=state.value==State.NORMAL||state.value==State.LIGHT||state.value==State.UNKNOWN
}
