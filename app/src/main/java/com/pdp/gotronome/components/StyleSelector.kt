package com.pdp.gotronome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MetronomeViewModel
import com.pdp.gotronome.MockMetronomeViewModel
import com.pdp.gotronome.data.stylesFor
import com.pdp.gotronome.ui.theme.GOTronomeTheme

/** Picks the drum style; only styles with a groove for the current time signature are offered. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleSelector(
    viewModel: MetronomeViewModel,
    modifier: Modifier = Modifier,
) {
    val timeSignature by viewModel.timeSignature.collectAsStateWithLifecycle()
    val effectiveStyle by viewModel.effectiveStyle.collectAsStateWithLifecycle()
    val options = stylesFor(viewModel.styles, timeSignature)

    Column(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Style",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        FlowRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.Center,
        ) {
            options.forEach { style ->
                Button(
                    modifier = Modifier.padding(4.dp),
                    onClick = { viewModel.setStyle(style.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (style.id == effectiveStyle.id) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    ),
                ) {
                    Text(
                        text = style.name,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0EAE2)
@Composable
fun StyleSelectorPreview() {
    GOTronomeTheme {
        StyleSelector(viewModel = viewModel<MockMetronomeViewModel>())
    }
}
