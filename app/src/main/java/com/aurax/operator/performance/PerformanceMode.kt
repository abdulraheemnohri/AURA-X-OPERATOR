package com.aurax.operator.performance

enum class PerformanceMode(val threads:Int,val contextLength:Int,val maxOutputTokens:Int,val batchSize:Int){BATTERY_SAVER(2,1024,256,256),BALANCED(4,2048,512,512),PERFORMANCE(6,4096,1024,1024),CUSTOM(4,2048,512,512)}
