package com.pdp.gotronome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MetronomeViewModel
import com.pdp.gotronome.MockMetronomeViewModel
import com.pdp.gotronome.data.MODE_BAR_LOOP
import com.pdp.gotronome.data.MODE_SILENT_BARS
import com.pdp.gotronome.data.modes

private const val TAG = "GOT-TimeSignatureSelectorVertical"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeSelector(
    viewModel: MetronomeViewModel,
) {
    val radioOptions = modes
    val selectedMode by viewModel.mode.collectAsStateWithLifecycle()
    Column (
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Mode",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .height(36.dp)
                    .selectable(
                        selected = (text == selectedMode),
                        onClick = { viewModel.setMode(text) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    selected = (text == selectedMode),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                if (text == selectedMode) {
                    when (text) {
                        MODE_SILENT_BARS -> {
                            NumSelector(
                                viewModel.numSilentMeasures,
                                { viewModel.setNumSilentMeasures(it) },
                                { viewModel.storeNumSilentMeasures() },
                                1,
                                10
                            )
                        }
                        MODE_BAR_LOOP -> {
                            NumSelector(
                                viewModel.numBars,
                                { viewModel.setNumBars(it) },
                                { viewModel.storeNumBars() },
                                2,
                                32
                            )
                        }
                        else -> {}
                    }
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
fun ModeSelectorPreviewBasic() {
    val vm = viewModel<MockMetronomeViewModel>()
    vm.setMode("Basic")
    ModeSelector(viewModel = vm)
}
@Preview(
    showBackground = true,
    backgroundColor = 0xFFF0EAE2
)
@Composable
fun ModeSelectorPreviewAdvanced() {
    val vm = viewModel<MockMetronomeViewModel>()
    vm.setMode("Silent bars")
    ModeSelector(viewModel = vm)
}
@Preview(
    showBackground = true,
    backgroundColor = 0xFFF0EAE2
)
@Composable
fun ModeSelectorPreviewBarloop() {
    val vm = viewModel<MockMetronomeViewModel>()
    vm.setMode("Bar loop")
    ModeSelector(viewModel = vm)
}