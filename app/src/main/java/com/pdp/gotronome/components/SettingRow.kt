package com.pdp.gotronome.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

internal val SETTING_ROW_HORIZONTAL_PADDING = 8.dp
private val SETTING_ROW_MIN_HEIGHT = 56.dp

@Composable
internal fun settingLabelStyle(): TextStyle = MaterialTheme.typography.titleLarge

/** A settings row: [label] flush left, [control] flush right, one label size for every row. */
@Composable
fun SettingRow(
    label: String,
    modifier: Modifier = Modifier,
    control: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SETTING_ROW_MIN_HEIGHT)
            .padding(horizontal = SETTING_ROW_HORIZONTAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = settingLabelStyle(),
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Spacer(modifier = Modifier.weight(1f))
        control()
    }
}
