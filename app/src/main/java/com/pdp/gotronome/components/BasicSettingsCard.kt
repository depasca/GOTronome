package com.pdp.gotronome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MetronomeViewModel
import com.pdp.gotronome.MockMetronomeViewModel
import com.pdp.gotronome.ui.theme.GOTronomeTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicSettingsCard(
    title: String? = null,
    viewModel: MetronomeViewModel,
    ) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge, // Updated style
                    )
                }
            }
            FlowRow (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Top,
            ){
                TimeSignatureSelector(viewModel = viewModel)
                // BPM
                NumSelector(
                    "BPM",
                    viewModel.beatsPerMinute,
                    { viewModel.setBeatsPerMinute(it) },
                    { viewModel.storeBeatsPerMinute() },
                    20,
                    240
                )
            }
        }
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
        BasicSettingsCard(viewModel =viewModel<MockMetronomeViewModel>())
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
        BasicSettingsCard(viewModel = viewModel<MockMetronomeViewModel>())
    }
}