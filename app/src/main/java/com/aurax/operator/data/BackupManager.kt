package com.aurax.operator.data

import android.content.Context
import android.util.Base64
import com.aurax.operator.data.database.AuraDatabase
import com.aurax.operator.data.entities.MemoryEntity
import com.aurax.operator.data.entities.MessageEntity
import com.aurax.operator.data.entities.SafetyEventEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONArray
import org.json.JSONObject

/** Encrypted export/import for local operational data and settings. */
class BackupManager(private val context: Context) {
    private val db = AuraDatabase.get(context)
    private val prefsName = "aura_settings_v3"

    suspend fun createBackup(password: String): File {
        require(password.length >= 8) { "Backup password must be at least 8 characters." }
        val dao = db.dao()
        val random = SecureRandom()
        val salt = ByteArray(16).also(random::nextBytes)
        val iv = ByteArray(12).also(random::nextBytes)

        val settings = JSONObject()
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).all.forEach { (key, value) ->
            when (value) {
                is Boolean, is String, is Int, is Long, is Float, is Double -> settings.put(key, value)
            }
        }

        val messages = JSONArray().apply {
            dao.getRecentMessages(10_000).asReversed().forEach { message ->
                put(JSONObject()
                    .put("role", message.role)
                    .put("content", message.content)
                    .put("conversationId", message.conversationId)
                    .put("timestamp", message.timestamp))
            }
        }
        val memories = JSONArray().apply {
            dao.memories().forEach { memory ->
                put(JSONObject().put("key", memory.key).put("value", memory.value).put("timestamp", memory.timestamp))
            }
        }
        val safety = JSONArray().apply {
            dao.safetyEvents().forEach { event ->
                put(JSONObject()
                    .put("type", event.type)
                    .put("reason", event.reason)
                    .put("packageName", event.packageName)
                    .put("action", event.action)
                    .put("timestamp", event.timestamp))
            }
        }

        val payload = JSONObject()
            .put("version", 2)
            .put("createdAt", System.currentTimeMillis())
            .put("messages", messages)
            .put("memories", memories)
            .put("safetyEvents", safety)
            .put("settings", settings)

