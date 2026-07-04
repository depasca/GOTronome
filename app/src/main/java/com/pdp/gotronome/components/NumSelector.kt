package com.pdp.gotronome.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MockMetronomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun NumSelector(
    label: String,
    numProperty: StateFlow<Int>,
    propertySetter: (Int) -> Unit,
    propertyStorer: () -> Unit,
    minVal: Int,
    maxVal: Int,
    modifier: Modifier = Modifier,
) {
    val value by numProperty.collectAsStateWithLifecycle()
    val leftInteractionSource = remember { MutableInteractionSource() }
    val rightInteractionSource = remember { MutableInteractionSource() }
    val leftPressed by leftInteractionSource.collectIsPressedAsState()
    val rightPressed by rightInteractionSource.collectIsPressedAsState()
    val viewConfiguration = LocalViewConfiguration.current
    var leftLongPressed by remember { mutableStateOf(false) }
    var rightLongPressed by remember { mutableStateOf(false) }
    var valueChanged by remember { mutableStateOf(false) }

    // Long-press the down button to keep decreasing; persist once on release.
    LaunchedEffect(leftPressed) {
        if (leftPressed) {
            valueChanged = true
            leftLongPressed = false
            delay(viewConfiguration.longPressTimeoutMillis)
            leftLongPressed = true
            while (leftPressed) {
                propertySetter(max(minVal, value - 4))
                delay(50)
            }
        } else {
            leftLongPressed = false
            if (valueChanged) {
                propertyStorer()
                valueChanged = false
            }
        }
    }

    // Long-press the up button to keep increasing; persist once on release.
    LaunchedEffect(rightPressed) {
        if (rightPressed) {
            valueChanged = true
            rightLongPressed = false
            delay(viewConfiguration.longPressTimeoutMillis)
            rightLongPressed = true
            while (rightPressed) {
                propertySetter(min(maxVal, value + 4))
                delay(50)
            }
        } else {
            rightLongPressed = false
            if (valueChanged) {
                propertyStorer()
                valueChanged = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(
                interactionSource = leftInteractionSource,
                onClick = { if (!leftLongPressed) propertySetter(max(minVal, value - 1)) },
                enabled = value > minVal,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Decrease $label",
                )
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { propertySetter(it.roundToInt().coerceIn(minVal, maxVal)) },
                onValueChangeFinished = { propertyStorer() },
                valueRange = minVal.toFloat()..maxVal.toFloat(),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                interactionSource = rightInteractionSource,
                onClick = { if (!rightLongPressed) propertySetter(min(maxVal, value + 1)) },
                enabled = value < maxVal,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Increase $label",
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
        label = "Loop bars",
        viewModel.numBars,
        { viewModel.setNumBars(it) },
        { viewModel.storeNumBars() },
        2,
        32,
    )
}
