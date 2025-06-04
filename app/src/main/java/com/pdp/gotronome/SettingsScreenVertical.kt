package com.pdp.gotronome

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pdp.gotronome.components.TimeSelectorVertical
import com.pdp.gotronome.components.TimeSignatureSelectorVertical
import com.pdp.gotronome.ui.theme.GOTronomeTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import com.pdp.gotronome.components.AppMenu

private const val TAG = "GOT-SettingsScreenVertical"

@Composable
fun SettingsScreenVertical(
    modifier: Modifier = Modifier,
    viewModel: MetronomeViewModel
    ) {
    Log.d(TAG, "start")
    Column(
        modifier = modifier.fillMaxSize()
            .padding(top = 30.dp, bottom = 30.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .border(1.dp, MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center

        ) {
            AppMenu({viewModel.setPage("info")})
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.gotronome_banner),
                contentDescription = "GOTronome banner"
            )
        }
        Spacer(modifier = Modifier.weight(0.5f))
        TimeSignatureSelectorVertical(modifier, viewModel)
        Spacer(modifier = Modifier.weight(0.5f))
        TimeSelectorVertical(modifier, viewModel)
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Tap anywhere to start/stop",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
    Log.d(TAG, "end")
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenVerticalPreview() {
    GOTronomeTheme {
        SettingsScreenVertical(viewModel = MockMetronomeViewModel())
    }
}