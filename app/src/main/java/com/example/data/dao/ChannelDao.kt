package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE isSaved = 1 ORDER BY lastSyncedAt DESC")
    fun getAllSavedChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    fun getChannelById(id: String): Flow<ChannelEntity?>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelByIdDirect(id: String): ChannelEntity?

    @Query("SELECT * FROM channels WHERE handle = :handle LIMIT 1")
    fun getChannelByHandle(handle: String): Flow<ChannelEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels WHERE id = :id")
    suspend fun deleteChannelById(id: String)

    @Query("DELETE FROM channels WHERE configId = :configId")
    suspend fun deleteChannelsByConfigId(configId: String)

    @Query("UPDATE channels SET lastSyncedAt = :timestamp WHERE id = :id")
    suspend fun updateLastSynced(id: String, timestamp: Long)
}
