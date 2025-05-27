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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CircularBpmSelector(
    modifier: Modifier = Modifier,
    initialBpm: Int = 120,
    minBpm: Int = 40,
    maxBpm: Int = 240,
    onBpmChanged: (Int) -> Unit,
    gearColor: Color = MaterialTheme.colorScheme.primary,
    indicatorColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    knobRadius: Dp = 80.dp,
    sensitivity: Float = 2f // Higher value means less rotation needed for BPM change
) {
    var currentRotationAngle by remember { mutableFloatStateOf(0f) }
    var currentBpm by remember { mutableIntStateOf(initialBpm) }
    val knobRadiusPx = with(LocalDensity.current) { knobRadius.toPx() }

    // Calculate initial rotation based on initialBpm
    // This is a simplified mapping; you might need a more precise one
    LaunchedEffect(initialBpm) {
        val normalizedBpm = (initialBpm - minBpm).toFloat() / (maxBpm - minBpm)
        currentRotationAngle = normalizedBpm * 360f // Example mapping
        // Ensure BPM is updated if initialBpm changes externally
        if (currentBpm != initialBpm) {
            currentBpm = initialBpm
            onBpmChanged(initialBpm)
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
                .size(knobRadius * 2)
                .pointerInput(Unit) {
                    var previousAngle = 0f
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Calculate angle of the initial touch point
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val dx = offset.x - centerX
                            val dy = offset.y - centerY
                            previousAngle = (atan2(dy, dx) * (180f / PI)).toFloat()
                        },
                        onDrag = { change, _ ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val dx = change.position.x - centerX
                            val dy = change.position.y - centerY
                            val currentDragAngle = (atan2(dy, dx) * (180f / PI)).toFloat()

                            var deltaAngle = currentDragAngle - previousAngle
                            // Handle wrap-around (e.g., from 170 to -170 degrees)
                            if (deltaAngle > 180) deltaAngle -= 360
                            if (deltaAngle < -180) deltaAngle += 360

                            currentRotationAngle += deltaAngle

                            // Map rotation to BPM (this is a simplified example)
                            // A more robust solution would map angle ranges to BPM values
                            val bpmChange =
                                (deltaAngle * sensitivity / 10f).roundToInt() // Adjust sensitivity
                            val newBpm = (currentBpm + bpmChange)
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
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Draw the main gear/knob circle
            drawCircle(
                color = gearColor,
                radius = knobRadiusPx,
                center = Offset(centerX, centerY),
                style = Stroke(width = 20.dp.toPx()) // Make it look like a thick ring
            )

            // Draw markings or "teeth" (simplified)
            val numMarkings = 24 // Example number of markings
            for (i in 0 until numMarkings) {
                val angleRad =
                    (i * 360f / numMarkings - 90f) * (PI / 180f).toFloat() // -90 to start from top
                rotate(degrees = currentRotationAngle, pivot = Offset(centerX, centerY)) {
                    drawLine(
                        color = gearColor.copy(alpha = 0.7f),
                        start = Offset(
                            centerX + (knobRadiusPx - 25.dp.toPx()) * cos(angleRad),
                            centerY + (knobRadiusPx - 25.dp.toPx()) * sin(angleRad)
                        ),
                        end = Offset(
                            centerX + knobRadiusPx * cos(angleRad),
                            centerY + knobRadiusPx * sin(angleRad)
                        ),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }


            // Draw an indicator (e.g., a dot or line) based on the current BPM/Angle
            // This is a simplified indicator; you might want it fixed while the gear rotates under it,
            // or the indicator itself rotates with the gear.
            // For this example, let's rotate a small line on the gear.
            rotate(degrees = currentRotationAngle, pivot = Offset(centerX, centerY)) {
                drawLine(
                    color = indicatorColor,
                    start = Offset(centerX, centerY - knobRadiusPx + 5.dp.toPx()),
                    end = Offset(
                        centerX,
                        centerY - knobRadiusPx - 10.dp.toPx()
                    ), // Line pointing outwards
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircularBpmSelectorPreview() {
    CircularBpmSelector(onBpmChanged = {})
}