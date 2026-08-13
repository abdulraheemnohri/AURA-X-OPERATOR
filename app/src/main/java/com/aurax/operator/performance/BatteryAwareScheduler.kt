package com.aurax.operator.performance

import android.content.Context
import android.os.BatteryManager

class BatteryAwareScheduler(private val context:Context,private val thermal:ThermalManager){
    fun batteryPercent():Int=context.getSystemService(BatteryManager::class.java)?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(0,100)?:100
    fun isCharging():Boolean=context.getSystemService(BatteryManager::class.java)?.isCharging?:false
    fun shouldRunHeavyTask():Boolean{thermal.refresh();val b=batteryPercent();return (b>50||isCharging()||(b>20&&thermal.state.value==ThermalManager.State.LIGHT))&&thermal.canRunHeavyInference()}
}
