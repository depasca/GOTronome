package com.pdp.gotronome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MetronomeViewModel
import com.pdp.gotronome.MockMetronomeViewModel
import com.pdp.gotronome.data.timeSignatures

private const val TAG = "GOT-TimeSignatureSelectorVertical"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeSignatureSelector(
    viewModel: MetronomeViewModel,
) {
    val radioOptions = timeSignatures
    val selectedOption by viewModel.timeSignature.collectAsStateWithLifecycle()
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TS",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        FlowRow (
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3,
        ) {
            radioOptions.forEach { text ->
                Row(
                    Modifier
                        .selectable(
                            selected = (text == selectedOption),
                            onClick = { viewModel.setTimeSignature(text) },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        selected = (text == selectedOption),
                        onClick = null // null recommended for accessibility with screen readers
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFF0EAE2
)
@Composable
fun TimeSignatureSelectorPreview() {
    TimeSignatureSelector(
        viewModel = viewModel<MockMetronomeViewModel>()
    )
}