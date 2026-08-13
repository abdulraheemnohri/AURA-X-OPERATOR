package com.aurax.operator.ai.model

enum class ModelPreset(val context:Int,val maxTokens:Int,val threads:Int,val batch:Int){ULTRA_FAST(512,256,2,256),BALANCED(2048,512,4,512),QUALITY(4096,1024,4,768),MAX_QUALITY(8192,2048,6,1024),CUSTOM(2048,512,4,512)}
