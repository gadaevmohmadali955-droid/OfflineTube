package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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

enum class VideoPlayerSource {
    YOUTUBE_ONLINE,
    OFFLINE_LOCAL
}

@Composable
fun OfflineVideoPlayer(
    video: VideoEntity,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Check if real local file is available
    val localFile = remember(video.localFilePath) {
        video.localFilePath?.let { File(it) }?.takeIf { it.exists() && it.length() > 50_000 }
    }

    // Default to YouTube online player so user sees real content, or local if downloaded
    var currentSource by remember {
        mutableStateOf(VideoPlayerSource.YOUTUBE_ONLINE)
    }

    var isControlsVisible by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isWebLoading by remember { mutableStateOf(true) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    DisposableEffect(video.id) {
        onDispose {
            try {
                webViewRef?.stopLoading()
                webViewRef?.loadUrl("about:blank")
                webViewRef?.destroy()
            } catch (_: Exception) {}
            webViewRef = null
        }
    }

    // Configure transient swipe navigation bar so bottom phone buttons don't block video
    // and swiping up restores them
    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }
        onDispose {
            val window = activity?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BlackBackground)
        ) {
            when (currentSource) {
                VideoPlayerSource.YOUTUBE_ONLINE -> {
                    // REAL YouTube Player (Videos and Shorts)
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            try {
                                val jsCache = File(ctx.cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
                                if (!jsCache.exists()) jsCache.mkdirs()
                                val wasmCache = File(ctx.cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
                                if (!wasmCache.exists()) wasmCache.mkdirs()
                            } catch (_: Exception) {}

                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(android.graphics.Color.BLACK)
                                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    mediaPlaybackRequiresUserGesture = false
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    allowContentAccess = true
                                    allowFileAccess = false
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                }
                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        if (newProgress >= 70) {
                                            isWebLoading = false
                                        }
                                    }
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isWebLoading = false
                                    }
                                }

                                val embedUrl = if (video.isShort) {
                                    "https://www.youtube.com/embed/${video.id}?autoplay=1&playsinline=1&controls=1&rel=0&loop=1&playlist=${video.id}"
                                } else {
                                    "https://www.youtube.com/embed/${video.id}?autoplay=1&playsinline=1&controls=1&rel=0"
                                }

                                loadUrl(embedUrl)
                                webViewRef = this
                            }
                        },
                        update = {
                            // Ready
                        }
                    )

                    if (isWebLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PureWhite,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }

                VideoPlayerSource.OFFLINE_LOCAL -> {
                    // Local MP4 Player using MediaPlayer
                    LocalOfflinePlayerView(
                        video = video,
                        localFile = localFile
                    )
                }
            }

            // Top Bar Overlay
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = Color.Black.copy(alpha = 0.75f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button
                    IconButton(
                        onClick = {
                            webViewRef?.destroy()
                            onClose()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(DarkSurface, CircleShape)
                            .testTag("close_player_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Title & Channel
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentSource == VideoPlayerSource.YOUTUBE_ONLINE) Icons.Default.SmartDisplay else Icons.Default.OfflinePin,
                                contentDescription = null,
                                tint = if (currentSource == VideoPlayerSource.YOUTUBE_ONLINE) PureWhite else GreenSuccess,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentSource == VideoPlayerSource.YOUTUBE_ONLINE) "YouTube • ${video.channelName}" else "Офлайн-файл • ${video.channelName}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Open in YouTube app button
                    IconButton(
                        onClick = {
                            try {
                                val url = if (video.videoUrl.isNotEmpty()) video.videoUrl else "https://www.youtube.com/watch?v=${video.id}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.w("VideoPlayer", "Cannot open YouTube app: ${e.message}")
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(DarkSurface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Открыть в приложении YouTube",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Reload button (in case connection was slow)
                    if (currentSource == VideoPlayerSource.YOUTUBE_ONLINE) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                isWebLoading = true
                                webViewRef?.reload()
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .background(DarkSurface, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Обновить",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Switch to Offline Local if downloaded
                    if (localFile != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                currentSource = if (currentSource == VideoPlayerSource.YOUTUBE_ONLINE) {
                                    VideoPlayerSource.OFFLINE_LOCAL
                                } else {
                                    VideoPlayerSource.YOUTUBE_ONLINE
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .background(DarkSurface, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (currentSource == VideoPlayerSource.YOUTUBE_ONLINE) Icons.Default.OfflinePin else Icons.Default.SmartDisplay,
                                contentDescription = "Переключить источник",
                                tint = if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) GreenSuccess else PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalOfflinePlayerView(
    video: VideoEntity,
    localFile: File?
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isPlayerPrepared by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(1L) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var surfaceHolder by remember { mutableStateOf<SurfaceHolder?>(null) }
    var isBuffering by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying, isPlayerPrepared) {
        while (isPlaying && isPlayerPrepared) {
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        currentPositionMs = player.currentPosition.toLong()
                        val dur = player.duration.toLong()
                        if (dur > 0) durationMs = dur
                    }
                } catch (e: Exception) {
                    // Ignore
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
                    reset()
                    release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
            mediaPlayer = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            surfaceHolder = holder
                            mediaPlayer?.let { old ->
                                try {
                                    old.reset()
                                    old.release()
                                } catch (e: Exception) {}
                            }

                            try {
                                val player = MediaPlayer().apply {
                                    setDisplay(holder)
                                    setOnPreparedListener { mp ->
                                        isPlayerPrepared = true
                                        isBuffering = false
                                        val dur = mp.duration.toLong()
                                        if (dur > 0) durationMs = dur
                                        mp.start()
                                        isPlaying = true
                                    }
                                    setOnCompletionListener {
                                        isPlaying = false
                                        currentPositionMs = durationMs
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        isBuffering = false
                                        isPlaying = false
                                        true
                                    }

                                    if (localFile != null && localFile.exists()) {
                                        setDataSource(ctx, Uri.fromFile(localFile))
                                    } else {
                                        val uri = Uri.parse("android.resource://${ctx.packageName}/${com.example.R.raw.sample_offline_video}")
                                        setDataSource(ctx, uri)
                                    }
                                    prepareAsync()
                                }
                                mediaPlayer = player
                            } catch (e: Exception) {
                                Log.e("LocalPlayer", "Setup error: ${e.message}")
                                isBuffering = false
                            }
                        }

                        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            surfaceHolder = null
                            try {
                                mediaPlayer?.setDisplay(null)
                            } catch (e: Exception) {}
                        }
                    })
                }
            }
        )

        if (isBuffering) {
            CircularProgressIndicator(
                color = PureWhite,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }

        // Bottom Controls for local player
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color.Black.copy(alpha = 0.65f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Slider
                val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                Slider(
                    value = progress,
                    onValueChange = { newProg ->
                        val targetMs = (newProg * durationMs).toLong()
                        currentPositionMs = targetMs
                        mediaPlayer?.seekTo(targetMs.toInt())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = PureWhite,
                        activeTrackColor = PureWhite,
                        inactiveTrackColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatTimeMs(currentPositionMs)} / ${formatTimeMs(durationMs)}",
                        color = PureWhite,
                        fontSize = 12.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val newPos = (currentPositionMs - 10000).coerceAtLeast(0)
                                mediaPlayer?.seekTo(newPos.toInt())
                                currentPositionMs = newPos
                            }
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "-10s", tint = PureWhite)
                        }

                        IconButton(
                            onClick = {
                                mediaPlayer?.let { player ->
                                    if (isPlaying) {
                                        player.pause()
                                        isPlaying = false
                                    } else {
                                        player.start()
                                        isPlaying = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(PureWhite, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Пауза" else "Играть",
                                tint = BlackBackground,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val newPos = (currentPositionMs + 10000).coerceAtMost(durationMs)
                                mediaPlayer?.seekTo(newPos.toInt())
                                currentPositionMs = newPos
                            }
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "+10s", tint = PureWhite)
                        }
                    }

                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            mediaPlayer?.setVolume(if (isMuted) 0f else 1f, if (isMuted) 0f else 1f)
                        }
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Звук",
                            tint = PureWhite
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "%.1f GB".format(gb)
        mb >= 1.0 -> "%.1f MB".format(mb)
        else -> "%.0f KB".format(kb)
    }
}
