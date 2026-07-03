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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.components.AppMenu
import com.pdp.gotronome.components.BasicSettingsCard
import com.pdp.gotronome.components.ModeToggle
import com.pdp.gotronome.components.NumSelector
import com.pdp.gotronome.data.MODE_BAR_LOOP
import com.pdp.gotronome.data.MODE_SILENT_BARS
import com.pdp.gotronome.ui.theme.GOTronomeTheme

private const val TAG = "GOT-SettingsScreen"

@Composable
fun SettingsScreen(
    viewModel: MetronomeViewModel
    ) {
    val scrollState = rememberScrollState()
    val mode by viewModel.mode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 0.dp, bottom = 0.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(modifier = Modifier.weight(0.1f))
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
        Spacer(modifier = Modifier.weight(0.1f))

        FlowRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Top,
        ) {
            ModeToggle(viewModel)
            BasicSettingsCard(viewModel = viewModel)
            if (mode == MODE_SILENT_BARS) {
                NumSelector(
                    viewModel.numSilentMeasures,
                    { viewModel.setNumSilentMeasures(it) },
                    { viewModel.storeNumSilentMeasures() },
                    1,
                    10
                )
            }
            else if(mode == MODE_BAR_LOOP) {
                NumSelector(
                    viewModel.numBars,
                    { viewModel.setNumBars(it) },
                    { viewModel.storeNumBars() },
                    2,
                    32
                )
            }
        }
        Spacer(modifier = Modifier.weight(1.0f))
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
        val vm = viewModel<MockMetronomeViewModel>()
        vm.setMode("Silent bars")
        SettingsScreen(viewModel = vm)
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
        val vm = viewModel<MockMetronomeViewModel>()
        vm.setMode("Bar loop")
        SettingsScreen(viewModel = vm)
    }
}