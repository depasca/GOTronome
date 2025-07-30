package com.pdp.gotronome

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.pdp.gotronome.ui.theme.GOTronomeTheme

private const val TAG = "GOT-Settings"

@Composable
fun MetronomeScreen(
    viewModel: MetronomeViewModel,
    context: Context = LocalContext.current
) {
    val windowMetrics =
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(context as MainActivity)
    val currentBounds = windowMetrics.bounds
    val isLandscape = currentBounds.width() > currentBounds.height()
    val page by viewModel.page.collectAsStateWithLifecycle()
    val beatsPerMeasure by viewModel.beatsPerMeasure.collectAsStateWithLifecycle()
    val reviewPromptCounter by viewModel.reviewPromptCounter.collectAsStateWithLifecycle()
    val numRuns by viewModel.numRuns.collectAsStateWithLifecycle()
    val showBars by viewModel.showBars.collectAsStateWithLifecycle()
    val numBars by viewModel.numBars.collectAsStateWithLifecycle()
    val playingMode by viewModel.mode.collectAsStateWithLifecycle()

    var playingState by remember { mutableStateOf(0) }
    var currentBeat by remember { mutableIntStateOf(0) }
    var currentBar by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                currentBeat = viewModel.getCurrentBeat()
                currentBar = viewModel.getCurrentBar()
                playingState = viewModel.getIsPlaying()
                if (playingMode == "Silent bars" &&
                    playingState == PLAYING_STATE_SILENT) {
                    currentBeat = 0
                    currentBar = 0
                }
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

            // check for app updates
//            val appUpdateManager = AppUpdateManagerFactory.create(context)
//            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
//            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
//                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
//                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
//                ) {
//                    runUpdateFlow(appUpdateInfo, appUpdateManager)
//                }
//            }

            //in-app review logic
            if (numRuns >= reviewPromptCounter) {
                Log.d(TAG, "Showing review prompt. numRuns $numRuns, review counter $reviewPromptCounter")
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.gotronome_icon),
                    contentDescription = "GOTronome banner"
                )
                val manager = ReviewManagerFactory.create(context)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val flow =
                            manager.launchReviewFlow(context, reviewInfo)
                        flow.addOnCompleteListener { _ ->
                            viewModel.incrementReviewPromptCounter()
                            viewModel.resetNumRuns()
                        }
                    } else {
                        @ReviewErrorCode val reviewErrorCode =
                            (task.getException() as ReviewException).errorCode
                        Log.w(TAG, "Error: $reviewErrorCode")
                    }
                }
            } else {
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp)
                            .clickable(
                                onClick = {
                                    if (playingState != PLAYING_STATE_STOPPED) {
                                        viewModel.stop(); Log.d(TAG, "Metronome stopped")
                                        viewModel.incrementNumRuns()
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
                        if (playingState != PLAYING_STATE_STOPPED) {
                            Column (
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(4.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = CenterHorizontally
                            ){
                                Row (
                                    modifier = Modifier.weight(4f),
                                    ) {
                                    for (i in 1..beatsPerMeasure) {
                                        BeatView(
                                            number = i,
                                            beatNumber = currentBeat,
                                            beatsPerMeasure = beatsPerMeasure,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                if (showBars) {
                                    Row(
                                        modifier = Modifier.weight(1f).fillMaxWidth().padding(4.dp)
                                    ) {
                                        for (i in 1..numBars) {
                                            var bgColor = MaterialTheme.colorScheme.surface
                                            var textColor = MaterialTheme.colorScheme.primary
                                            if (i == currentBar) {
                                                bgColor = MaterialTheme.colorScheme.primary
                                                textColor = MaterialTheme.colorScheme.secondary
                                            }
                                            Box(
                                                modifier = Modifier.padding(2.dp)
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .border(
                                                        width = 2.dp,
                                                        color = bgColor,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(bgColor),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = i.toString(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = textColor,
                                                )

                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            when (page) {
                                "info" -> InfoScreen(handleClick = {
                                    viewModel.setPage("settings")
                                })

                                else ->
                                    SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                } else { // Portrait
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp)
                            .clickable(
                                onClick = {
                                    if (playingState != PLAYING_STATE_STOPPED) {
                                        viewModel.stop(); Log.d(TAG, "Metronome stopped")
                                        viewModel.incrementNumRuns()
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
                        if (playingState != PLAYING_STATE_STOPPED) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = CenterVertically
                            )
                            {
                                Column(
                                    modifier = Modifier.weight(4f),
                                )
                                {
                                    for (i in 1..beatsPerMeasure) {
                                        BeatView(
                                            number = i,
                                            beatNumber = currentBeat,
                                            beatsPerMeasure = beatsPerMeasure,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                if (showBars) {
                                    Column(
                                        modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
                                    ) {
                                        for (i in 1..numBars) {
                                            var bgColor = MaterialTheme.colorScheme.surface
                                            var textColor = MaterialTheme.colorScheme.primary
                                            if(i == currentBar) {
                                                bgColor = MaterialTheme.colorScheme.primary
                                                textColor = MaterialTheme.colorScheme.secondary
                                            }
                                            Box(
                                                modifier = Modifier.padding(2.dp)
                                                    .weight(1f)
                                                    .fillMaxWidth()
                                                    .border(
                                                        width = 2.dp,
                                                        color = bgColor,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(bgColor),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = i.toString(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = textColor,
                                                )

                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            when (page) {
                                "info" -> InfoScreen(handleClick = {
                                    viewModel.setPage("settings")
                                })

                                else ->
                                    SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

//fun runUpdateFlow(appUpdateInfo: AppUpdateInfo, appUpdateManager: AppUpdateManager) {
//
//    Log.i(TAG, "Update available")
//    val activityResultLauncher = registerForActivityResult(StartIntentSenderForResult()) { result: ActivityResult ->
//        // handle callback
//        if (result.resultCode != RESULT_OK) {
//            Log.w("Update flow failed! Result code: " + result.resultCode);
//            // If the update is canceled or fails,
//            // you can request to start the update again.
//        }
//    }
//    appUpdateManager.startUpdateFlowForResult(
//        appUpdateInfo,
//        activityResultLauncher,
//        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
//    )
//}


@Preview
@Composable
fun MetronomeScreenPreview() {
    MetronomeScreen(viewModel = viewModel<MockMetronomeViewModel>())
}