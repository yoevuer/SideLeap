package hunoia.sideleap.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.settings.model.GestureSettings.VirtualMouseTrailStyle
import kotlinx.coroutines.delay

@Composable
fun VirtualMouseCursor(
    position: Offset,
    modifier: Modifier = Modifier,
    settings: GestureSettings.VirtualMouse = GestureSettings.VirtualMouse(),
    clickPulseKey: Int = 0,
) {
    val trail = remember { mutableStateListOf<Offset>() }
    var pulse by remember { mutableStateOf(0f) }
    val accentColor = MaterialTheme.colorScheme.primary
    val trailStrength = settings.trailStrength.coerceIn(0.5f, 2f)
    val trailAlpha = settings.trailAlpha.coerceIn(0.2f, 1f)
    val maxTrailSize = (12 * trailStrength).toInt().coerceIn(6, 24)
    LaunchedEffect(position, settings.trailStyle, maxTrailSize) {
        if (settings.trailStyle != VirtualMouseTrailStyle.None && position.x.isFinite() && position.y.isFinite()) {
            trail.add(position)
            while (trail.size > maxTrailSize) trail.removeAt(0)
        } else {
            trail.clear()
        }
    }
    LaunchedEffect(clickPulseKey, settings.clickAnimationEnabled) {
        if (!settings.clickAnimationEnabled || clickPulseKey == 0) return@LaunchedEffect
        pulse = 1f
        repeat(8) {
            delay(16)
            pulse *= 0.72f
        }
        pulse = 0f
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        if (!position.x.isFinite() || !position.y.isFinite()) return@Canvas
        val baseColor = accentColor.copy(alpha = settings.cursorAlpha)
        val radius = settings.cursorSizeDp.dp.toPx() / 2f
        val ringRadius = radius * (1f - pulse * 0.12f)
        if (settings.trailStyle == VirtualMouseTrailStyle.Dots) {
            trail.dropLast(1).forEachIndexed { index, offset ->
                val progress = (index + 1).toFloat() / trail.size
                val alpha = progress * trailAlpha * settings.cursorAlpha * 0.55f
                drawCircle(
                    color = baseColor.copy(alpha = alpha),
                    radius = radius * (0.18f + 0.16f * trailStrength),
                    center = offset
                )
            }
        } else if (settings.trailStyle == VirtualMouseTrailStyle.LightBand) {
            trail.zipWithNext().forEachIndexed { index, (start, end) ->
                val progress = (index + 1).toFloat() / trail.size
                val alpha = progress * trailAlpha * settings.cursorAlpha
                drawLine(
                    color = baseColor.copy(alpha = alpha * 0.18f),
                    start = start,
                    end = end,
                    strokeWidth = radius * (0.7f + 0.35f * trailStrength),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = baseColor.copy(alpha = alpha * 0.46f),
                    start = start,
                    end = end,
                    strokeWidth = radius * (0.22f + 0.16f * trailStrength),
                    cap = StrokeCap.Round,
                )
            }
        }
        if (pulse > 0f) {
            drawCircle(
                color = baseColor.copy(alpha = pulse * 0.26f),
                radius = radius * (1.25f + (1f - pulse) * 1.1f),
                center = position,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
        drawCircle(
            color = Color.Black.copy(alpha = 0.75f * settings.cursorAlpha),
            radius = ringRadius,
            center = position,
            style = Stroke(width = 4.dp.toPx()),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f * settings.cursorAlpha),
            radius = ringRadius,
            center = position,
            style = Stroke(width = 2.dp.toPx()),
        )
        drawCircle(
            color = baseColor,
            radius = ringRadius,
            center = position,
            style = Stroke(width = 1.2.dp.toPx()),
        )
        drawCircle(color = Color.Black.copy(alpha = 0.75f * settings.cursorAlpha), radius = radius * 0.16f, center = position)
        drawCircle(color = Color.White.copy(alpha = 0.9f * settings.cursorAlpha), radius = radius * 0.1f, center = position)
        drawCircle(color = baseColor, radius = radius * 0.06f, center = position)
    }
}
