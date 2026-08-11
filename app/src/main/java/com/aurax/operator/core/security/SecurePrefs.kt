package com.aurax.operator.core.security
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
class SecurePrefs(context:Context){ private val prefs=EncryptedSharedPreferences.create(context,"aura_secure",MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM); var policy:String get()=prefs.getString("policy","CONFIRM_ACTIONS")!!; set(v){prefs.edit().putString("policy",v).apply()} }