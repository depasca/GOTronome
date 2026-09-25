package com.pdp.gotronome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MetronomeViewModel
import com.pdp.gotronome.MockMetronomeViewModel
import com.pdp.gotronome.data.rootNames
import com.pdp.gotronome.ui.theme.GOTronomeTheme

/** Bass on/off and the root note; shown only for styles that carry a bass line. */
@Composable
fun BassControls(
    viewModel: MetronomeViewModel,
    modifier: Modifier = Modifier,
) {
    val enabled by viewModel.bassEnabled.collectAsStateWithLifecycle()
    val root by viewModel.bassRoot.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(all = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Bass",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        Switch(
            checked = enabled,
            onCheckedChange = { viewModel.setBassEnabled(it) },
            modifier = Modifier.padding(start = 16.dp).semantics { contentDescription = "Bass on/off" },
        )
        Box(modifier = Modifier.padding(start = 16.dp)) {
            OutlinedButton(onClick = { expanded = true }, enabled = enabled) {
                Text(
                    text = "Root ${rootNames[root]}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Choose root",
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                rootNames.forEachIndexed { pitchClass, name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            expanded = false
                            viewModel.setBassRoot(pitchClass)
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0EAE2)
@Composable
fun BassControlsPreview() {
    GOTronomeTheme {
        BassControls(viewModel = viewModel<MockMetronomeViewModel>())
    }
}
