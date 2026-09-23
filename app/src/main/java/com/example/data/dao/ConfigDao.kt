package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao {
    @Query("SELECT * FROM configs ORDER BY createdAt DESC")
    fun getAllConfigs(): Flow<List<ConfigEntity>>

    @Query("SELECT * FROM configs WHERE id = :id LIMIT 1")
    suspend fun getConfigById(id: String): ConfigEntity?

    @Query("SELECT * FROM configs WHERE code = :code LIMIT 1")
    suspend fun getConfigByCode(code: String): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ConfigEntity)

    @Update
    suspend fun updateConfig(config: ConfigEntity)

    @Delete
    suspend fun deleteConfig(config: ConfigEntity)

    @Query("DELETE FROM configs WHERE id = :id")
    suspend fun deleteConfigById(id: String)

    @Query("UPDATE configs SET isDeletedByCreator = 1 WHERE id = :id")
    suspend fun markDeletedByCreator(id: String)
}
