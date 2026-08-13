package com.aurax.operator.app

import android.app.Application
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
class AuraApplication : Application() {
    val db by lazy { AuraDatabase.get(this) }

    @Inject lateinit var modelHub: ModelHub

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        OperatorAudit.init(this)
        applicationScope.launch { modelHub.seedBuiltIns() }
    }
}
