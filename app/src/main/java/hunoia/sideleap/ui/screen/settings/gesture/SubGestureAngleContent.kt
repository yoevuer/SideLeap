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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Velocity
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
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
    val names = listOf("右", "右上", "上", "左上", "左", "左下", "下", "右下")

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

        val nestedScrollConnection = remember {
            object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): Offset = available
                override fun onPostScroll(consumed: Offset, available: Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): Offset = Offset.Zero
                override suspend fun onPreFling(available: Velocity): Velocity = available
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .nestedScroll(nestedScrollConnection)
                .background(color = surfaceColor, shape = MaterialTheme.shapes.extraLarge)
                .border(width = 1.dp, color = outlineColor, shape = MaterialTheme.shapes.extraLarge)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .pointerInput(angle) {
                        detectDragGestures { change, _ ->
                            val c = Offset(size.width / 2f, size.height / 2f)
                            val r = minOf(size.width, size.height) / 2f * 0.85f
                            val dx = change.position.x - c.x
                            val dy = change.position.y - c.y
                            var norm = (atan2(-dy, dx) / (2f * PI.toFloat()))
                            if (norm < 0f) norm += 1f
                            if (angle.boundaries.size != 8) return@detectDragGestures
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
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = minOf(size.width, size.height) / 2f * 0.85f

                drawCircle(color = outlineColor, radius = r, center = c, style = Stroke(width = 2f))
                drawCircle(color = outlineColor, radius = 4f, center = c)

                if (angle.boundaries.size == 8) {
                    angle.boundaries.forEachIndexed { index, bound ->
                        val angleRad = bound * 2f * PI.toFloat()
                        val px = c.x + r * cos(angleRad)
                        val py = c.y + r * sin(angleRad)
                        drawLine(color = color, start = c, end = Offset(px, py), strokeWidth = 3f)
                        drawCircle(color = androidx.compose.ui.graphics.Color.White, radius = 12f, center = Offset(px, py))
                        drawCircle(color = color, radius = 8f, center = Offset(px, py))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
