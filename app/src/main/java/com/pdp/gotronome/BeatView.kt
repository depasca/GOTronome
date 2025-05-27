package com.pdp.gotronome

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdp.gotronome.ui.theme.GOTronomeTheme

private const val TAG = "GOT-BeatView"

@Composable
fun BeatView(
    number: Int,
    beatNumber: Int,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
){
    val color = when (number) {
        beatNumber ->  if (beatNumber == 1)
            MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.background
    }
    val borderCcolor = when (number) {
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    Log.d(TAG, "Beat number: $number/${beatNumber}, color: $color")
    Box(
        modifier = modifier.padding(8.dp)
            .fillMaxSize()
            .border(
                width = 4.dp,
                color = borderCcolor,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$number",
            textAlign = TextAlign.Center,
            fontSize = 120.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Preview
@Composable
fun BeatViewPreview() {
    GOTronomeTheme {
        BeatView(
            number = 1,
            beatNumber = 1,
        )
    }
}