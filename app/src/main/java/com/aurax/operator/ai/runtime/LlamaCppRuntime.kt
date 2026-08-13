package com.aurax.operator.ai.runtime

import android.content.Context
import com.aurax.operator.ai.inference.GenerationRequest
import com.aurax.operator.ai.model.AIModelRuntime
import com.aurax.operator.ai.model.ModelRepository
import com.aurax.operator.core.settings.SettingsRepository
import java.io.File

class LlamaCppRuntime(context: Context) : AIModelRuntime {
    private val repository = ModelRepository(context.applicationContext)
    private val settings = SettingsRepository(context.applicationContext)
    private var configuredThreads = settings.cpuThreads
    private var configuredContext = settings.contextLength
    private var configuredOutput = settings.maxOutputTokens
    private var configuredBatch = settings.batchSize

    companion object {
        private var loaded = runCatching { System.loadLibrary("aurax_native"); true }.getOrDefault(false)
    }

    private external fun nativeGenerate(modelPath:String, prompt:String, maxTokens:Int, temperature:Float, contextTokens:Int, threads:Int, batch:Int, topK:Int):String

    override suspend fun generate(request:GenerationRequest):String {
        return generateFrom(repository.primaryModel, request)
    }

    suspend fun generateFrom(modelFile:File, request:GenerationRequest):String {
        check(loaded) { "Native local engine is unavailable" }
        check(modelFile.isFile) { "Local model file is unavailable: ${modelFile.absolutePath}" }
        return nativeGenerate(modelFile.absolutePath, request.prompt, request.safeMaxTokens.coerceAtMost(configuredOutput), request.safeTemperature, request.safeContextTokens.coerceAtMost(configuredContext), configuredThreads.coerceIn(1,12), configuredBatch.coerceIn(32,2048), settings.topK.coerceIn(1,200))
    }

    override fun isReady():Boolean = loaded && repository.isInstalled()
    override fun setThreads(value:Int){ configuredThreads=value.coerceIn(1,12) }
    override fun setContextLength(value:Int){ configuredContext=value.coerceIn(256,4096) }
    override fun setMaxOutputTokens(value:Int){ configuredOutput=value.coerceIn(32,2048) }
    override fun setBatchSize(value:Int){ configuredBatch=value.coerceIn(32,2048) }
    override fun threads()=configuredThreads
    override fun contextLength()=configuredContext
    override fun maxOutputTokens()=configuredOutput
    override fun batchSize()=configuredBatch
}
