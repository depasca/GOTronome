package com.pdp.gotronome.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Opens the Play app on our listing, falling back to the web listing if it is not installed. */
private fun openStoreListing(context: Context) {
    val listing = "details?id=${context.packageName}"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://$listing")))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/$listing"))
        )
    }
}

@Composable
fun AppMenu(
    handleClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.Menu, contentDescription = "App menu")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("About") },
                leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                onClick = { expanded = false; handleClick("info") }
            )
            DropdownMenuItem(
                text = { Text("Check for updates") },
                leadingIcon = { Icon(Icons.Outlined.Refresh, contentDescription = null) },
                onClick = { expanded = false; openStoreListing(context) }
            )
//            DropdownMenuItem(
//                text = { Text("Send Feedback") },
//                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
//                trailingIcon = { Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null) },
//                onClick = { expanded = false; handleClick("feedback")},
//            )
        }
    }
}