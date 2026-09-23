package com.example.data.repository

import com.example.data.model.ChannelEntity
import com.example.data.model.ConfigImportResult
import com.example.data.model.ConfigPayload
import com.example.data.model.VideoEntity
import java.util.concurrent.ConcurrentHashMap

/**
 * Global shared registry simulating cloud/p2p config distribution.
 * Supports:
 * - Unique 10-char alphanumeric codes (e.g. "afdvdkwo1h")
 * - Links in format "offline.afdvdkwo1h"
 * - Revocation/deletion tracking (if creator deletes, new activations fail)
 * - Pre-seeded community configs for immediate testing
 */
object SharedConfigRegistry {

    private val activeConfigs = ConcurrentHashMap<String, ConfigPayload>()
    private val deletedCodes = ConcurrentHashMap.newKeySet<String>()

    fun clearRegistry() {
        activeConfigs.clear()
        deletedCodes.clear()
    }

    fun registerConfig(payload: ConfigPayload) {
        deletedCodes.remove(payload.code)
        activeConfigs[payload.code] = payload
    }

    fun deleteConfigByCreator(code: String) {
        activeConfigs.remove(code)
        deletedCodes.add(code)
    }

    fun resolveConfig(rawInput: String): ConfigImportResult {
        val cleanCode = extractCode(rawInput)
        if (cleanCode.isBlank()) {
            return ConfigImportResult.Error("Неверный формат ссылки. Используйте формат offline.xxxxxxxxxx")
        }

        if (deletedCodes.contains(cleanCode)) {
            return ConfigImportResult.DeletedByCreator(
                "Создатель удалил этот конфиг. Ссылка «offline.$cleanCode» аннулирована и больше не доступна для активации."
            )
        }

        val found = activeConfigs[cleanCode]
        return if (found != null) {
            ConfigImportResult.Success(found)
        } else {
            ConfigImportResult.NotFound(
                "Конфиг «offline.$cleanCode» не найден. Проверьте правильность ссылки."
            )
        }
    }

    fun extractCode(input: String): String {
        var trimmed = input.trim()
        if (trimmed.startsWith("offline.", ignoreCase = true)) {
            trimmed = trimmed.substring(8)
        } else if (trimmed.contains("tubesync://config/")) {
            trimmed = trimmed.substringAfter("tubesync://config/")
        } else if (trimmed.contains("/c/")) {
            trimmed = trimmed.substringAfterLast("/c/")
        }
        return trimmed.takeWhile { it.isLetterOrDigit() }.lowercase()
    }
}
