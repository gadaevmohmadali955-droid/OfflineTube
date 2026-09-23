package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
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

fun isDeviceOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val net = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(net) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun OfflineVideoPlayer(
    video: VideoEntity,
    onClose: () -> Unit,
    onDownload: ((VideoEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Check if network is connected
    val online = remember { isDeviceOnline(context) }

    // Check if real local file is available on device
    val localFile = remember(video.localFilePath, video.isDownloaded) {
        val targetPath = video.localFilePath ?: File(context.filesDir, "offline_videos/${video.id}.mp4").absolutePath
        File(targetPath).takeIf { it.exists() && it.length() > 50_000 }
    }

    // Default to OFFLINE_LOCAL if no internet, or YOUTUBE_ONLINE if online
    var currentSource by remember {
        mutableStateOf(
            if (!online && localFile != null) VideoPlayerSource.OFFLINE_LOCAL
            else if (localFile != null && video.isDownloaded) VideoPlayerSource.OFFLINE_LOCAL
            else VideoPlayerSource.YOUTUBE_ONLINE
        )
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isWebLoading by remember { mutableStateOf(true) }
    var isDownloadStarted by remember { mutableStateOf(false) }

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

    // Hide phone bottom buttons (transient swipe up restores them)
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
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BlackBackground)
        ) {
            when (currentSource) {
                VideoPlayerSource.YOUTUBE_ONLINE -> {
                    if (!online && localFile == null) {
                        // Offline notice
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Нет подключения к интернету",
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Чтобы смотреть видео без интернета, скачайте его заранее в приложении при наличии сети.",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = onClose,
                                    colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = BlackBackground)
                                ) {
                                    Text("Понятно", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Embedded YouTube Player with exact origin & referer policy to prevent Error 153
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
                                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                                    }

                                    webChromeClient = object : WebChromeClient() {
                                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                            if (newProgress >= 70) {
                                                isWebLoading = false
                                            }
                                        }
                                    }

                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                            val url = request?.url?.toString() ?: ""
                                            if (url.startsWith("intent:") || url.startsWith("vnd.youtube:")) {
                                                try {
                                                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                                    ctx.startActivity(intent)
                                                    return true
                                                } catch (_: Exception) {
                                                    return true
                                                }
                                            }
                                            return false
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            isWebLoading = false
                                        }
                                    }

                                    // HTML structure with referrerpolicy="strict-origin-when-cross-origin"
                                    // and origin=https://www.youtube.com which resolves YouTube Error 153
                                    val embedSrc = "https://www.youtube-nocookie.com/embed/${video.id}?autoplay=1&playsinline=1&controls=1&enablejsapi=1&origin=https://www.youtube.com&rel=0&modestbranding=1"

                                    val html = """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                            <style>
                                                * { margin: 0; padding: 0; box-sizing: border-box; }
                                                html, body {
                                                    width: 100%;
                                                    height: 100%;
                                                    background-color: #000000;
                                                    overflow: hidden;
                                                }
                                                .player-container {
                                                    position: absolute;
                                                    top: 0;
                                                    left: 0;
                                                    width: 100%;
                                                    height: 100%;
                                                    display: flex;
                                                    align-items: center;
                                                    justify-content: center;
                                                }
                                                iframe {
                                                    width: 100%;
                                                    height: 100%;
                                                    border: 0;
                                                }
                                            </style>
                                        </head>
                                        <body>
                                            <div class="player-container">
                                                <iframe 
                                                    id="ytplayer"
                                                    type="text/html"
                                                    src="$embedSrc"
                                                    referrerpolicy="strict-origin-when-cross-origin"
                                                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                                                    allowfullscreen>
                                                </iframe>
                                            </div>
                                        </body>
                                        </html>
                                    """.trimIndent()

                                    loadDataWithBaseURL(
                                        "https://www.youtube.com",
                                        html,
                                        "text/html",
                                        "UTF-8",
                                        "https://www.youtube.com"
                                    )
                                    webViewRef = this
                                }
                            },
                            update = {}
                        )

                        if (isWebLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = PureWhite,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }

                VideoPlayerSource.OFFLINE_LOCAL -> {
                    // Local MP4 Player (completely offline without internet)
                    LocalOfflinePlayerView(
                        video = video,
                        localFile = localFile
                    )
                }
            }

            // Top Header Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = Color.Black.copy(alpha = 0.82f)
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
                                imageVector = if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) Icons.Default.OfflinePin else Icons.Default.SmartDisplay,
                                contentDescription = null,
                                tint = if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) GreenSuccess else PureWhite,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) "Офлайн (без интернета) • ${video.channelName}" else "YouTube Онлайн • ${video.channelName}",
                                color = if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) GreenSuccess else TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Open in YouTube app button (always available fallback)
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

                    // Download for offline button (if not downloaded)
                    if (localFile == null && onDownload != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                isDownloadStarted = true
                                onDownload(video)
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .background(if (isDownloadStarted) GreenSuccess.copy(alpha = 0.2f) else DarkSurface, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isDownloadStarted) Icons.Default.FileDownloadDone else Icons.Default.Download,
                                contentDescription = "Скачать для офлайн",
                                tint = if (isDownloadStarted) GreenSuccess else PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Toggle between Online & Offline if local file is ready
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
                                .background(if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) GreenSuccess.copy(alpha = 0.25f) else DarkSurface, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) Icons.Default.OfflinePin else Icons.Default.SmartDisplay,
                                contentDescription = "Переключить источник",
                                tint = if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) GreenSuccess else PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Offline Helper Strip
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = Color.Black.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) Icons.Default.OfflinePin else Icons.Default.Download,
                            contentDescription = null,
                            tint = if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) GreenSuccess else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentSource == VideoPlayerSource.OFFLINE_LOCAL) {
                                "Офлайн-режим: воспроизведение из памяти (без интернета)"
                            } else if (localFile != null) {
                                "Файл сохранен! Нажмите значок галочки сверху для режима без интернета"
                            } else {
                                "Для просмотра без интернета нажмите кнопку скачивания со стрелкой"
                            },
                            color = PureWhite,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (localFile == null && onDownload != null && !isDownloadStarted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                isDownloadStarted = true
                                onDownload(video)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                        ) {
                            Text("Скачать", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                } catch (_: Exception) {}
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
                } catch (_: Exception) {}
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
                                } catch (_: Exception) {}
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
                            } catch (_: Exception) {}
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
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            color = Color.Black.copy(alpha = 0.65f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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
