package hunoia.sideleap.ui.screen.settings.gesture

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hunoia.sideleap.R
import hunoia.sideleap.settings.model.SubGestureAngle
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SubGestureAngleContent(
    angle: SubGestureAngle,
    onAngleChange: (SubGestureAngle) -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { onAngleChange(SubGestureAngle()) }) {
                Icon(Icons.Default.Restore, contentDescription = null)
                Text(text = stringResource(id = R.string.reset))
            }
        }

        val names = listOf("右", "右上", "上", "左上", "左", "左下", "下", "右下")
        val dialColor = color

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                    shape = MaterialTheme.shapes.extraLarge
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(24.dp)
                .pointerInput(angle) {
                    detectDragGestures { change, _ ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = minOf(size.width, size.height) / 2f
                        val dx = change.position.x - center.x
                        val dy = change.position.y - center.y
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        if (dist < radius * 0.3f || dist > radius * 1.2f) return@detectDragGestures
                        var norm = (atan2(-dy, dx) / (2f * PI.toFloat()))
                        if (norm < 0f) norm += 1f
                        val newBoundaries = angle.boundaries.toMutableList()
                        val closestIndex = newBoundaries.indices.minByOrNull { i ->
                            val diff = (norm - newBoundaries[i])
                            minOf(kotlin.math.abs(diff), kotlin.math.abs(diff - 1f), kotlin.math.abs(diff + 1f))
                        } ?: return@detectDragGestures
                        val prev = if (closestIndex == 0) newBoundaries.last() - 1f else newBoundaries[closestIndex - 1]
                        val next = if (closestIndex == 7) newBoundaries.first() + 1f else newBoundaries[closestIndex + 1]
                        val clamped = norm.coerceIn(prev + 0.01f, next - 0.01f)
                        val wrapped = if (clamped < 0f) clamped + 1f else if (clamped >= 1f) clamped - 1f else clamped
                        newBoundaries[closestIndex] = wrapped
                        try {
                            onAngleChange(SubGestureAngle(boundaries = newBoundaries.sorted()))
                        } catch (_: Exception) { }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) / 2f

            drawCircle(
                color = MaterialTheme.colorScheme.outlineVariant,
                style = Stroke(width = 2f)
            )

            val sortedBounds = angle.boundaries.sorted()
            sortedBounds.forEachIndexed { index, bound ->
                val angleRad = bound * 2f * PI.toFloat()
                val px = center.x + radius * cos(angleRad)
                val py = center.y + radius * sin(angleRad)
                val nextBound = if (index == 7) sortedBounds.first() + 1f else sortedBounds[index + 1]
                val midAngle = (bound + (nextBound - bound) / 2f) * 2f * PI.toFloat()
                val labelR = radius * 0.75f
                val lx = center.x + labelR * cos(midAngle)
                val ly = center.y + labelR * sin(midAngle)

                drawLine(
                    color = dialColor,
                    start = center,
                    end = Offset(px, py),
                    strokeWidth = 2f
                )

                drawCircle(
                    color = dialColor,
                    radius = 8f,
                    center = Offset(px, py)
                )

                drawContext.canvas.save()
                drawContext.canvas.translate(lx, ly)
                drawContext.canvas.drawText(
                    names[index],
                    0,
                    0,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
                drawContext.canvas.restore()

                val degStr = "${(bound * 360f).roundToInt()}°"
                val degX = center.x + radius * 1.15f * cos(angleRad)
                val degY = center.y + radius * 1.15f * sin(angleRad)
                drawContext.canvas.save()
                drawContext.canvas.translate(degX, degY)
                drawContext.canvas.drawText(
                    degStr,
                    0,
                    0,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 22f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
                drawContext.canvas.restore()
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val degrees = angle.boundaries.map { "${(it * 360f).roundToInt()}°" }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            names.forEachIndexed { index, name ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = degrees[index],
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
