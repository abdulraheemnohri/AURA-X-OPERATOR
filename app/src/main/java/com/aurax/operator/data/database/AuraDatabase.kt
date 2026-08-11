package com.aurax.operator.data.database

import android.content.Context
import androidx.room.*
import com.aurax.operator.data.entities.*

@Database(
    entities = [
        OperatorActionEntity::class,
        SafetyEventEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun dao(): AuraDao

    companion object {
        @Volatile private var INSTANCE: AuraDatabase? = null
        fun get(c: Context): AuraDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(c.applicationContext, AuraDatabase::class.java, "aura.db")
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}

@Dao
interface AuraDao {
    @Insert suspend fun addAction(e: OperatorActionEntity)
    @Insert suspend fun addSafety(e: SafetyEventEntity)
    @Insert suspend fun addMessage(e: MessageEntity)
    @Insert suspend fun addTask(e: TaskEntity): Long
    @Insert suspend fun addMemory(e: MemoryEntity)
    @Update suspend fun updateTask(e: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun tasks(): List<TaskEntity>

    @Query("SELECT * FROM safety_events ORDER BY timestamp DESC")
    suspend fun safetyEvents(): List<SafetyEventEntity>

    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    suspend fun memories(): List<MemoryEntity>
}
