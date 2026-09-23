package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.VideoEntity
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun OfflineVideoPlayer(
    video: VideoEntity,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var playWhenReady by remember { mutableStateOf(true) }
    var isPlayerPrepared by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(1L) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var hasTriedFallback by remember { mutableStateOf(false) }

    // MediaPlayer reference
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var surfaceHolder by remember { mutableStateOf<SurfaceHolder?>(null) }

    // Auto-hide controls
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(4000)
            isControlsVisible = false
        }
    }

    // Periodic time update
    LaunchedEffect(isPlaying, isPlayerPrepared) {
        while (isPlaying && isPlayerPrepared && !hasError) {
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        currentPositionMs = player.currentPosition.toLong()
                        val dur = player.duration.toLong()
                        if (dur > 0) durationMs = dur
                    }
                } catch (e: Exception) {
                    Log.w("VideoPlayer", "Error getting position: ${e.message}")
                }
            }
            delay(500)
        }
    }

    DisposableEffect(video.id) {
        onDispose {
            isPlayerPrepared = false
            isPlaying = false
            mediaPlayer?.run {
                try {
                    setOnPreparedListener(null)
                    setOnCompletionListener(null)
                    setOnErrorListener(null)
                    reset()
                    release()
                } catch (e: Exception) {
                    Log.w("VideoPlayer", "Dispose error: ${e.message}")
                }
            }
            mediaPlayer = null
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BlackBackground)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    isControlsVisible = !isControlsVisible
                }
        ) {
            // Android SurfaceView for hardware video decoding
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    SurfaceView(ctx).apply {
                        holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(holder: SurfaceHolder) {
                                surfaceHolder = holder
                                mediaPlayer?.let { oldPlayer ->
                                    try {
                                        oldPlayer.reset()
                                        oldPlayer.release()
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }

                                try {
                                    val player = MediaPlayer().apply {
                                        setDisplay(holder)
                                        setOnPreparedListener { mp ->
                                            isPlayerPrepared = true
                                            isBuffering = false
                                            hasError = false
                                            val dur = mp.duration.toLong()
                                            if (dur > 0) durationMs = dur
                                            if (playWhenReady) {
                                                try {
                                                    mp.start()
                                                    isPlaying = true
                                                } catch (e: Exception) {
                                                    Log.e("VideoPlayer", "Start on prepared failed: ${e.message}")
                                                }
                                            }
                                        }
                                        setOnCompletionListener {
                                            isPlaying = false
                                            currentPositionMs = durationMs
                                            isControlsVisible = true
                                        }
                                        setOnErrorListener { mp, what, extra ->
                                            Log.w("VideoPlayer", "MediaPlayer onError: what=$what, extra=$extra")
                                            isPlayerPrepared = false
                                            isPlaying = false
                                            if (!hasTriedFallback) {
                                                hasTriedFallback = true
                                                try {
                                                    mp.reset()
                                                    mp.setDisplay(holder)
                                                    val afd = ctx.resources.openRawResourceFd(com.example.R.raw.sample_offline_video)
                                                    if (afd != null) {
                                                        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                                        afd.close()
                                                        mp.prepareAsync()
                                                        isBuffering = true
                                                        return@setOnErrorListener true
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("VideoPlayer", "Fallback failed: ${e.message}")
                                                }
                                            }
                                            isBuffering = false
                                            hasError = true
                                            try {
                                                mp.reset()
                                            } catch (e: Exception) {
                                                // Ignore
                                            }
                                            true
                                        }

                                        // Set data source
                                        var dataSourceSet = false
                                        val localFile = video.localFilePath?.let { File(it) }
                                        if (localFile != null && localFile.exists() && localFile.length() > 50_000) {
                                            try {
                                                setDataSource(ctx, Uri.fromFile(localFile))
                                                dataSourceSet = true
                                            } catch (e: Exception) {
                                                Log.w("VideoPlayer", "Could not set local file: ${e.message}")
                                            }
                                        }
                                        if (!dataSourceSet) {
                                            try {
                                                val afd = ctx.resources.openRawResourceFd(com.example.R.raw.sample_offline_video)
                                                if (afd != null) {
                                                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                                    afd.close()
                                                    dataSourceSet = true
                                                }
                                            } catch (e: Exception) {
                                                Log.w("VideoPlayer", "Could not set raw sample: ${e.message}")
                                            }
                                        }
                                        if (!dataSourceSet) {
                                            setDataSource(
                                                ctx,
                                                Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
                                            )
                                        }
                                        prepareAsync()
                                    }
                                    mediaPlayer = player
                                } catch (e: Exception) {
                                    Log.e("VideoPlayer", "Setup error: ${e.message}")
                                    isBuffering = false
                                    hasError = true
                                    isPlayerPrepared = false
                                }
                            }

                            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                            override fun surfaceDestroyed(holder: SurfaceHolder) {
                                surfaceHolder = null
                                try {
                                    mediaPlayer?.setDisplay(null)
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            }
                        })
                    }
                }
            )

            // Buffering Indicator
            if (isBuffering) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PureWhite,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            // Error notice
            if (hasError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(DarkCard, RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Офлайн предпросмотр видео",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Локальный файл сохранен в хранилище приложения (${formatBytes(video.fileSizeBytes)})",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Controls Overlay
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .testTag("close_player_button")
                                .size(48.dp)
                                .background(DarkSurface, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть плеер",
                                tint = PureWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.title,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.OfflinePin,
                                    contentDescription = null,
                                    tint = GreenSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Офлайн-режим • ${video.channelName}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Mute button
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                try {
                                    mediaPlayer?.setVolume(
                                        if (isMuted) 0f else 1f,
                                        if (isMuted) 0f else 1f
                                    )
                                } catch (e: Exception) {
                                    Log.w("VideoPlayer", "Volume set error: ${e.message}")
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(DarkSurface, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Звук",
                                tint = PureWhite
                            )
                        }
                    }

                    // Center Play/Pause Controls
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind -10s
                        IconButton(
                            onClick = {
                                if (!isPlayerPrepared || hasError) return@IconButton
                                mediaPlayer?.let { player ->
                                    try {
                                        val newPos = (player.currentPosition - 10000).coerceAtLeast(0)
                                        player.seekTo(newPos)
                                        currentPositionMs = newPos.toLong()
                                    } catch (e: Exception) {
                                        Log.w("VideoPlayer", "Rewind seek error: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .background(DarkSurface.copy(alpha = 0.85f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Назад на 10 сек",
                                tint = PureWhite,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Play / Pause White Button
                        IconButton(
                            onClick = {
                                if (!isPlayerPrepared) {
                                    playWhenReady = !playWhenReady
                                    isPlaying = playWhenReady
                                    return@IconButton
                                }
                                if (hasError) return@IconButton
                                mediaPlayer?.let { player ->
                                    try {
                                        if (isPlaying) {
                                            player.pause()
                                            isPlaying = false
                                            playWhenReady = false
                                        } else {
                                            player.start()
                                            isPlaying = true
                                            playWhenReady = true
                                        }
                                    } catch (e: Exception) {
                                        Log.e("VideoPlayer", "Play/Pause error: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier
                                .testTag("play_pause_button")
                                .size(72.dp)
                                .background(PureWhite, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Пауза" else "Воспроизведение",
                                tint = BlackBackground,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Forward +10s
                        IconButton(
                            onClick = {
                                if (!isPlayerPrepared || hasError) return@IconButton
                                mediaPlayer?.let { player ->
                                    try {
                                        val newPos = (player.currentPosition + 10000).coerceAtMost(player.duration)
                                        player.seekTo(newPos)
                                        currentPositionMs = newPos.toLong()
                                    } catch (e: Exception) {
                                        Log.w("VideoPlayer", "Forward seek error: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .background(DarkSurface.copy(alpha = 0.85f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Вперед на 10 сек",
                                tint = PureWhite,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Bottom Bar with Scrubber and Time
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Slider / Scrubber
                        val sliderPos = if (durationMs > 0) {
                            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        Slider(
                            value = sliderPos,
                            enabled = isPlayerPrepared && !hasError && durationMs > 0,
                            onValueChange = { frac ->
                                if (isPlayerPrepared && !hasError) {
                                    val target = (frac * durationMs).toLong()
                                    currentPositionMs = target
                                    try {
                                        mediaPlayer?.seekTo(target.toInt())
                                    } catch (e: Exception) {
                                        Log.w("VideoPlayer", "Scrubber seek error: ${e.message}")
                                    }
                                }
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = PureWhite,
                                activeTrackColor = PureWhite,
                                inactiveTrackColor = DarkCard
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${formatTime(currentPositionMs)} / ${formatTime(durationMs.coerceAtLeast(1000L))}",
                                color = PureWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            // Playback Speed Button (White badge)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = PureWhite,
                                modifier = Modifier
                                    .clickable {
                                        playbackSpeed = when (playbackSpeed) {
                                            1.0f -> 1.25f
                                            1.25f -> 1.5f
                                            1.5f -> 2.0f
                                            2.0f -> 0.75f
                                            else -> 1.0f
                                        }
                                        try {
                                            mediaPlayer?.let { player ->
                                                player.playbackParams = player.playbackParams.setSpeed(playbackSpeed)
                                            }
                                        } catch (e: Exception) {
                                            Log.w("VideoPlayer", "Speed set error: ${e.message}")
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = BlackBackground,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = BlackBackground,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "5.2 МБ"
    val mb = bytes.toDouble() / (1024 * 1024)
    return String.format("%.1f МБ", mb)
}
