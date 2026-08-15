package com.aurax.operator.di

import com.aurax.operator.ai.runtime.LlamaCppRuntime
import com.aurax.operator.ai.runtime.LocalRuntimeManager
import com.aurax.operator.ai.vision.LlavaVisionRuntime
import com.aurax.operator.ai.vision.VisionRuntime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing AI runtime dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object RuntimeModule {
    
    @Provides
    @Singleton
    fun provideLlamaCppRuntime(): LlamaCppRuntime {
        return LlamaCppRuntime()
    }
    
    @Provides
    @Singleton
    fun provideLocalRuntimeManager(llamaCppRuntime: LlamaCppRuntime): LocalRuntimeManager {
        return LocalRuntimeManager(llamaCppRuntime)
    }
    
    @Provides
    @Singleton
    fun provideVisionRuntime(): VisionRuntime {
        return LlavaVisionRuntime()
    }
}
