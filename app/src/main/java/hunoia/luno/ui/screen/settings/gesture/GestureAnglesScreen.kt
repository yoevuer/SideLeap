package hunoia.luno.ui.screen.settings.gesture

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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import hunoia.luno.R
import hunoia.luno.gesture.GESTURE_ANGLE_BASE
import hunoia.luno.gesture.GestureAngle
import hunoia.luno.gesture.GestureButton
import hunoia.luno.gesture.Position
import hunoia.luno.gesture.defaultGestureAngleFor
import hunoia.luno.gesture.getDegree
import hunoia.luno.gesture.getArcDegrees
import hunoia.luno.gesture.getDegrees
import hunoia.luno.gesture.getKProperty
import hunoia.luno.settings.defaults.SettingsUiDefaults.GestureButtonColorAlpha
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVertical
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.SectionPadding
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.reflect.KProperty0

@Composable
fun GestureButtonAngleContent(
    gestureButton: GestureButton,
    onDismiss: () -> Unit,
    onSave: (GestureAngle) -> Unit
) {
    var angle by remember(gestureButton.id, gestureButton.position, gestureButton.angle) {
        mutableStateOf(gestureButton.angle)
    }
    val color = when (gestureButton.isDefault) {
        true -> MaterialTheme.colorScheme.primary
        else -> Color(gestureButton.color)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPaddingHorizontal, vertical = ContentPaddingVertical)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItemPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(color.copy(alpha = GestureButtonColorAlpha), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.gesture_angles),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = gestureButtonTitle(gestureButton.position),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = { angle = defaultGestureAngleFor(gestureButton.position) }) {
                Icon(Icons.Default.Restore, contentDescription = null)
                Text(text = stringResource(id = R.string.reset))
            }
        }

        Spacer(modifier = Modifier.height(SectionPadding))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                    shape = MaterialTheme.shapes.extraLarge
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column {
                AdjustAngle(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    angle = angle,
                    onAngleChange = { angle = it },
                    position = gestureButton.position,
                    color = color
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val sectorNames = gestureButtonSectorNames(gestureButton.position)
                    val arcDegrees = angle.getArcDegrees()
                    sectorNames.forEachIndexed { index: Int, name: String ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${arcDegrees[index].roundToInt()}°",
                                color = color,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SectionPadding),
            horizontalArrangement = Arrangement.spacedBy(ItemPadding)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onDismiss
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = { onSave(angle) }
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        }
    }
}

@Composable
private fun gestureButtonTitle(position: Position): String {
    return when (position) {
        Position.Left -> stringResource(id = R.string.left_gesture_button)
        Position.Right -> stringResource(id = R.string.right_gesture_button)
        Position.Bottom -> stringResource(id = R.string.bottom_gesture_button)
    }
}

@Composable
private fun gestureButtonSectorNames(position: Position): List<String> {
    return when (position) {
        Position.Bottom -> listOf(
            stringResource(R.string.gesture_angle_left2),
            stringResource(R.string.left),
            stringResource(R.string.direction_center),
            stringResource(R.string.right),
            stringResource(R.string.gesture_angle_right2)
        )
        else -> listOf(
            stringResource(R.string.gesture_angle_top2),
            stringResource(R.string.top),
            stringResource(R.string.direction_center),
            stringResource(R.string.bottom),
            stringResource(R.string.gesture_angle_bottom2)
        )
    }
}

