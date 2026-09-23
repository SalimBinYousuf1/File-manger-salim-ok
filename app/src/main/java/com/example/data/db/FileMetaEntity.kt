package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file_metadata")
data class FileMetaEntity(
    @PrimaryKey val path: String,
    val isFavorite: Boolean = false,
    val tagColorHex: String? = null,
    val tagName: String? = null,
    val lastOpenedAt: Long = 0L,
    val isInTrash: Boolean = false,
    val deletedAt: Long? = null
)
