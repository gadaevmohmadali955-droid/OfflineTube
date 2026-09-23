package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChannelEntity
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import com.example.ui.viewmodel.ExploreCategory
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ChannelsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val savedChannels by viewModel.savedChannels.collectAsState()
    val selectedChannel by viewModel.selectedChannelForDetail.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    // If channel is selected, show Channel Detail Screen
    if (selectedChannel != null) {
        ChannelDetailScreen(
            channel = selectedChannel!!,
            viewModel = viewModel,
            onBack = { viewModel.closeChannelDetail() },
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Сохраненные каналы",
                        color = PureWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Синхронизация и просмотр офлайн (${savedChannels.size})",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Sync All Button (White button)
                Button(
                    onClick = { viewModel.syncAllChannels() },
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = BlackBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("sync_all_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = BlackBackground,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = BlackBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Обновить",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Empty state
        if (savedChannels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp)
                        .background(DarkSurface, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = DarkCard,
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Subscriptions,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Каналы еще не сохранены",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Найдите канал во вкладке 1 («Поиск / Добавить») и нажмите «Сохранить канал»",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // White button
                        Button(
                            onClick = {
                                viewModel.setExploreCategory(ExploreCategory.CHANNEL)
                                viewModel.selectTab(0)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = BlackBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = BlackBackground,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Найти канал",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            val standaloneChannels = savedChannels.filter { it.configName.isNullOrBlank() }
            val configGroups = savedChannels.filter { !it.configName.isNullOrBlank() }.groupBy { it.configName!! }

            if (standaloneChannels.isNotEmpty()) {
                if (configGroups.isNotEmpty()) {
                    item {
                        ConfigSectionHeader(title = "Мои каналы")
                    }
                }
                items(standaloneChannels, key = { it.id }) { channel ->
                    SavedChannelItem(
                        channel = channel,
                        onOpen = { viewModel.openChannelDetail(channel) },
                        onSync = { viewModel.syncChannel(channel.id) },
                        onRemove = { viewModel.removeChannel(channel.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            configGroups.forEach { (cfgName, chans) ->
                item {
                    ConfigSectionHeader(title = "Конфиг: $cfgName")
                }
                items(chans, key = { it.id }) { channel ->
                    SavedChannelItem(
                        channel = channel,
                        onOpen = { viewModel.openChannelDetail(channel) },
                        onSync = { viewModel.syncChannel(channel.id) },
                        onRemove = { viewModel.removeChannel(channel.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun SavedChannelItem(
    channel: ChannelEntity,
    onOpen: () -> Unit,
    onSync: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("saved_channel_${channel.id}")
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Avatar Icon
            AsyncImage(
                model = channel.avatarUrl,
                contentDescription = channel.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, PureWhite, CircleShape)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = channel.handle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = channel.subscribers,
                            color = PureWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• Нажмите для видео/shorts",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Sync icon & Delete icon & Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSync,
                    modifier = Modifier
                        .size(36.dp)
                        .background(DarkSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Синхронизировать",
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(36.dp)
                        .background(DarkSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Удалить канал",
                        tint = YouTubeRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

