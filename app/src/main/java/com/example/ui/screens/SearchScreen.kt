package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChannelEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.formatBytes
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import com.example.ui.viewmodel.ChannelContentType
import com.example.ui.viewmodel.ExploreCategory
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val category by viewModel.exploreCategory.collectAsState()
    val videoInput by viewModel.videoUrlInput.collectAsState()
    val channelInput by viewModel.channelQueryInput.collectAsState()
    val isVideoLoading by viewModel.isVideoLoading.collectAsState()
    val isChannelLoading by viewModel.isChannelLoading.collectAsState()
    val fetchedVideo by viewModel.fetchedVideo.collectAsState()
    val fetchedChannel by viewModel.fetchedChannel.collectAsState()
    val fetchedChannelVideos by viewModel.fetchedChannelVideos.collectAsState()
    val channelContentType by viewModel.channelContentType.collectAsState()
    val downloadProgressMap by viewModel.downloadProgressMap.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // White YouTube Logo Badge
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp, 28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = BlackBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TubeSync",
                            color = PureWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Офлайн-плеер и синхронизация",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // White Instruction Tutorial Button
                Button(
                    onClick = { viewModel.showInstructions() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = BlackBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("open_instructions_button")
                ) {
                    Text(
                        text = "📖 Инструкция",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Category Switcher: [ Видео ] vs [ Канал ]
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Video Button
                Surface(
                    color = if (category == ExploreCategory.VIDEO) PureWhite else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("category_video_tab")
                        .clickable { viewModel.setExploreCategory(ExploreCategory.VIDEO) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = null,
                            tint = if (category == ExploreCategory.VIDEO) BlackBackground else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Видео",
                            color = if (category == ExploreCategory.VIDEO) BlackBackground else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Channel Button
                Surface(
                    color = if (category == ExploreCategory.CHANNEL) PureWhite else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("category_channel_tab")
                        .clickable { viewModel.setExploreCategory(ExploreCategory.CHANNEL) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subscriptions,
                            contentDescription = null,
                            tint = if (category == ExploreCategory.CHANNEL) BlackBackground else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Канал",
                            color = if (category == ExploreCategory.CHANNEL) BlackBackground else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ==================== CATEGORY: VIDEO ====================
        if (category == ExploreCategory.VIDEO) {
            item {
                Text(
                    text = "Введите ссылку на YouTube видео или Shorts:",
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Input Field
                OutlinedTextField(
                    value = videoInput,
                    onValueChange = { viewModel.setVideoUrlInput(it) },
                    placeholder = {
                        Text(
                            text = "https://youtube.com/watch?v=... или Shorts",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        if (videoInput.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setVideoUrlInput("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Очистить",
                                    tint = PureWhite
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_url_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search/Fetch White Button
                Button(
                    onClick = { viewModel.searchVideo() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = BlackBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("search_video_button")
                ) {
                    if (isVideoLoading) {
                        CircularProgressIndicator(
                            color = BlackBackground,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Загрузка…", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = BlackBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Найти и загрузить видео",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick presets
                Text(
                    text = "Быстрый выбор для теста:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(
                        "⚡ Shorts: Лайфхак за 30с" to "https://www.youtube.com/shorts/sample_short_1",
                        "🎬 Обзор технологий 2026" to "https://www.youtube.com/watch?v=tech_review_2026",
                        "⚡ Shorts: Смешной момент" to "https://www.youtube.com/shorts/sample_short_2",
                        "🌌 Космос и Вселенная 4K" to "https://www.youtube.com/watch?v=space_documentary"
                    )
                    items(presets) { (label, url) ->
                        Surface(
                            color = DarkCard,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.clickable {
                                viewModel.setVideoUrlInput(url)
                                viewModel.searchVideo(url)
                            }
                        ) {
                            Text(
                                text = label,
                                color = OffWhite,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Fetched Video Preview Card
            fetchedVideo?.let { video ->
                item {
                    val downloadProgress = downloadProgressMap[video.id]
                    VideoPreviewCard(
                        video = video,
                        downloadProgress = downloadProgress,
                        onWatch = { viewModel.openPlayer(video) },
                        onDownload = { viewModel.downloadVideoForOffline(video) },
                        onDelete = { viewModel.deleteOfflineVideo(video) }
                    )
                }
            } ?: item {
                // If nothing searched yet, show helper message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Вставьте ссылку выше или выберите пример",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Видео можно сохранить для просмотра без интернета",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // ==================== CATEGORY: CHANNEL ====================
        if (category == ExploreCategory.CHANNEL) {
            item {
                Text(
                    text = "Введите ссылку на канал YouTube или @хэндл:",
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Channel Input Field
                OutlinedTextField(
                    value = channelInput,
                    onValueChange = { viewModel.setChannelQueryInput(it) },
                    placeholder = {
                        Text(
                            text = "@AndroidDevelopers или ссылка на канал",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        if (channelInput.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setChannelQueryInput("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Очистить",
                                    tint = PureWhite
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("channel_query_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Channel White Button
                Button(
                    onClick = { viewModel.searchChannel() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = BlackBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("search_channel_button")
                ) {
                    if (isChannelLoading) {
                        CircularProgressIndicator(
                            color = BlackBackground,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Поиск канала…", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = BlackBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Найти и открыть канал",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick preset channels
                Text(
                    text = "Популярные каналы для теста:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val sampleChannels = listOf(
                        "@AndroidDevelopers",
                        "@MrBeast",
                        "@Veritasium",
                        "@NASA",
                        "@TED"
                    )
                    items(sampleChannels) { handle ->
                        Surface(
                            color = DarkCard,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.clickable {
                                viewModel.setChannelQueryInput(handle)
                                viewModel.searchChannel(handle)
                            }
                        ) {
                            Text(
                                text = handle,
                                color = OffWhite,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Fetched Channel Profile & Content
            fetchedChannel?.let { channel ->
                // Channel Header Card
                item {
                    ChannelProfileCard(
                        channel = channel,
                        onSaveChannel = { viewModel.saveFetchedChannel() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Sub-tabs: [ Все видео ] vs [ Shorts ]
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = if (channelContentType == ChannelContentType.VIDEOS) PureWhite else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setChannelContentType(ChannelContentType.VIDEOS) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Все видео (${fetchedChannelVideos.count { !it.isShort }})",
                                    color = if (channelContentType == ChannelContentType.VIDEOS) BlackBackground else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Surface(
                            color = if (channelContentType == ChannelContentType.SHORTS) PureWhite else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setChannelContentType(ChannelContentType.SHORTS) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ Shorts (${fetchedChannelVideos.count { it.isShort }})",
                                    color = if (channelContentType == ChannelContentType.SHORTS) BlackBackground else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Channel Videos List
                val displayedVideos = fetchedChannelVideos.filter {
                    if (channelContentType == ChannelContentType.VIDEOS) !it.isShort else it.isShort
                }

                items(displayedVideos, key = { it.id }) { vid ->
                    val progress = downloadProgressMap[vid.id]
                    ChannelVideoItem(
                        video = vid,
                        downloadProgress = progress,
                        onWatch = { viewModel.openPlayer(vid) },
                        onDownload = { viewModel.downloadVideoForOffline(vid) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            } ?: item {
                // Empty channel state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Subscriptions,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Введите канал или выберите из списка выше",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Канал можно сохранить во вкладку 3 для авто-синхронизации",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPreviewCard(
    video: VideoEntity,
    downloadProgress: Float?,
    onWatch: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_preview_card"),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column {
            // Thumbnail with Duration & Shorts tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                )

                // Shorts or Video badge
                if (video.isShort) {
                    Surface(
                        color = YouTubeRed,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚡ SHORTS",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Duration Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = video.duration,
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Downloaded badge
                if (video.isDownloaded) {
                    Surface(
                        color = GreenSuccess,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BlackBackground,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ОФЛАЙН",
                                color = BlackBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = video.title,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = video.channelName,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = "•", color = TextMuted)
                    Text(
                        text = "${video.views} просмотров",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Download progress bar
                if (downloadProgress != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Скачивание в память устройства…",
                                color = PureWhite,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${(downloadProgress * 100).toInt()}%",
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            color = PureWhite,
                            trackColor = DarkSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: White Buttons!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Watch button
                    Button(
                        onClick = onWatch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = BlackBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("watch_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = BlackBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Смотреть",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Save / Download for offline
                    if (!video.isDownloaded) {
                        Button(
                            onClick = onDownload,
                            enabled = downloadProgress == null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkCardElevated,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("download_offline_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Скачать офлайн",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Already downloaded badge button
                        Surface(
                            color = DarkCardElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownloadDone,
                                    contentDescription = null,
                                    tint = GreenSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "В офлайне (Вкл. 2)",
                                    color = GreenSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelProfileCard(
    channel: ChannelEntity,
    onSaveChannel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column {
            // Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(DarkSurface)
            ) {
                AsyncImage(
                    model = channel.bannerUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Avatar & Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                AsyncImage(
                    model = channel.avatarUrl,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(2.dp, PureWhite, CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.name,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = channel.handle,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = channel.subscribers,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Save Channel Button: White Button!
                Button(
                    onClick = onSaveChannel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (channel.isSaved) DarkSurface else PureWhite,
                        contentColor = if (channel.isSaved) GreenSuccess else BlackBackground
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = if (channel.isSaved) androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess) else null,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("save_channel_button")
                ) {
                    Icon(
                        imageVector = if (channel.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (channel.isSaved) "Сохранен" else "Сохранить",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelVideoItem(
    video: VideoEntity,
    downloadProgress: Float?,
    onWatch: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = if (video.isShort) 90.dp else 65.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Duration
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = video.duration,
                        color = PureWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = PureWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${video.views} • ${video.publishedAt}",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                if (downloadProgress != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        color = PureWhite,
                        trackColor = DarkSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Actions: Watch & Download
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Play Icon Button (White)
                IconButton(
                    onClick = onWatch,
                    modifier = Modifier
                        .size(36.dp)
                        .background(PureWhite, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Смотреть",
                        tint = BlackBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Download Icon Button
                IconButton(
                    onClick = onDownload,
                    enabled = downloadProgress == null && !video.isDownloaded,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (video.isDownloaded) GreenSuccess.copy(alpha = 0.2f) else DarkSurface,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (video.isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                        contentDescription = "Скачать",
                        tint = if (video.isDownloaded) GreenSuccess else PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
