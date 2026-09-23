package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val bannerUrl: String = "",
    val subscribers: String = "1.2M",
    val videoCount: Int = 120,
    val description: String = "",
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val isSaved: Boolean = true,
    val configId: String? = null,
    val configName: String? = null
)
