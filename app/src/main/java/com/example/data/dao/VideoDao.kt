package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE isDownloaded = 1 ORDER BY savedAtTimestamp DESC")
    fun getDownloadedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos ORDER BY savedAtTimestamp DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    fun getVideoById(id: String): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE channelId = :channelId OR channelName = :channelName ORDER BY savedAtTimestamp DESC")
    fun getVideosByChannel(channelId: String, channelName: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Delete
    suspend fun deleteVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: String)

    @Query("DELETE FROM videos WHERE channelId = :channelId OR channelName = :channelName")
    suspend fun deleteVideosByChannel(channelId: String, channelName: String)

    @Query("DELETE FROM videos WHERE configId = :configId")
    suspend fun deleteVideosByConfigId(configId: String)

    @Query("SELECT * FROM videos WHERE configId = :configId")
    suspend fun getVideosByConfigId(configId: String): List<VideoEntity>

    @Query("UPDATE videos SET isDownloaded = :isDownloaded, localFilePath = :localPath, fileSizeBytes = :size, savedAtTimestamp = :savedAt WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean, localPath: String?, size: Long, savedAt: Long = System.currentTimeMillis())
}
