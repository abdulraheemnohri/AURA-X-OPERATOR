package com.aurax.operator.di

import android.content.Context
import androidx.work.WorkManager
import com.aurax.operator.data.database.AuraDao
import com.aurax.operator.data.database.AuraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AuraDatabase = AuraDatabase.get(context)

    @Provides
    @Singleton
    fun provideDao(database: AuraDatabase): AuraDao = database.dao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
