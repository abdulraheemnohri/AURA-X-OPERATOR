package com.aurax.operator.analytics

data class AnalyticsSnapshot(val tasks:Int,val completed:Int,val failed:Int,val memories:Int,val safetyEvents:Int){val successRate:Float get()=if(tasks==0)0f else completed.toFloat()/tasks}
