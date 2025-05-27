package com.pdp.gotronome.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pdp.gotronome.R // Import your R file
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun CircularBpmSelectorWithImage(
    modifier: Modifier = Modifier,
    initialBpm: Int = 120,
    minBpm: Int = 40,
    maxBpm: Int = 240,
    onBpmChanged: (Int) -> Unit,
    gearImageRes: Int, // Pass the drawable resource ID for the gear
    indicatorColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    knobSize: Dp = 160.dp, // Overall size of the knob/image area
    sensitivity: Float = 2f
) {
    var currentRotationAngle by remember { mutableFloatStateOf(0f) }
    var currentBpm by remember { mutableIntStateOf(initialBpm) }
    val gearPainter: Painter = painterResource(id = gearImageRes)

    LaunchedEffect(initialBpm, minBpm, maxBpm) {
        // Calculate initial rotation based on initialBpm
        val normalizedBpm = (initialBpm.toFloat() - minBpm) / (maxBpm - minBpm)
        currentRotationAngle = normalizedBpm * 360f // Full circle mapping
        if (currentBpm != initialBpm) {
            currentBpm = initialBpm
            // onBpmChanged(initialBpm) // Already handled by the remember currentBpm
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BPM: $currentBpm",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .size(knobSize) // Use a general size for the canvas
                .pointerInput(Unit) {
                    var previousAngle = 0f
                    detectDragGestures(
                        onDragStart = { touchOffset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val dx = touchOffset.x - centerX
                            val dy = touchOffset.y - centerY
                            previousAngle = (atan2(dy, dx) * (180f / PI)).toFloat()
                        },
                        onDrag = { change, _ ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val dx = change.position.x - centerX
                            val dy = change.position.y - centerY
                            val currentDragAngle = (atan2(dy, dx) * (180f / PI)).toFloat()

                            var deltaAngle = currentDragAngle - previousAngle
                            if (deltaAngle > 180) deltaAngle -= 360
                            if (deltaAngle < -180) deltaAngle += 360

                            currentRotationAngle += deltaAngle
                            currentRotationAngle %= 360f // Keep angle within 0-360

                            // Map rotation to BPM
                            // Assuming 360 degrees of rotation maps to the full BPM range
                            val normalizedRotation = currentRotationAngle / 360f
                            val newBpm = (minBpm + normalizedRotation * (maxBpm - minBpm))
                                .roundToInt()
                                .coerceIn(minBpm, maxBpm)

                            if (newBpm != currentBpm) {
                                currentBpm = newBpm
                                onBpmChanged(currentBpm)
                            }
                            previousAngle = currentDragAngle
                            change.consume()
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val canvasCenter = Offset(canvasWidth / 2f, canvasHeight / 2f)

            // Calculate the drawing size for the image to fit within the knobSize
            // while maintaining aspect ratio (assuming gear image is somewhat circular)
            val painterIntrinsicWidth = gearPainter.intrinsicSize.width
            val painterIntrinsicHeight = gearPainter.intrinsicSize.height

            val drawSize = if (painterIntrinsicWidth > painterIntrinsicHeight) {
                Size(canvasWidth, canvasHeight * (painterIntrinsicHeight / painterIntrinsicWidth))
            } else {
                Size(canvasWidth * (painterIntrinsicWidth / painterIntrinsicHeight), canvasHeight)
            }
            val drawOffset = Offset(
                (canvasWidth - drawSize.width) / 2f,
                (canvasHeight - drawSize.height) / 2f
            )

            rotate(degrees = currentRotationAngle, pivot = canvasCenter) {
                with(gearPainter) {
                    draw(
                        size = drawSize,
//                        topLeft = drawOffset
                        // If your image already has an indicator and you want the whole image to rotate
                    )
                }
            }

            // Optional: Draw a fixed indicator (needle) on top if your gear image doesn't have one
            // Or if you want the indicator to be separate from the gear's rotation
            // For example, a small line at the top center:
            drawLine(
                color = indicatorColor,
                start = Offset(canvasCenter.x, canvasCenter.y - (drawSize.height / 2) - 5.dp.toPx()),
                end = Offset(canvasCenter.x, canvasCenter.y - (drawSize.height / 2) - 15.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircularBpmSelectorWithImagePreview() {
    MaterialTheme {
        CircularBpmSelectorWithImage(
            initialBpm = 100,
            onBpmChanged = { /* Preview BPM: it */ },
            gearImageRes = R.drawable.ic_launcher_foreground // Replace with your actual gear image resource
            // Make sure you have a placeholder or actual gear image in drawables
        )
    }
}