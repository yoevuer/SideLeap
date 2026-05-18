package hunoia.sideleap.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import hunoia.sideleap.settings.model.GestureSettings
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
    LaunchedEffect(position, settings.trailEnabled) {
        if (settings.trailEnabled && position.x.isFinite() && position.y.isFinite()) {
            trail.add(position)
            while (trail.size > 8) trail.removeAt(0)
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
        val baseColor = Color(settings.cursorColor.toInt()).copy(alpha = settings.cursorAlpha)
        val radius = settings.cursorSizeDp.dp.toPx() / 2f
        if (settings.trailEnabled) {
            trail.forEachIndexed { index, offset ->
                val alpha = (index + 1).toFloat() / trail.size * settings.cursorAlpha * 0.35f
                drawCircle(
                    color = baseColor.copy(alpha = alpha),
                    radius = radius * 0.65f,
                    center = offset,
                )
            }
        }
        if (settings.shadowEnabled) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.22f * settings.cursorAlpha),
                radius = radius * 1.25f,
                center = position + Offset(radius * 0.12f, radius * 0.12f),
            )
        }
        drawCircle(
            color = baseColor,
            radius = radius,
            center = position,
        )
        if (settings.outerRingEnabled) {
            drawCircle(
                color = Color.White.copy(alpha = 0.7f * settings.cursorAlpha),
                radius = radius * 1.22f,
                center = position,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
        if (pulse > 0f) {
            drawCircle(
                color = baseColor.copy(alpha = pulse * 0.35f),
                radius = radius * (1.4f + (1f - pulse) * 1.6f),
                center = position,
                style = Stroke(width = 3.dp.toPx()),
            )
        }
    }
}
