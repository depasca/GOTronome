package com.pdp.gotronome.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MetronomeViewModel
import com.pdp.gotronome.MockMetronomeViewModel
import com.pdp.gotronome.data.modes
import com.pdp.gotronome.ui.theme.GOTronomeTheme

@Composable
fun ModeToggle(
    viewModel: MetronomeViewModel
) {
    val selectedMode by viewModel.mode.collectAsStateWithLifecycle()

    Row(modifier = Modifier.selectableGroup()) {
        modes.forEach { mode ->
            Button(
                modifier = Modifier.padding(4.dp),
                onClick = { viewModel.setMode(mode) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mode == selectedMode) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ),
            ) {
                Text(
                    text = mode,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun ModeTogglePreview() {
    GOTronomeTheme {
        val vm = viewModel<MockMetronomeViewModel>()
        vm.setMode("Basic")
        ModeToggle(vm)
    }
}