@Composable
private fun AdjustAngle(
    onAngleChange: (GestureAngle) -> Unit,
    angle: GestureAngle,
    modifier: Modifier = Modifier,
    position: Position = Position.Left,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val lineWidth = 3.dp
    val dragHandleRadius = 10.dp
    val dragHitRadius = 26.dp
    var circleRadius by remember { mutableFloatStateOf(0f) }
    var circleCenter by remember { mutableStateOf(Offset.Zero) }
    var viewBounds by remember { mutableStateOf(Rect.Zero) }
    val degrees = remember(angle) { angle.getDegrees() }
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
                            val bounds = Rect(center = pOffset, radius = dragHitRadius.toPx())
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
                        val newAngle = curAngle.copyNewNoGap(
                            fieldName = target.name,
                            newP = newP
                        )
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
        drawCircle(
            color = color,
            radius = dialRadius,
            center = myCenter,
            alpha = 0.25f,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = color,
            radius = lineRadius,
            center = myCenter,
            alpha = 0.18f,
            style = Stroke(width = 1.dp.toPx())
        )

        listOf(0f, GESTURE_ANGLE_BASE).fastForEach { degree ->
            drawLine(
                color = color,
                start = myCenter,
                end = calcOffset(myCenter, lineRadius, toScreenDegree(position, degree)),
                strokeWidth = 1.dp.toPx(),
                alpha = 0.35f
            )
        }

        degrees.fastForEach { degree ->
            val offset = calcOffset(myCenter, lineRadius, toScreenDegree(position, degree))
            drawLine(color = color, start = myCenter, end = offset, strokeWidth = lineWidthPx)
            drawCircle(color = Color.White, radius = pointRadiusPx + 2.dp.toPx(), center = offset)
            drawCircle(color = color, radius = pointRadiusPx, center = offset)
        }

        drawCircle(color = color, radius = 7.dp.toPx(), center = myCenter)
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = myCenter)
    }
}

private fun GestureAngle.copyNewNoGap(fieldName: String, newP: Float): GestureAngle {
    val p = newP.coerceIn(0f, 1f)
    return when (fieldName) {
        ::p1.name -> copy(p1 = p.coerceAtMost(p2))
        ::p2.name -> copy(p2 = p.coerceIn(p1, p3))
        ::p3.name -> copy(p3 = p.coerceIn(p2, p4))
        ::p4.name -> copy(p4 = p.coerceAtLeast(p3))
        else -> this
    }
}

private fun selectDragTarget(
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

private fun GestureAngle.getP(fieldName: String): Float {
    return when (fieldName) {
        ::p1.name -> p1
        ::p2.name -> p2
        ::p3.name -> p3
        ::p4.name -> p4
        else -> 0f
    }
}

private fun GestureAngle.indexOfP(fieldName: String): Int {
    return when (fieldName) {
        ::p1.name -> 1
        ::p2.name -> 2
        ::p3.name -> 3
        ::p4.name -> 4
        else -> 0
    }
}

private fun GestureAngle.getKProperty(index: Int): KProperty0<Float>? {
    return when (index) {
        0 -> ::p1
        1 -> ::p2
        2 -> ::p3
        3 -> ::p4
        else -> null
    }
}

private fun calcOffset(center: Offset, radius: Float, degree: Float): Offset {
    val radians = Math.toRadians(degree.toDouble())
    return Offset(
        x = center.x + radius * cos(radians).toFloat(),
        y = center.y + radius * sin(radians).toFloat()
    )
}

private fun toScreenDegree(position: Position, gestureDegree: Float): Float {
    return when (position) {
        Position.Left -> gestureDegree - 90f
        Position.Right -> 270f - gestureDegree
        Position.Bottom -> 180f + gestureDegree
    }
}

private fun toGestureDegree(position: Position, offset: Offset, center: Offset): Float {
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    val screenDegree = normalizeDegree(Math.toDegrees(atan2(dy, dx).toDouble()).toFloat())
    val degree = when (position) {
        Position.Left -> {
            val signed = if (screenDegree > 180f) screenDegree - 360f else screenDegree
            signed + 90f
        }
        Position.Right -> 270f - screenDegree
        Position.Bottom -> when {
            screenDegree >= 180f -> screenDegree - 180f
            screenDegree <= 90f -> GESTURE_ANGLE_BASE
            else -> 0f
        }
    }
    return degree.coerceIn(0f, GESTURE_ANGLE_BASE)
}

private fun normalizeDegree(degree: Float): Float {
    val normalized = degree % 360f
    return when (normalized < 0f) {
        true -> normalized + 360f
        else -> normalized
    }
}
