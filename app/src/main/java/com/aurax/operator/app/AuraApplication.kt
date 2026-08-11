package com.aurax.operator.app
import android.app.Application
import com.aurax.operator.data.database.AuraDatabase
class AuraApplication: Application(){ val db by lazy { AuraDatabase.get(this) } }