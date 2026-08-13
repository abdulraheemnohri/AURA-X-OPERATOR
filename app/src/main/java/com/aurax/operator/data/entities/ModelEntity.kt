package com.aurax.operator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val displayName: String,
    val category: String,
    val format: String,
    val quantization: String,
    val sourceUrl: String,
    val localPath: String? = null,
    val sizeBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val sha256: String = "",
    val contextLength: Int = 2048,
    val parameters: String = "0.5B",
    val status: String = "AVAILABLE",
    val isLoaded: Boolean = false,
    val lastUsed: Long = 0L,
    val benchmarkTokensPerSec: Float? = null,
    val userRating: Int? = null,
    val tags: String = "",
    val description: String = "",
    val license: String = "",
    val minRamMB: Int = 512,
    val recommendedRamMB: Int = 1024,
    val isBuiltIn: Boolean = false,
    val isUserImported: Boolean = false,
    val importDate: Long? = null
)
