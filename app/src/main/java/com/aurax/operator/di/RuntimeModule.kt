package com.aurax.operator.di

import com.aurax.operator.ai.runtime.LlamaCppRuntime
import com.aurax.operator.ai.runtime.LocalRuntimeManager
import com.aurax.operator.ai.vision.LlavaVisionRuntime
import com.aurax.operator.ai.vision.VisionRuntime
import com.aurax.operator.vision.ocr.MlKitOcrRuntime
import com.aurax.operator.vision.ocr.OcrRuntime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt module for providing AI and perception runtime dependencies. */
@Module
@InstallIn(SingletonComponent::class)
object RuntimeModule {
    @Provides
    @Singleton
    fun provideLlamaCppRuntime(): LlamaCppRuntime = LlamaCppRuntime()

    @Provides
    @Singleton
    fun provideLocalRuntimeManager(llamaCppRuntime: LlamaCppRuntime): LocalRuntimeManager =
        LocalRuntimeManager(llamaCppRuntime)

    @Provides
    @Singleton
    fun provideVisionRuntime(): VisionRuntime = LlavaVisionRuntime()

    @Provides
    @Singleton
    fun provideOcrRuntime(): OcrRuntime = MlKitOcrRuntime()
}
