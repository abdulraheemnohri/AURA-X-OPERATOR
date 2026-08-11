package com.aurax.operator.ai.runtime

import android.content.Context
import com.aurax.operator.ai.inference.GenerationRequest
import com.aurax.operator.ai.model.AIModelRuntime
import com.aurax.operator.ai.model.ModelRepository

class LlamaCppRuntime(context: Context) : AIModelRuntime {
    private val repository = ModelRepository(context.applicationContext)

    companion object {
        private var loaded = false
        init { loaded = runCatching { System.loadLibrary("aurax_native"); true }.getOrDefault(false) }
    }

    private external fun nativeGenerate(modelPath: String, prompt: String, maxTokens: Int): String

    override suspend fun generate(request: GenerationRequest): String {
        check(loaded) { "Native llama.cpp library is unavailable" }
        check(repository.isInstalled()) {
            "Local GGUF model is not installed. Import ${ModelRepository.RECOMMENDED_FILENAME} from Hugging Face."
        }
        return nativeGenerate(repository.primaryModel.absolutePath, request.prompt, request.maxTokens)
    }

    override fun isReady(): Boolean = loaded && repository.isInstalled()
}
