package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val channelName: String,
    val channelId: String = "",
    val channelThumbnail: String = "",
    val videoUrl: String,
    val thumbnailUrl: String,
    val duration: String = "03:45",
    val views: String = "100K",
    val publishedAt: String = "Недавно",
    val isShort: Boolean = false,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val fileSizeBytes: Long = 0L,
    val savedAtTimestamp: Long = System.currentTimeMillis(),
    val configId: String? = null,
    val configName: String? = null
)
