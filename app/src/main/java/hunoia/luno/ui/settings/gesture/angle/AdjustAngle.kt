package hunoia.luno.ui.settings.gesture.angle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.fastForEach
import hunoia.luno.config.model.GESTURE_ANGLE_BASE
import hunoia.luno.config.model.GestureAngle
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.getDegree
import hunoia.luno.config.model.getDegrees
import hunoia.luno.ui.theme.Spacing1
import hunoia.luno.ui.theme.Spacing10
import hunoia.luno.ui.theme.Spacing2
import kotlin.reflect.KProperty0

@Composable
internal fun AdjustAngle(
    onAngleChange: (GestureAngle) -> Unit,
    angle: GestureAngle,
    modifier: Modifier = Modifier,
    position: Position = Position.Left,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val lineWidth = 3.dp
    val dragHandleRadius = Spacing10
    val dragHitRadius = 26.dp
    var circleRadius by remember { mutableFloatStateOf(0f) }
    var circleCenter by remember { mutableStateOf(Offset.Zero) }
    var viewBounds by remember { mutableStateOf(Rect.Zero) }
    val degrees = remember(angle) { angle.getDegrees() }
    val density = LocalDensity.current
    Canvas(
        modifier = modifier.let {
            val curOnAngleChange by rememberUpdatedState(newValue = onAngleChange)
            val curAngle by rememberUpdatedState(newValue = angle)
            val curPosition by rememberUpdatedState(newValue = position)
            it.pointerInput(dragHitRadius) {
                var dragOffset = Offset.Zero
                var candidates = emptyList<KProperty0<Float>>()
                var property: KProperty0<Float>? = null
                detectDragGestures(
                    onDragStart = { offset ->
                        dragOffset = offset
                        candidates = curAngle.ps.mapIndexedNotNull { index, _ ->
                            val degree = curAngle.getDegree(index)
                            val pOffset = calcOffset(circleCenter, circleRadius, toScreenDegree(curPosition, degree))
                            val bounds = Rect(center = pOffset, radius = with(density) { dragHitRadius.toPx() })
                            if (bounds.contains(offset)) curAngle.getKProperty(index) else null
                        }
                        property = candidates.singleOrNull()
                    },
                    onDrag = onDrag@{ _, dragAmount ->
                        dragOffset += dragAmount
                        if (!viewBounds.contains(dragOffset)) return@onDrag
                        val newDegree = toGestureDegree(curPosition, dragOffset, circleCenter)
                        val newP = newDegree / GESTURE_ANGLE_BASE
                        val target = property ?: selectDragTarget(curAngle, candidates, newP) ?: return@onDrag
                        property = target
                        val newAngle = curAngle.copyNewNoGap(fieldName = target.name, newP = newP)
                        curOnAngleChange(newAngle)
                    },
                    onDragEnd = {
                        dragOffset = Offset.Zero
                        candidates = emptyList()
                        property = null
                    },
                    onDragCancel = {
                        dragOffset = Offset.Zero
                        candidates = emptyList()
                        property = null
                    }
                )
            }
        }
    ) {
        val dialRadius = size.minDimension * 0.26f
        val lineRadius = size.minDimension * 0.39f
        circleRadius = lineRadius
        val myCenter = center
        circleCenter = myCenter
        viewBounds = Rect(offset = Offset.Zero, size = size)
        val lineWidthPx = lineWidth.toPx()
        val pointRadiusPx = dragHandleRadius.toPx()

        drawCircle(color = color, radius = dialRadius, center = myCenter, alpha = 0.08f)
        drawCircle(color = color, radius = dialRadius, center = myCenter, alpha = 0.25f, style = Stroke(width = Spacing2.toPx()))
        drawCircle(color = color, radius = lineRadius, center = myCenter, alpha = 0.18f, style = Stroke(width = Spacing1.toPx()))

        listOf(0f, GESTURE_ANGLE_BASE).fastForEach { degree ->
            drawLine(color = color, start = myCenter, end = calcOffset(myCenter, lineRadius, toScreenDegree(position, degree)), strokeWidth = Spacing1.toPx(), alpha = 0.35f)
        }

        degrees.fastForEach { degree ->
            val offset = calcOffset(myCenter, lineRadius, toScreenDegree(position, degree))
            drawLine(color = color, start = myCenter, end = offset, strokeWidth = lineWidthPx)
            drawCircle(color = Color.White, radius = pointRadiusPx + Spacing2.toPx(), center = offset)
            drawCircle(color = color, radius = pointRadiusPx, center = offset)
        }

        drawCircle(color = color, radius = 7.dp.toPx(), center = myCenter)
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = myCenter)
    }
}

internal fun GestureAngle.copyNewNoGap(fieldName: String, newP: Float): GestureAngle {
    val p = newP.coerceIn(0f, 1f)
    return when (fieldName) {
        ::p1.name -> copy(p1 = p.coerceAtMost(p2))
        ::p2.name -> copy(p2 = p.coerceIn(p1, p3))
        ::p3.name -> copy(p3 = p.coerceIn(p2, p4))
        ::p4.name -> copy(p4 = p.coerceAtLeast(p3))
        else -> this
    }
}

internal fun selectDragTarget(
    angle: GestureAngle,
    candidates: List<KProperty0<Float>>,
    newP: Float
): KProperty0<Float>? {
    if (candidates.isEmpty()) return null
    if (candidates.size == 1) return candidates.first()
    val baseline = candidates.map { angle.getP(it.name) }.average().toFloat()
    return when (newP >= baseline) {
        true -> candidates.maxBy { angle.indexOfP(it.name) }
        false -> candidates.minBy { angle.indexOfP(it.name) }
    }
}

internal fun GestureAngle.getP(fieldName: String): Float = when (fieldName) {
    ::p1.name -> p1
    ::p2.name -> p2
    ::p3.name -> p3
    ::p4.name -> p4
    else -> 0f
}

internal fun GestureAngle.indexOfP(fieldName: String): Int = when (fieldName) {
    ::p1.name -> 1
    ::p2.name -> 2
    ::p3.name -> 3
    ::p4.name -> 4
    else -> 0
}

internal fun GestureAngle.getKProperty(index: Int): KProperty0<Float>? = when (index) {
    0 -> ::p1
    1 -> ::p2
    2 -> ::p3
    3 -> ::p4
    else -> null
}
