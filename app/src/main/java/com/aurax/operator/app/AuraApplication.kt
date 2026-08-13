package com.aurax.operator.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.aurax.operator.ai.model.ModelHub
import com.aurax.operator.data.database.AuraDatabase
import com.aurax.operator.operator.OperatorAudit
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class AuraApplication : Application(), Configuration.Provider {
    val db by lazy { AuraDatabase.get(this) }

    @Inject lateinit var modelHub: ModelHub
    @Inject lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        OperatorAudit.init(this)
        applicationScope.launch { modelHub.seedBuiltIns() }
    }
}
