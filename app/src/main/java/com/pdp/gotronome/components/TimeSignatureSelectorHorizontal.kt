package com.pdp.gotronome.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdp.gotronome.MetronomeViewModel
import com.pdp.gotronome.MockMetronomeViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeSignatureSelectorHorizontal(
    modifier: Modifier = Modifier,
    viewModel: MetronomeViewModel,
) {
    val radioOptions = viewModel.timeSignatures
    val selectedOption by viewModel.selectedTimeSignature.collectAsStateWithLifecycle()
    Row (
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = "TS",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(all = 16.dp)
        )
        FlowRow (modifier.selectableGroup()) {
            radioOptions.forEach { text ->
                Row(
                    Modifier
                        .height(56.dp)
                        .selectable(
                            selected = (text == selectedOption),
                            onClick = { viewModel.setTimeSignature(text) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == selectedOption),
                        onClick = null // null recommended for accessibility with screen readers
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 16.dp)
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
fun TimeSignatureSelectorHorizontalPreview() {
    TimeSignatureSelectorHorizontal(
        viewModel = MockMetronomeViewModel()
    )
}