package com.pdp.gotronome

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.window.layout.WindowMetricsCalculator
import com.pdp.gotronome.ui.theme.GOTronomeTheme
import androidx.lifecycle.viewmodel.compose.viewModel
private const val TAG = "GOT-MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make activity fullscreen before setting content
        // This allows your content to draw edge-to-edge.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Get the WindowInsetsController
        val windowInsetsController =
            WindowInsetsControllerCompat(window, window.decorView)

        // Hide the system bars (status bar and navigation bar)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configure the behavior for showing transient system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        enableEdgeToEdge()
        setContent {
            val metronome: Metronome = Metronome()
            val viewModel: MetronomeViewModel = viewModel<MetronomeViewModel>()
            viewModel.setMetronome(metronome)
            GOTronomeTheme {
                Log.d(TAG, "start")
                MetronomeScreen(viewModel)
                Log.d(TAG, "end")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GOTronomePreview() {
    GOTronomeTheme {
        MetronomeScreen(viewModel = viewModel<MockMetronomeViewModel>())
    }
}