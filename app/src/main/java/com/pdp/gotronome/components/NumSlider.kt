package com.pdp.gotronome.components

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
    Row (verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp) ){
        Text(text = sliderPosition.toString(), color = MaterialTheme.colorScheme.secondary,)
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it.roundToInt().toFloat() },
            onValueChangeFinished = { propertySetter(sliderPosition.toInt()); propertyStorer() },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = maxVal - minVal - 1,
            valueRange = minVal.toFloat()..maxVal.toFloat()
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