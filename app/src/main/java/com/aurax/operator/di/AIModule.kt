package com.aurax.operator.di

import android.content.Context
import com.aurax.operator.ai.runtime.LlamaCppRuntime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {
    @Provides
    @Singleton
    fun provideLlamaCppRuntime(@ApplicationContext context: Context): LlamaCppRuntime {
        return LlamaCppRuntime(context)
    }
}
