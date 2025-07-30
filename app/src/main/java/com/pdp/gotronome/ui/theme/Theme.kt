package com.pdp.gotronome.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private const val TAG = "GOT-GOTronomeTheme"

private val GOTDarkColorScheme = darkColorScheme(
    primary =  GOTOrange,
    secondary = GOTWhite,
    tertiary = GOTDarkOrange,
    background = GOTBlack,
    surface = GOTDarkGrey,
    onSurface = GOTLightOrange
)

private val GOTLightColorScheme = lightColorScheme(
    primary = GOTOrange,
    secondary = GOTBlack,
    tertiary = GOTDarkOrange,
    background = GOTWhite,
    surface = GOTLightGrey,
    onSurface = GOTLightOrange
)

@Composable
fun GOTronomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> { // …2
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }


        darkTheme -> GOTDarkColorScheme
        else -> GOTLightColorScheme
    }

    MaterialTheme( // …3
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}