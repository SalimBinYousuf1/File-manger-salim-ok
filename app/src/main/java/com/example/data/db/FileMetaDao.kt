package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FileMetaDao {
    @Query("SELECT * FROM file_metadata")
    fun getAllMetas(): Flow<List<FileMetaEntity>>

    @Query("SELECT * FROM file_metadata WHERE path = :path LIMIT 1")
    suspend fun getMetaByPath(path: String): FileMetaEntity?

    @Query("SELECT * FROM file_metadata WHERE isFavorite = 1 AND isInTrash = 0")
    fun getFavorites(): Flow<List<FileMetaEntity>>

    @Query("SELECT * FROM file_metadata WHERE isInTrash = 1 ORDER BY deletedAt DESC")
    fun getTrashItems(): Flow<List<FileMetaEntity>>

    @Query("SELECT * FROM file_metadata WHERE lastOpenedAt > 0 AND isInTrash = 0 ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun getRecentlyOpened(limit: Int = 30): Flow<List<FileMetaEntity>>

    @Query("SELECT * FROM file_metadata WHERE tagColorHex IS NOT NULL AND isInTrash = 0")
    fun getTaggedFiles(): Flow<List<FileMetaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(meta: FileMetaEntity)

    @Query("DELETE FROM file_metadata WHERE path = :path")
    suspend fun delete(path: String)

    @Query("DELETE FROM file_metadata WHERE isInTrash = 1")
    suspend fun emptyTrash()
}
