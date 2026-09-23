package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainScreen
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
        }
        handleIncomingIntent(intent)
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BlackBackground
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        var rawConfigLink: String? = null

        // 1. Deep link: tubesync://config/afdvdkwo1h
        val dataUri = intent.data
        if (dataUri != null) {
            val scheme = dataUri.scheme
            val host = dataUri.host
            if (scheme == "tubesync" && host == "config") {
                val path = dataUri.lastPathSegment ?: ""
                rawConfigLink = "offline.$path"
            } else if (dataUri.toString().contains("offline.")) {
                rawConfigLink = dataUri.toString().substringAfter("offline.")
                rawConfigLink = "offline.$rawConfigLink"
            }
        }

        // 2. Shared text from Telegram / WhatsApp / Messenger
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null && sharedText.contains("offline.")) {
            val match = Regex("""offline\.([a-zA-Z0-9]{10})""").find(sharedText)
            if (match != null) {
                rawConfigLink = match.value
            }
        }

        rawConfigLink?.let { link ->
            // Switch to Configs tab (tab index 3)
            viewModel.selectTab(3)
            viewModel.setConfigLinkInput(link)
            viewModel.resolveAndPreviewConfig(link)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        text = "TubeSync - $name",
        color = androidx.compose.ui.graphics.Color.White,
        modifier = modifier
    )
}
