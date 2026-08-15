package com.aurax.operator.di

import android.content.Context
import com.aurax.operator.ai.model.ModelHub
import com.aurax.operator.network.lan.LANServer
import com.aurax.operator.network.lan.LANSettings
import com.aurax.operator.network.lan.QRPairingManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing network-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideLANSettings(): LANSettings {
        return LANSettings(
            enabled = false,
            port = 8080,
            requireAuth = false,
            authToken = ""
        )
    }
    
    @Provides
    @Singleton
    fun provideLANServer(
        context: Context,
        modelHub: ModelHub,
        settings: LANSettings
    ): LANServer {
        return LANServer(context, modelHub, settings)
    }
    
    @Provides
    @Singleton
    fun provideQRPairingManager(
        context: Context,
        lanServer: LANServer
    ): QRPairingManager {
        return QRPairingManager(context, lanServer)
    }
}
