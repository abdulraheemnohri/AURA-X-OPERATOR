package com.aurax.operator.app

import android.app.Application
import com.aurax.operator.data.database.AuraDatabase
import com.aurax.operator.operator.OperatorAudit

class AuraApplication : Application() {
    val db by lazy { AuraDatabase.get(this) }
    override fun onCreate() {
        super.onCreate()
        OperatorAudit.init(this)
    }
}
