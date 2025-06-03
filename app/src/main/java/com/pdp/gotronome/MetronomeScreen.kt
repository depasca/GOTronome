package com.pdp.gotronome

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.layout.WindowMetricsCalculator
import com.pdp.gotronome.ui.theme.GOTronomeTheme

private const val TAG = "GOT-MetronomeScreen"

@Composable
fun MetronomeScreen(
    viewModel: MetronomeViewModel,
    context: Context = LocalContext.current
) {
    val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(context as MainActivity)
    val currentBounds = windowMetrics.bounds
    val isLandscape = currentBounds.width() > currentBounds.height()
    Log.d(TAG, "isLandscape: $isLandscape")
    val beatsPerMeasure by viewModel.beatsPerMeasure.collectAsStateWithLifecycle()
    var isPlaying by remember { mutableStateOf(false) }
    var currentBeat by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                isPlaying = viewModel.getIsPlaying()
                currentBeat = viewModel.getCurrentBeat()
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    GOTronomeTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.gotbg),
                contentDescription = "Background",
                modifier = Modifier.fillMaxHeight(),
                alignment = Alignment.Center,
                contentScale = ContentScale.FillHeight
            )
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp)
                        .clickable(
                            onClick = {
                                if (isPlaying) {
                                    viewModel.stop(); Log.d(TAG, "Metronome stopped")
                                } else {
                                    viewModel.start(); Log.d(TAG, "Metronome started")
                                }
                            },
                            interactionSource = interactionSource,
                            indication = ripple(),
                        ),
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isPlaying) {
                        for (i in 1..beatsPerMeasure) {
                            BeatView(
                                number = i,
                                beatNumber = currentBeat,
                                beatsPerMeasure = beatsPerMeasure,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Log.d(TAG, "Metronome Settings horizontal")
                        SettingsScreenHorizontal(viewModel = viewModel)
                    }
                }
            } else { // Portrait
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp)
                        .clickable(
                            onClick = {
                                if (isPlaying) {
                                    viewModel.stop(); Log.d(TAG, "Metronome stopped")
                                } else {
                                    viewModel.start(); Log.d(TAG, "Metronome started")
                                }
                            },
                            interactionSource = interactionSource,
                            indication = ripple(),
                        ),
                    horizontalAlignment = CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isPlaying) {
                        for (i in 1..beatsPerMeasure) {
                            BeatView(
                                number = i,
                                beatNumber = currentBeat,
                                beatsPerMeasure = beatsPerMeasure,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Log.d(TAG, "Metronome Settings vertical")
                        SettingsScreenVertical(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MetronomeScreenPreview() {
    MetronomeScreen(viewModel = viewModel<MockMetronomeViewModel>())
}