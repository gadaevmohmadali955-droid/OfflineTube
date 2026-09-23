package com.example.data.model

data class ConfigPayload(
    val id: String,
    val name: String,
    val code: String,
    val link: String,
    val videos: List<VideoEntity>,
    val channels: List<ChannelEntity>,
    val createdBy: String = "User",
    val createdAt: Long = System.currentTimeMillis()
)

sealed class ConfigImportResult {
    data class Success(val payload: ConfigPayload) : ConfigImportResult()
    data class DeletedByCreator(val message: String) : ConfigImportResult()
    data class NotFound(val message: String) : ConfigImportResult()
    data class Error(val message: String) : ConfigImportResult()
}
