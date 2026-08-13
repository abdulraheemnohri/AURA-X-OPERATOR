package com.aurax.operator.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 2 formalizes the persisted schema without changing columns.
            }
        }

        @Volatile private var INSTANCE: AuraDatabase? = null

        fun get(context: Context): AuraDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AuraDatabase::class.java,
                "aura.db"
            )
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
    @Insert suspend fun addMessage(e: MessageEntity): Long
    @Insert suspend fun addTask(e: TaskEntity): Long
    @Insert suspend fun addMemory(e: MemoryEntity)
    @Update suspend fun updateTask(e: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Query("UPDATE tasks SET status = :status, log = :log WHERE id = :taskId")
    suspend fun setTaskStatus(taskId: Long, status: String, log: String)

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun tasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<MessageEntity>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentMessages(limit: Int): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages")
    suspend fun clearMessages()

    @Query("DELETE FROM memories")
    suspend fun clearMemories()

    @Query("SELECT * FROM safety_events ORDER BY timestamp DESC")
    suspend fun safetyEvents(): List<SafetyEventEntity>

    @Query("SELECT * FROM safety_events ORDER BY timestamp DESC")
    fun observeSafetyEvents(): Flow<List<SafetyEventEntity>>

    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    suspend fun memories(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE key LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT :limit")
    suspend fun searchMemories(query: String, limit: Int): List<MemoryEntity>
}
