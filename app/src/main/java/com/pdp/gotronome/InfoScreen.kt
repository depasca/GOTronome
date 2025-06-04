package com.pdp.gotronome

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdp.gotronome.ui.theme.GOTronomeTheme // Assuming you have this
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class) // For Card
@Composable
fun InfoScreen(
    handleClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    GOTronomeTheme { // Apply your app's theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = 30.dp, bottom = 30.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp) // Space between sections
            ) {
                // App Icon or Logo (Optional)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.gotronome_icon),
                        contentDescription = "GOTronome banner",
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "GOTronome",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { handleClick() },
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "GOTronome icon",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                }
                InfoSectionCard(
                    title = "About GOTronome",
                    icon = Icons.Filled.Info
                ) {
                    Text(
                        text = "GOTronome was initially created to help the GOT band rehearse. " +
                                "The GOT band was formed in 2023 by three 9-year-old friends and has performed at school events in front of big and enthusiastic crowds ever since. " +
                                "When learning a new tune it is useful for the band to practice with a metronome, though most metronomes are not designed for a band setting. " +
                                "GOTronome is meant to be simple and effective: big bright visuals let all band members see the beats even if they can't hear them - the band rocks much louder than the metronome :-) " +
                                "We believe that this app can be useful to other musicians, who are looking for a lightweight simple and effective metronome. Therefore we published the app and hope you will enjoy it as we do.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify, // Justify for a block-text feel
                        lineHeight = 20.sp
                    )
                }

                InfoSectionCard(
                    title = "Our Promise",
                    icon = Icons.Filled.FavoriteBorder
                ) {
                    Text(
                        text = "This app will always be free, without ads, and without in-app purchases of any kind. We made it for fun and to help fellow musicians!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp

                    )
                }

                InfoSectionCard(
                    title = "Open Source",
                    icon = Icons.Filled.Star
                ) {
                    val githubUrl = "https://github.com/depasca/GOTronome"
                    Text(
                        text = "Love coding? GOTronome is open source! Check out the source code, contribute, or just see how it's made:",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = githubUrl,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, githubUrl.toUri())
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp) // Add padding for better click target
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Extra space at the bottom
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge, // Updated style
                )
            }
            content()
        }
    }
}

@Preview(showBackground = true, name = "Info Screen Light")
@Composable
fun InfoScreenPreviewLight() {
    GOTronomeTheme(darkTheme = false) {
        InfoScreen()
    }
}

@Preview(showBackground = true, name = "Info Screen Dark")
@Composable
fun InfoScreenPreviewDark() {
    GOTronomeTheme(darkTheme = true) {
        InfoScreen()
    }
}