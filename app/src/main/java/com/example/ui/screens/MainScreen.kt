package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InstructionsDialog
import com.example.ui.components.OfflineVideoPlayer
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val offlineVideos by viewModel.offlineVideos.collectAsState()
    val savedChannels by viewModel.savedChannels.collectAsState()
    val configs by viewModel.configs.collectAsState()
    val activeVideo by viewModel.activePlayerVideo.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val isInstructionsVisible by viewModel.isInstructionsVisible.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BlackBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = DarkCard,
                    contentColor = PureWhite,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .border(1.dp, PureWhite.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = data.visuals.message,
                        color = PureWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = DarkBorder,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .height(72.dp)
                    .testTag("bottom_navigation_bar")
            ) {
                // Tab 1: Search / Add
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Search else Icons.Outlined.Search,
                            contentDescription = "Поиск и добавление"
                        )
                    },
                    label = {
                        Text(
                            text = "Поиск",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BlackBackground,
                        selectedTextColor = PureWhite,
                        indicatorColor = PureWhite,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_search")
                )

                // Tab 2: Offline Videos
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (offlineVideos.isNotEmpty()) {
                                    Badge(
                                        containerColor = PureWhite,
                                        contentColor = BlackBackground
                                    ) {
                                        Text(
                                            text = offlineVideos.size.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.OfflinePin else Icons.Filled.DownloadDone,
                                contentDescription = "Офлайн видео"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Видео",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BlackBackground,
                        selectedTextColor = PureWhite,
                        indicatorColor = PureWhite,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_offline")
                )

                // Tab 3: Saved Channels
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (savedChannels.isNotEmpty()) {
                                    Badge(
                                        containerColor = PureWhite,
                                        contentColor = BlackBackground
                                    ) {
                                        Text(
                                            text = savedChannels.size.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Filled.Subscriptions else Icons.Outlined.Subscriptions,
                                contentDescription = "Каналы"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Каналы",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BlackBackground,
                        selectedTextColor = PureWhite,
                        indicatorColor = PureWhite,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_channels")
                )

                // Tab 4: Configs (New!)
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (configs.isNotEmpty()) {
                                    Badge(
                                        containerColor = PureWhite,
                                        contentColor = BlackBackground
                                    ) {
                                        Text(
                                            text = configs.size.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 3) Icons.Filled.Tune else Icons.Outlined.Tune,
                                contentDescription = "Конфиги"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Конфиги",
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BlackBackground,
                        selectedTextColor = PureWhite,
                        indicatorColor = PureWhite,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_configs")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BlackBackground)
        ) {
            when (selectedTab) {
                0 -> SearchScreen(viewModel = viewModel)
                1 -> OfflineVideosScreen(viewModel = viewModel)
                2 -> ChannelsScreen(viewModel = viewModel)
                3 -> ConfigsScreen(viewModel = viewModel)
            }

            // Fullscreen Offline Video Player Overlay
            activeVideo?.let { video ->
                OfflineVideoPlayer(
                    video = video,
                    onClose = { viewModel.closePlayer() }
                )
            }

            // Global Instruction Tutorial Modal (with slides matching screenshots)
            if (isInstructionsVisible) {
                InstructionsDialog(
                    onDismiss = { viewModel.hideInstructions() }
                )
            }
        }
    }
}
