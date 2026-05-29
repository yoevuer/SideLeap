package hunoia.luno.ui.screen.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import hunoia.luno.config.model.WaveStyle
import kotlin.math.min

@Composable
fun ShapePreview(shapeType: Int, modifier: Modifier, color: Color) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), MaterialTheme.shapes.small)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val path = Path()
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            when (shapeType) {
                WaveStyle.SHAPE_WAVE -> {
                    path.moveTo(2f, cy)
                    path.cubicTo(w * 0.3f, 2f, w * 0.7f, h - 2f, w - 2f, cy)
                }
                WaveStyle.SHAPE_LINE -> {
                    path.moveTo(cx, 2f)
                    path.lineTo(cx, h - 2f)
                }
            }
            drawPath(path, color = color, style = Stroke(2.5.dp.toPx()))
        }
    }
}

@Composable
fun LinePreview(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), MaterialTheme.shapes.small)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val lineH = h * 0.75f
            val top = (h - lineH) / 2f
            val strokeWidth = w * 0.18f
            drawLine(
                color = color,
                start = Offset(cx, top),
                end = Offset(cx, top + lineH),
                strokeWidth = strokeWidth.coerceAtLeast(1f),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun CapsulePreview(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), MaterialTheme.shapes.small)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val rectW = w * 0.8f
            val rectH = h * 0.55f
            val corner = rectH / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset((w - rectW) / 2f, (h - rectH) / 2f),
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(2.5.dp.toPx())
            )
        }
    }
}

@Composable
fun BubblePreview(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), MaterialTheme.shapes.small)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = minOf(size.width, size.height) * 0.42f
            drawCircle(color = color, radius = r, center = Offset(cx, cy), style = Stroke(2.5.dp.toPx()))
        }
    }
}
