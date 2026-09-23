package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChannelEntity
import com.example.data.model.ConfigEntity
import com.example.data.model.ConfigImportResult
import com.example.data.model.ConfigPayload
import com.example.data.model.VideoEntity
import com.example.data.repository.SharedConfigRegistry
import com.example.data.repository.YouTubeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ExploreCategory {
    VIDEO,
    CHANNEL
}

enum class ChannelContentType {
    VIDEOS,
    SHORTS
}

enum class OfflineFilter {
    ALL,
    VIDEOS,
    SHORTS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = YouTubeRepository(application.applicationContext)

    // Current Bottom Navigation Tab (0: Search/Add, 1: Offline Videos, 2: Channels)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Tab 1: Search & Add
    private val _exploreCategory = MutableStateFlow(ExploreCategory.VIDEO)
    val exploreCategory: StateFlow<ExploreCategory> = _exploreCategory.asStateFlow()

    private val _videoUrlInput = MutableStateFlow("")
    val videoUrlInput: StateFlow<String> = _videoUrlInput.asStateFlow()

    private val _channelQueryInput = MutableStateFlow("")
    val channelQueryInput: StateFlow<String> = _channelQueryInput.asStateFlow()

    private val _isVideoLoading = MutableStateFlow(false)
    val isVideoLoading: StateFlow<Boolean> = _isVideoLoading.asStateFlow()

    private val _isChannelLoading = MutableStateFlow(false)
    val isChannelLoading: StateFlow<Boolean> = _isChannelLoading.asStateFlow()

    private val _fetchedVideo = MutableStateFlow<VideoEntity?>(null)
    val fetchedVideo: StateFlow<VideoEntity?> = _fetchedVideo.asStateFlow()

    private val _fetchedChannel = MutableStateFlow<ChannelEntity?>(null)
    val fetchedChannel: StateFlow<ChannelEntity?> = _fetchedChannel.asStateFlow()

    private val _fetchedChannelVideos = MutableStateFlow<List<VideoEntity>>(emptyList())
    val fetchedChannelVideos: StateFlow<List<VideoEntity>> = _fetchedChannelVideos.asStateFlow()

    private val _channelContentType = MutableStateFlow(ChannelContentType.VIDEOS)
    val channelContentType: StateFlow<ChannelContentType> = _channelContentType.asStateFlow()

    // Download progresses: videoId -> Float (0f..1f)
    private val _downloadProgressMap = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgressMap: StateFlow<Map<String, Float>> = _downloadProgressMap.asStateFlow()

    // Tab 2: Offline Videos
    val offlineVideos: StateFlow<List<VideoEntity>> = repository.getDownloadedVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _offlineFilter = MutableStateFlow(OfflineFilter.ALL)
    val offlineFilter: StateFlow<OfflineFilter> = _offlineFilter.asStateFlow()

    // Tab 3: Saved Channels
    val savedChannels: StateFlow<List<ChannelEntity>> = repository.getAllSavedChannels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedChannelForDetail = MutableStateFlow<ChannelEntity?>(null)
    val selectedChannelForDetail: StateFlow<ChannelEntity?> = _selectedChannelForDetail.asStateFlow()

    private val _channelDetailVideos = MutableStateFlow<List<VideoEntity>>(emptyList())
    val channelDetailVideos: StateFlow<List<VideoEntity>> = _channelDetailVideos.asStateFlow()

    private val _channelDetailSubTab = MutableStateFlow(ChannelContentType.VIDEOS)
    val channelDetailSubTab: StateFlow<ChannelContentType> = _channelDetailSubTab.asStateFlow()

    // Active Video Player
    private val _activePlayerVideo = MutableStateFlow<VideoEntity?>(null)
    val activePlayerVideo: StateFlow<VideoEntity?> = _activePlayerVideo.asStateFlow()

    // UI Feedback
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.cleanInvalidOfflineFiles()
            repository.removeFakeInitialData()
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setExploreCategory(category: ExploreCategory) {
        _exploreCategory.value = category
    }

    fun setVideoUrlInput(url: String) {
        _videoUrlInput.value = url
    }

    fun setChannelQueryInput(query: String) {
        _channelQueryInput.value = query
    }

    fun setChannelContentType(type: ChannelContentType) {
        _channelContentType.value = type
    }

    fun setChannelDetailSubTab(type: ChannelContentType) {
        _channelDetailSubTab.value = type
    }

