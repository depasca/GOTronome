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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/** Picks the drum style from a dropdown; only styles with a groove for the current time signature are offered. */
@Composable
fun StyleSelector(
    viewModel: MetronomeViewModel,
    modifier: Modifier = Modifier,
) {
    val timeSignature by viewModel.timeSignature.collectAsStateWithLifecycle()
    val effectiveStyle by viewModel.effectiveStyle.collectAsStateWithLifecycle()
    val options = stylesFor(viewModel.styles, timeSignature)
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(all = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Style",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        Box(modifier = Modifier.padding(start = 16.dp)) {
            OutlinedButton(onClick = { expanded = true }) {
                Text(
                    text = effectiveStyle.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Choose style",
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { style ->
                    DropdownMenuItem(
                        text = { Text(style.name) },
                        onClick = {
                            expanded = false
                            viewModel.setStyle(style.id)
                        },
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
