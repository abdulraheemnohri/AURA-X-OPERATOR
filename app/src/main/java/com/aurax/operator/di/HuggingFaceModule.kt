package com.aurax.operator.di

import com.aurax.operator.ai.model.HuggingFaceClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HuggingFaceModule {
    @Provides
    @Singleton
    fun provideHuggingFaceClient(): HuggingFaceClient = HuggingFaceClient()
}