        val encrypted = encrypt(payload.toString().toByteArray(StandardCharsets.UTF_8), password, salt, iv)
        val file = File(context.cacheDir, "aura_backup_${System.currentTimeMillis()}.aura")
        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.putNextEntry(ZipEntry("payload.bin"))
            zip.write(encrypted)
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("meta.json"))
            zip.write(
                JSONObject()
                    .put("version", 2)
                    .put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                    .put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
                    .toString()
                    .toByteArray(StandardCharsets.UTF_8)
            )
            zip.closeEntry()
        }
        return file
    }

    /** Decrypts and non-destructively merges an encrypted archive into current local state. */
    suspend fun restoreBackup(file: File, password: String): Result<Int> = runCatching {
        require(password.length >= 8) { "Backup password must be at least 8 characters." }
        require(file.isFile) { "Backup file not found." }

        val entries = unzip(file)
        val payloadBytes = entries["payload.bin"] ?: error("Backup payload is missing.")
        val meta = JSONObject(String(entries["meta.json"] ?: error("Backup metadata is missing."), StandardCharsets.UTF_8))
        val version = meta.optInt("version", 1)
        val salt: ByteArray
        val iv: ByteArray

        if (version >= 2) {
            salt = Base64.decode(meta.getString("salt"), Base64.DEFAULT)
            iv = Base64.decode(meta.getString("iv"), Base64.DEFAULT)
            require(salt.size == 16 && iv.size == 12) { "Invalid backup encryption metadata." }
        } else {
            salt = sha256(("AURA-X:" + context.packageName).toByteArray(StandardCharsets.UTF_8))
            iv = meta.getString("iv").split(',').map { it.toByte() }.toByteArray()
            require(iv.size == 12) { "Invalid legacy backup IV." }
        }

        val payload = JSONObject(String(decrypt(payloadBytes, password, salt, iv), StandardCharsets.UTF_8))
        val dao = db.dao()
        val currentMessages = dao.getRecentMessages(10_000)
        val currentMemories = dao.memories()
        val currentSafety = dao.safetyEvents()
        var imported = 0

        val settings = payload.optJSONObject("settings") ?: JSONObject()
        val editor = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
        val keys = settings.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = settings.opt(key)
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Double -> editor.putFloat(key, value.toFloat())
                is Number -> editor.putLong(key, value.toLong())
                is String -> editor.putString(key, value)
            }
        }
        editor.apply()

        val memoryArray = payload.optJSONArray("memories") ?: JSONArray()
        for (i in 0 until memoryArray.length()) {
            val item = memoryArray.getJSONObject(i)
            val key = item.optString("key")
            val value = item.optString("value")
            if (key.isBlank() || value.isBlank()) continue
            if (currentMemories.none { it.key == key && it.value == value }) {
                dao.addMemory(MemoryEntity(key = key, value = value, timestamp = item.optLong("timestamp", System.currentTimeMillis())))
                imported++
            }
        }

        val safetyArray = payload.optJSONArray("safetyEvents") ?: JSONArray()
        for (i in 0 until safetyArray.length()) {
            val item = safetyArray.getJSONObject(i)
            val type = item.optString("type")
            val reason = item.optString("reason")
            val action = item.optString("action").takeIf { it.isNotBlank() }
            val packageName = item.optString("packageName").takeIf { it.isNotBlank() }
            val timestamp = item.optLong("timestamp", System.currentTimeMillis())
            if (currentSafety.none {
                    it.type == type && it.reason == reason && it.action == action && it.packageName == packageName && it.timestamp == timestamp
                }) {
                dao.addSafety(SafetyEventEntity(type = type, reason = reason, packageName = packageName, action = action, timestamp = timestamp))
                imported++
            }
        }

        val messageArray = payload.optJSONArray("messages") ?: JSONArray()
        for (i in 0 until messageArray.length()) {
            val item = messageArray.getJSONObject(i)
            val role = item.optString("role")
            val content = item.optString("content")
            val conversationId = item.optLong("conversationId", 0L)
            val timestamp = item.optLong("timestamp", System.currentTimeMillis())
            if (role.isBlank() || content.isBlank()) continue
            if (currentMessages.none { it.role == role && it.content == content && it.timestamp == timestamp }) {
                dao.addMessage(MessageEntity(conversationId = conversationId, role = role, content = content, timestamp = timestamp))
                imported++
            }
        }

        imported
    }

    fun stageRestore(file: File, password: String): Result<File> = runCatching {
        require(file.exists()) { "Backup file not found." }
        require(password.length >= 8) { "Backup password must be at least 8 characters." }
        val staged = File(context.filesDir, "restore/pending.aura")
        staged.parentFile?.mkdirs()
        FileInputStream(file).use { input -> FileOutputStream(staged).use { output -> input.copyTo(output) } }
        staged
    }

    private fun encrypt(plain: ByteArray, password: String, salt: ByteArray, iv: ByteArray): ByteArray {
        val key = SecretKeySpec(sha256(password.toByteArray(StandardCharsets.UTF_8) + salt), "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(plain)
    }

    private fun decrypt(ciphertext: ByteArray, password: String, salt: ByteArray, iv: ByteArray): ByteArray {
        val key = SecretKeySpec(sha256(password.toByteArray(StandardCharsets.UTF_8) + salt), "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun unzip(file: File): Map<String, ByteArray> {
        val result = linkedMapOf<String, ByteArray>()
        ZipInputStream(FileInputStream(file)).use { zip ->
            var entry = zip.nextEntry
            val buffer = ByteArray(16 * 1024)
            while (entry != null) {
                if (!entry!!.isDirectory && (entry!!.name == "payload.bin" || entry!!.name == "meta.json")) {
                    val out = ByteArrayOutputStream()
                    while (true) {
                        val read = zip.read(buffer)
                        if (read <= 0) break
                        out.write(buffer, 0, read)
                    }
                    result[entry!!.name] = out.toByteArray()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return result
    }

    private fun sha256(value: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(value)
}
