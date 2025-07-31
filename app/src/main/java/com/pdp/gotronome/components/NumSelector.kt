package com.pdp.gotronome.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MockMetronomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.unit.dp

@Composable
fun NumSelector(
    numProperty: StateFlow<Int>,
    propertySetter: (Int) -> Unit,
    propertyStorer: () -> Unit,
    minVal: Int,
    maxVal: Int
) {
    val _numProperty by numProperty.collectAsStateWithLifecycle()
    val leftInteractionSource = remember { MutableInteractionSource() }
    val rightInteractionSource = remember { MutableInteractionSource() }
    val leftPressed by leftInteractionSource.collectIsPressedAsState()
    val rightPressed by rightInteractionSource.collectIsPressedAsState()
    val viewConfiguration = LocalViewConfiguration.current
    var leftLongPressed by remember { mutableStateOf(false) }
    var rightLongPressed by remember { mutableStateOf(false) }
    var numPropertyChanged by remember { mutableStateOf(false) }

    // Left Button Long Press Logic
    LaunchedEffect(leftPressed) {
        if (leftPressed) {
            numPropertyChanged = true
            leftLongPressed = false // Reset long press state on new press
            delay(viewConfiguration.longPressTimeoutMillis) // Initial delay
            leftLongPressed = true // Mark as long press after initial delay
            while (leftPressed) {
                // Continuous update while pressed
                val value = max(minVal, _numProperty - 4) // Decrease by 4
                propertySetter(value)
                delay(50) // Adjust this delay to control update speed (e.g., 50ms)
            }
        } else {
            leftLongPressed = false // Reset when released
            if (numPropertyChanged) {
                propertyStorer()
                numPropertyChanged = false
            }
        }
    }

    // Right Button Long Press Logic
    LaunchedEffect(rightPressed) {
        if (rightPressed) {
            numPropertyChanged = true
            rightLongPressed = false // Reset long press state on new press
            delay(viewConfiguration.longPressTimeoutMillis) // Initial delay
            rightLongPressed = true // Mark as long press after initial delay
            while (rightPressed) {
                // Continuous update while pressed
                val value = min(maxVal, _numProperty + 4) // Increase by 4
                propertySetter(value)
                delay(50) // Adjust this delay to control update speed (e.g., 50ms)
            }
        } else {
            rightLongPressed = false // Reset when released
            if (numPropertyChanged) {
                propertyStorer()
                numPropertyChanged = false
            }
        }
    }
    FlowRow(
        modifier = Modifier.padding(all=8.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
        verticalArrangement = Arrangement.Center
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(
                interactionSource = leftInteractionSource,
                onClick = {
                    if (!leftLongPressed) { // Only trigger single click if not a long press
                        val value = max(minVal, _numProperty - 1)
                        propertySetter(value)
                    }
                },
                enabled = +_numProperty > minVal,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown, // More distinct icon
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Decrease Num Bars"
                )
            }
            Text(
                text = _numProperty.toString(),
                style = MaterialTheme.typography.bodyLarge, // Larger, more prominent
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
            IconButton(
                interactionSource = rightInteractionSource,
                onClick = {
                    if (!rightLongPressed) { // Only trigger single click if not a long press
                        val value = min(maxVal, _numProperty + 1)
                        propertySetter(value)
                    }
                },
                enabled = _numProperty <= maxVal,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp, // More distinct icon
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Increase Num Bars"
                )
            }
        }
    }
}

@Preview
@Composable
fun NumSelectorPreview() {
    val viewModel = viewModel<MockMetronomeViewModel>()
    NumSelector(
        viewModel.numBars,
        {viewModel.setNumBars(it)},
        {viewModel.storeNumBars()},
        2,
        32
    )
}