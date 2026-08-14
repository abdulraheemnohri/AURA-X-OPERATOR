package com.aurax.operator.data

import android.content.Context
import com.aurax.operator.data.database.AuraDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

/** Encrypted export container for local operational data and settings. */
class BackupManager(private val context: Context) {
    private val db = AuraDatabase.get(context)

    suspend fun createBackup(password: String): File {
        require(password.length >= 8) { "Backup password must be at least 8 characters." }
        val dao = db.dao()
        val payload = JSONObject()
            .put("version", 1)
            .put("createdAt", System.currentTimeMillis())
            .put("memories", dao.memories().map { JSONObject().put("key", it.key).put("value", it.value) })
            .put("safetyEvents", dao.safetyEvents().map { JSONObject().put("timestamp", it.timestamp).put("type", it.type).put("reason", it.reason).put("packageName", it.packageName).put("action", it.action) })
            .put("settings", JSONObject(context.getSharedPreferences("aura_settings_v3", Context.MODE_PRIVATE).all))
        val plain = payload.toString().toByteArray(Charsets.UTF_8)
        val salt = MessageDigest.getInstance("SHA-256").digest(("AURA-X:" + context.packageName).toByteArray())
        val key = SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(password.toByteArray() + salt), "AES")
        val iv = MessageDigest.getInstance("SHA-256").digest((System.currentTimeMillis().toString() + password).toByteArray()).copyOf(12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plain)
        val file = File(context.cacheDir, "aura_backup_${System.currentTimeMillis()}.aura")
        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.putNextEntry(ZipEntry("payload.bin")); zip.write(encrypted); zip.closeEntry()
            zip.putNextEntry(ZipEntry("meta.json")); zip.write(JSONObject().put("version", 1).put("iv", iv.joinToString(",")).toString().toByteArray()); zip.closeEntry()
        }
        return file
    }

    /** Stages the encrypted archive for a future safe restore transaction. */
    fun stageRestore(file: File, password: String): Result<File> = runCatching {
        require(file.exists()) { "Backup file not found." }
        require(password.length >= 8) { "Backup password must be at least 8 characters." }
        val staged = File(context.filesDir, "restore/pending.aura")
        staged.parentFile?.mkdirs()
        FileInputStream(file).use { input -> FileOutputStream(staged).use { output -> input.copyTo(output) } }
        staged
    }
}
