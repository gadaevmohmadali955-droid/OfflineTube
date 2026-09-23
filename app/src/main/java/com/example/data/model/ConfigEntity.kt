package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configs")
data class ConfigEntity(
    @PrimaryKey
    val id: String, // e.g. "cfg_1727092300"
    val name: String, // e.g. "Моя подборка видео и каналов"
    val code: String, // 10-char random code, e.g. "afdvdkwo1h"
    val link: String, // e.g. "offline.afdvdkwo1h"
    val videoIdsJson: String = "[]", // JSON array of video IDs
    val channelIdsJson: String = "[]", // JSON array of channel IDs
    val videoCount: Int = 0,
    val channelCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isCreatedByMe: Boolean = true,
    val isDeletedByCreator: Boolean = false
)
