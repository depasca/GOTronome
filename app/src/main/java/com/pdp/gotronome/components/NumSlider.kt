package com.pdp.gotronome.components

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MockMetronomeViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumSlider(
    numProperty: StateFlow<Int>,
    propertySetter: (Int) -> Unit,
    propertyStorer: () -> Unit,
    minVal: Int,
    maxVal: Int,
){
    val _numProperty by numProperty.collectAsStateWithLifecycle()
    var sliderPosition by remember { mutableFloatStateOf(_numProperty.toFloat()) }
    Row (verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp) ){
        Text(text = sliderPosition.toInt().toString(), color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(16.dp))
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it.roundToInt().toFloat() },
            onValueChangeFinished = {
                propertySetter(sliderPosition.toInt())
                propertyStorer()
            },
            valueRange = minVal.toFloat()..maxVal.toFloat(),
            // Use the track parameter to remove the "stops" (dots)
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier,
                    drawStopIndicator = null, // THIS REMOVES THE DOT
                    thumbTrackGapSize = 0.dp,  // THIS REMOVES THE GAP
                    trackInsideCornerSize = 0.dp,
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f),
                    )
                )
            }
        )
    }
}

@Preview
@Composable
fun NumSliderPreview(){
    val viewModel = viewModel<MockMetronomeViewModel>()
    NumSlider(
        viewModel.numBars,
        {viewModel.setNumBars(it)},
        {viewModel.storeNumBars()},
        0,
        10,
    )
}