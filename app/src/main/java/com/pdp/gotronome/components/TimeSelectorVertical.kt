package com.pdp.gotronome.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdp.gotronome.MetronomeViewModel
import com.pdp.gotronome.MockMetronomeViewModel
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeSelectorVertical(
    modifier: Modifier = Modifier,
    viewModel: MetronomeViewModel,
) {
    val beatsPeerMinute by viewModel.beatsPerMinute.collectAsStateWithLifecycle()
    val leftInteractionSource = remember { MutableInteractionSource() }
    val rightInteractionSource = remember { MutableInteractionSource() }
    val leftPressed by leftInteractionSource.collectIsPressedAsState()
    val rightPressed by rightInteractionSource.collectIsPressedAsState()
    val viewConfiguration = LocalViewConfiguration.current
    val lastTimestamp = remember { mutableLongStateOf(System.currentTimeMillis()) }

    // State to track if a long press has started to avoid single click trigger on release
    var leftLongPressed by remember { mutableStateOf(false) }
    var rightLongPressed by remember { mutableStateOf(false) }

    // Left Button Long Press Logic
    LaunchedEffect(leftPressed) {
        if (leftPressed) {
            leftLongPressed = false // Reset long press state on new press
            delay(viewConfiguration.longPressTimeoutMillis) // Initial delay
            leftLongPressed = true // Mark as long press after initial delay
            while (leftPressed) {
                // Continuous update while pressed
                val value = max(40, beatsPeerMinute - 4) // Decrease by 4
                viewModel.setBeatsPerMinute(value)
                delay(50) // Adjust this delay to control update speed (e.g., 50ms)
            }
        } else {
            leftLongPressed = false // Reset when released
        }
    }

    // Right Button Long Press Logic
    LaunchedEffect(rightPressed) {
        if (rightPressed) {
            rightLongPressed = false // Reset long press state on new press
            delay(viewConfiguration.longPressTimeoutMillis) // Initial delay
            rightLongPressed = true // Mark as long press after initial delay
            while (rightPressed) {
                // Continuous update while pressed
                val value = min(240, beatsPeerMinute + 4) // Increase by 4
                viewModel.setBeatsPerMinute(value)
                delay(50) // Adjust this delay to control update speed (e.g., 50ms)
            }
        } else {
            rightLongPressed = false // Reset when released
        }
    }

    Column {
        Text(
            text = "BPM",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(all = 16.dp)
        )
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                interactionSource = leftInteractionSource,
                onClick = {
                    if (!leftLongPressed) { // Only trigger single click if not a long press
                        val value = max(0, beatsPeerMinute - 1)
                        viewModel.setBeatsPerMinute(value)
                    }
                },
                enabled = beatsPeerMinute > 0,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown, // More distinct icon
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Decrease BPM"
                )
            }
            Text(
                text = beatsPeerMinute.toString(),
                style = MaterialTheme.typography.bodyLarge, // Larger, more prominent
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(min = 16.dp), // Ensure minimum width for text
                textAlign = TextAlign.Center
            )
            IconButton(
                interactionSource = rightInteractionSource,
                onClick = {
                    if (!rightLongPressed) { // Only trigger single click if not a long press
                        val value = min(240, beatsPeerMinute + 1)
                        viewModel.setBeatsPerMinute(value)
                    }
                },
                enabled = beatsPeerMinute <= 240,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp, // More distinct icon
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Increase BPM"
                )
            }
        }
    }
}

@Preview
@Composable
fun TimeSelectorVerticalPreview() {
    TimeSelectorVertical(
        viewModel = MockMetronomeViewModel() // Provide a mock ViewModel for preview
    )
}