    fun setOfflineFilter(filter: OfflineFilter) {
        _offlineFilter.value = filter
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun searchVideo(urlOrId: String? = null) {
        val query = urlOrId ?: _videoUrlInput.value
        if (query.isBlank()) return
        viewModelScope.launch {
            _isVideoLoading.value = true
            try {
                val video = repository.fetchVideoInfo(query)
                _fetchedVideo.value = video
            } catch (e: Exception) {
                _uiMessage.value = "Ошибка при загрузке видео: ${e.message}"
            } finally {
                _isVideoLoading.value = false
            }
        }
    }

    fun searchChannel(queryOrHandle: String? = null) {
        val query = queryOrHandle ?: _channelQueryInput.value
        if (query.isBlank()) return
        viewModelScope.launch {
            _isChannelLoading.value = true
            try {
                val (channel, videos) = repository.fetchChannelInfo(query)
                _fetchedChannel.value = channel
                _fetchedChannelVideos.value = videos
            } catch (e: Exception) {
                _uiMessage.value = "Ошибка при поиске канала: ${e.message}"
            } finally {
                _isChannelLoading.value = false
            }
        }
    }

    fun saveFetchedChannel() {
        val channel = _fetchedChannel.value ?: return
        val videos = _fetchedChannelVideos.value
        viewModelScope.launch {
            repository.saveChannel(channel, videos)
            _fetchedChannel.value = channel.copy(isSaved = true)
            _uiMessage.value = "Канал \"${channel.name}\" сохранен во вкладку «Каналы»!"
        }
    }

    fun removeChannel(channelId: String) {
        viewModelScope.launch {
            repository.removeChannel(channelId)
            if (_selectedChannelForDetail.value?.id == channelId) {
                _selectedChannelForDetail.value = null
            }
            _uiMessage.value = "Канал удален из сохраненных"
        }
    }

    fun downloadVideoForOffline(video: VideoEntity) {
        viewModelScope.launch {
            val videoId = video.id
            _downloadProgressMap.value = _downloadProgressMap.value + (videoId to 0.05f)
            _uiMessage.value = "Скачивание в память приложения: ${video.title}…"

            val success = repository.downloadVideoForOffline(video) { progress ->
                _downloadProgressMap.value = _downloadProgressMap.value + (videoId to progress)
            }

            if (success) {
                _downloadProgressMap.value = _downloadProgressMap.value - videoId
                _uiMessage.value = "✓ Видео сохранено! Доступно без интернета во вкладке «Офлайн видео»"
                // Update local fetched video state if same
                if (_fetchedVideo.value?.id == videoId) {
                    _fetchedVideo.value = _fetchedVideo.value?.copy(isDownloaded = true)
                }
            } else {
                _downloadProgressMap.value = _downloadProgressMap.value - videoId
                _uiMessage.value = "Не удалось сохранить видео"
            }
        }
    }

    fun deleteOfflineVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.deleteOfflineVideo(video)
            if (_activePlayerVideo.value?.id == video.id) {
                _activePlayerVideo.value = null
            }
            if (_fetchedVideo.value?.id == video.id) {
                _fetchedVideo.value = _fetchedVideo.value?.copy(isDownloaded = false, localFilePath = null)
            }
            _uiMessage.value = "Видео удалено из памяти приложения"
        }
    }

    fun openPlayer(video: VideoEntity) {
        _activePlayerVideo.value = video
    }

    fun closePlayer() {
        _activePlayerVideo.value = null
    }

    fun openChannelDetail(channel: ChannelEntity) {
        _selectedChannelForDetail.value = channel
        viewModelScope.launch {
            repository.getVideosForChannel(channel.id, channel.name).collect { vids ->
                _channelDetailVideos.value = vids
            }
        }
    }

    fun closeChannelDetail() {
        _selectedChannelForDetail.value = null
    }

    fun syncChannel(channelId: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncChannel(channelId)
            _isSyncing.value = false
            _uiMessage.value = result.message
        }
    }

    // Instructions Dialog (Photos from YouTube tutorial)
    private val _isInstructionsVisible = MutableStateFlow(false)
    val isInstructionsVisible: StateFlow<Boolean> = _isInstructionsVisible.asStateFlow()

    fun showInstructions() { _isInstructionsVisible.value = true }
    fun hideInstructions() { _isInstructionsVisible.value = false }

    // Tab 4: Configs
    val configs: StateFlow<List<ConfigEntity>> = repository.getAllConfigs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _configLinkInput = MutableStateFlow("")
    val configLinkInput: StateFlow<String> = _configLinkInput.asStateFlow()

    private val _isCreateConfigDialogVisible = MutableStateFlow(false)
    val isCreateConfigDialogVisible: StateFlow<Boolean> = _isCreateConfigDialogVisible.asStateFlow()

    private val _configImportPreview = MutableStateFlow<ConfigPayload?>(null)
    val configImportPreview: StateFlow<ConfigPayload?> = _configImportPreview.asStateFlow()

    private val _configErrorMessage = MutableStateFlow<String?>(null)
    val configErrorMessage: StateFlow<String?> = _configErrorMessage.asStateFlow()

    private val _configErrorTitle = MutableStateFlow<String>("Ошибка конфига")
    val configErrorTitle: StateFlow<String> = _configErrorTitle.asStateFlow()

    fun setConfigLinkInput(value: String) { _configLinkInput.value = value }
    fun showCreateConfigDialog() { _isCreateConfigDialogVisible.value = true }
    fun hideCreateConfigDialog() { _isCreateConfigDialogVisible.value = false }
    fun dismissConfigPreview() { _configImportPreview.value = null }
    fun dismissConfigError() {
        _configErrorMessage.value = null
        _configErrorTitle.value = "Ошибка конфига"
    }

    fun createConfig(name: String, selectedVideos: List<VideoEntity>, selectedChannels: List<ChannelEntity>) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiMessage.value = "Введите название конфига"
                return@launch
            }
            if (selectedVideos.isEmpty() && selectedChannels.isEmpty()) {
                _uiMessage.value = "Выберите хотя бы одно видео или канал"
                return@launch
            }
            val cfg = repository.createConfig(name, selectedVideos, selectedChannels)
            _isCreateConfigDialogVisible.value = false
            _uiMessage.value = "✓ Конфиг «${cfg.name}» создан! Ссылка: ${cfg.link}"
        }
    }

    fun resolveAndPreviewConfig(rawLink: String) {
        val trimmed = rawLink.trim()
        if (trimmed.isBlank()) {
            _configErrorTitle.value = "Пустая ссылка"
            _configErrorMessage.value = "Пожалуйста, введите код или ссылку конфига (например, offline.xxxxxxxxxx)"
            return
        }

        // Clean to pure code
        val cleanCode = SharedConfigRegistry.extractCode(trimmed)

        // Check if user already has this config locally
        val existing = configs.value.find { cfg ->
            cfg.code.equals(cleanCode, ignoreCase = true) ||
            cfg.link.equals(trimmed, ignoreCase = true) ||
            cfg.link.contains(cleanCode, ignoreCase = true)
        }

        if (existing != null) {
            if (existing.isCreatedByMe) {
                _configErrorTitle.value = "Вы создатель конфига"
                _configErrorMessage.value = "Вы сами создали этот конфиг («${existing.name}»)! Он уже находится в вашем списке."
            } else {
                _configErrorTitle.value = "Конфиг уже добавлен"
                _configErrorMessage.value = "Конфиг «${existing.name}» (offline.${existing.code}) уже добавлен в ваш список! Добавить его повторно нельзя."
            }
            return
        }

        viewModelScope.launch {
            val res = repository.resolveConfigCode(trimmed)
            when (res) {
                is ConfigImportResult.Success -> {
                    // Double check by ID
                    val alreadyHasId = configs.value.find { it.id == res.payload.id || it.code.equals(res.payload.code, ignoreCase = true) }
                    if (alreadyHasId != null) {
                        if (alreadyHasId.isCreatedByMe) {
                            _configErrorTitle.value = "Вы создатель конфига"
                            _configErrorMessage.value = "Вы сами создали этот конфиг («${alreadyHasId.name}»)! Он уже в вашем списке."
                        } else {
                            _configErrorTitle.value = "Конфиг уже добавлен"
                            _configErrorMessage.value = "Конфиг «${alreadyHasId.name}» уже добавлен в ваш список!"
                        }
                        return@launch
                    }
                    _configImportPreview.value = res.payload
                }
                is ConfigImportResult.DeletedByCreator -> {
                    _configErrorTitle.value = "Конфиг аннулирован"
                    _configErrorMessage.value = res.message
                }
                is ConfigImportResult.NotFound -> {
                    _configErrorTitle.value = "Конфиг не найден"
                    _configErrorMessage.value = res.message
                }
                is ConfigImportResult.Error -> {
                    _configErrorTitle.value = "Ошибка ссылки"
                    _configErrorMessage.value = res.message
                }
            }
        }
    }

    fun confirmImportConfig(payload: ConfigPayload) {
        val existing = configs.value.find { it.id == payload.id || it.code.equals(payload.code, ignoreCase = true) }
        if (existing != null) {
            _configImportPreview.value = null
            _configErrorTitle.value = "Конфиг уже добавлен"
            _configErrorMessage.value = "Конфиг «${existing.name}» уже есть в вашем списке."
            return
        }
        viewModelScope.launch {
            repository.importAndSaveConfig(payload)
            _configImportPreview.value = null
            _configLinkInput.value = ""
            _uiMessage.value = "✓ Конфиг «${payload.name}» сохранен! Каналы и видео добавлены."
        }
    }

    fun removeImportedConfig(configId: String) {
        viewModelScope.launch {
            repository.deleteConfig(configId)
            _uiMessage.value = "Конфиг убран из вашего списка"
        }
    }

    fun deleteConfigPermanently(configId: String) {
        viewModelScope.launch {
            repository.deleteConfig(configId)
            _uiMessage.value = "Конфиг удален навсегда и аннулирован для всех"
        }
    }

    fun syncAllChannels() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncAllChannels()
            _isSyncing.value = false
            _uiMessage.value = result.message
        }
    }
}
