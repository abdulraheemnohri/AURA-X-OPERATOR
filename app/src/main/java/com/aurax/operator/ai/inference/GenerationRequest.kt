package com.aurax.operator.ai.inference
data class GenerationRequest(val prompt:String,val maxTokens:Int=256,val temperature:Float=.2f)