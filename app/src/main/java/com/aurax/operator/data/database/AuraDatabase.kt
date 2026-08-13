package com.aurax.operator.data.database

import androidx.room.*
import android.content.Context
import com.aurax.operator.data.entities.*
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        OperatorActionEntity::class,
        SafetyEventEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        TaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun dao(): AuraDao

    companion object {
        @Volatile private var INSTANCE: AuraDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 2 formalizes the migration path without changing the schema.
                // Keeping this explicit prevents accidental destructive data loss.
            }
        }

        fun get(c: Context): AuraDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(c.applicationContext, AuraDatabase::class.java, "aura.db")
                .addMigrations(MIGRATION_1_2)
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

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun tasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM safety_events ORDER BY timestamp DESC")
    suspend fun safetyEvents(): List<SafetyEventEntity>

    @Query("SELECT * FROM safety_events ORDER BY timestamp DESC")
    fun observeSafetyEvents(): Flow<List<SafetyEventEntity>>

    @Query("SELECT * FROM memories ORDER BY timestamp DESC LIMIT 50")
    suspend fun memories(): List<MemoryEntity>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>>
}
