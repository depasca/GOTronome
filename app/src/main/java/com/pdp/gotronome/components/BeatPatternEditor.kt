package com.pdp.gotronome.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MetronomeViewModel
import com.pdp.gotronome.MockMetronomeViewModel
import com.pdp.gotronome.data.BEAT_ACCENT
import com.pdp.gotronome.data.BEAT_MUTE
import com.pdp.gotronome.data.BEAT_NORMAL

private fun levelFraction(level: Int): Float = when (level) {
    BEAT_ACCENT -> 1f
    BEAT_NORMAL -> 0.5f
    else -> 0f
}

@Composable
fun BeatPatternEditor(
    viewModel: MetronomeViewModel,
) {
    val pattern by viewModel.accentPattern.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Accents",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Tap a beat to cycle its sound",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            pattern.forEachIndexed { index, level ->
                BeatCell(
                    beatNumber = index + 1,
                    level = level,
                    onClick = { viewModel.cycleAccentBeat(index) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(level = BEAT_ACCENT, label = "Accent")
            LegendItem(level = BEAT_NORMAL, label = "Normal")
            LegendItem(level = BEAT_MUTE, label = "Mute")
        }
    }
}

@Composable
private fun BeatCell(
    beatNumber: Int,
    level: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Fill height conveys loudness: full = accent, half = normal, empty = mute.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(levelFraction(level))
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = beatNumber.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = if (level == BEAT_ACCENT) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun LegendItem(
    level: Int,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(levelFraction(level))
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
fun BeatPatternEditorPreview() {
    BeatPatternEditor(viewModel = viewModel<MockMetronomeViewModel>())
}
