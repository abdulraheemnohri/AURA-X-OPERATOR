package com.aurax.operator.ai.model
import com.aurax.operator.ai.inference.GenerationRequest
interface AIModelRuntime{suspend fun generate(request:GenerationRequest):String;fun isReady():Boolean}