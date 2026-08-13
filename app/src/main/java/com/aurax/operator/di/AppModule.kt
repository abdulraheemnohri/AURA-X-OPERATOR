package com.aurax.operator.di

import android.content.Context
import com.aurax.operator.agent.planner.LocalModelPlanner
import com.aurax.operator.agent.planner.OperatorPlanner
import com.aurax.operator.data.database.AuraDao
import com.aurax.operator.data.database.AuraDatabase
import com.aurax.operator.data.export.SafetyLogExporter
import com.aurax.operator.agent.execution.TaskExecutor
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
    fun providePlanner(): OperatorPlanner = OperatorPlanner()

    @Provides
    @Singleton
    fun provideLocalModelPlanner(@ApplicationContext context: Context): LocalModelPlanner = LocalModelPlanner(context)

    @Provides
    @Singleton
    fun provideTaskExecutor(
        @ApplicationContext context: Context,
        database: AuraDatabase,
        planner: OperatorPlanner,
        localModelPlanner: LocalModelPlanner
    ): TaskExecutor = TaskExecutor(context, database, planner, localModelPlanner)
}
