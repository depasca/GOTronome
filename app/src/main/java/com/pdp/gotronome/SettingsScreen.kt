package com.pdp.gotronome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.components.AppMenu
import com.pdp.gotronome.components.BasicSettingsCard
import com.pdp.gotronome.components.NumSelector
import com.pdp.gotronome.ui.theme.GOTronomeTheme

private const val TAG = "GOT-SettingsScreen"

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: MetronomeViewModel
    ) {
    Column (
        modifier = modifier.fillMaxSize()
            .padding(top = 30.dp, bottom = 0.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .border(1.dp, MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center

        ) {
            AppMenu({ viewModel.setPage("info") })
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.gotronome_banner),
                contentDescription = "GOTronome banner"
            )
        }

        FlowRow (
            modifier = Modifier.fillMaxWidth().weight(1.0f).padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.Top,
        ){
            val modifier = Modifier
            BasicSettingsCard(viewModel = viewModel)
            Spacer(modifier = Modifier.weight(1.0f))
            // Silent Bars
            NumSelector(
                "Silent Bars",
                viewModel.numSilentMeasures,
                { viewModel.setNumSilentMeasures(it) },
                { viewModel.storeNumSilentMeasures() },
                0,
                7
            )
            // Bars
            NumSelector(
                "Show Bars",
                viewModel.numBars,
                { viewModel.setNumBars(it) },
                { viewModel.storeNumBars() },
                2,
                32
            )
            Spacer(modifier = Modifier.weight(1.0f))
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Tap anywhere to start/stop",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Preview(
    name = "Vertical Preview",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
fun SettingsScreenVerticalPreview() {
    GOTronomeTheme {
        SettingsScreen(viewModel =viewModel<MockMetronomeViewModel>())
    }
}

@Preview(
    name = "Horizontal Preview (Landscape)", // Optional name
    showBackground = true,
    widthDp = 720,
    heightDp = 360
)
@Composable
fun SettingsScreenHorizontalPreview() {
    GOTronomeTheme {
        SettingsScreen(viewModel = viewModel<MockMetronomeViewModel>())
    }
}