package hunoia.sideleap.ui.screen.gestureangles

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import hunoia.sideleap.R
import hunoia.sideleap.gesture.GESTURE_ANGLE_BASE
import hunoia.sideleap.gesture.GestureAngle
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.gesture.copyNew
import hunoia.sideleap.gesture.defaultGestureAngleFor
import hunoia.sideleap.gesture.getArcDegrees
import hunoia.sideleap.gesture.getDegree
import hunoia.sideleap.gesture.getDegrees
import hunoia.sideleap.gesture.getKProperty
import hunoia.sideleap.settings.api.SettingsUiDefaults.GestureButtonColorAlpha
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVertical
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.SectionPadding
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
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

        Text(
            text = stringResource(id = R.string.gesture_button_angles_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 360.dp)
                .padding(top = SectionPadding)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                    shape = MaterialTheme.shapes.extraLarge
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(8.dp)
        ) {
            AdjustAngle(
                modifier = Modifier.matchParentSize(),
                angle = angle,
                onAngleChange = { angle = it },
                position = gestureButton.position,
                color = color
            )
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
private fun AdjustAngle(
    onAngleChange: (GestureAngle) -> Unit,
    angle: GestureAngle,
    modifier: Modifier = Modifier,
    position: Position = Position.Left,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val lineWidth = when (position == Position.Bottom) {
        true -> 4.dp
        else -> 5.dp
    }
    val dragHandleRadius = when (position == Position.Bottom) {
        true -> 16.dp
        else -> 18.dp
    }
    var circleRadius by remember { mutableFloatStateOf(0f) }
    var circleCenter by remember { mutableStateOf(Offset.Zero) }
    var viewBounds by remember { mutableStateOf(Rect.Zero) }
    val degrees = remember(angle) { angle.getDegrees() }
    val arcDegrees = remember(angle) { angle.getArcDegrees() }
    val textMeasurer = rememberTextMeasurer()
    val context = LocalContext.current
    Canvas(
        modifier = modifier.let {
            val density = LocalDensity.current
            val minGapP by remember(density, dragHandleRadius) {
                derivedStateOf {
                    if (circleRadius <= 0f) return@derivedStateOf 0f
                    val opposite = density.run { dragHandleRadius.toPx() }
                    val sinVal = (opposite / circleRadius).coerceIn(0f, 1f)
                    val radians = sin(sinVal)
                    Math.toDegrees(radians.toDouble()).toFloat() / GESTURE_ANGLE_BASE
                }
            }
            val curOnAngleChange by rememberUpdatedState(newValue = onAngleChange)
            val curAngle by rememberUpdatedState(newValue = angle)
            val curPosition by rememberUpdatedState(newValue = position)
            it.pointerInput(dragHandleRadius) {
                var dragOffset = Offset.Zero
                var property: KProperty0<Float>? = null
                detectDragGestures(
                    onDragStart = { offset ->
                        dragOffset = offset
                        val p = curAngle.ps.find { p ->
                            val index = curAngle.ps.indexOf(p)
                            val degree = curAngle.getDegree(index)
                            val pOffset = calcDragHandleOffset(curPosition, circleCenter, circleRadius, degree)
                            val bounds = Rect(center = pOffset, radius = dragHandleRadius.toPx() * 1.25f)
                            bounds.contains(offset)
                        }
                        property = curAngle.getKProperty(p)
                    },
                    onDrag = onDrag@{ _, dragAmount ->
                        dragOffset += dragAmount
                        if (!viewBounds.contains(dragOffset)) return@onDrag
                        val target = property ?: return@onDrag
                        val opposite = when (curPosition) {
                            Position.Left -> dragOffset.x
                            Position.Right -> circleCenter.x - dragOffset.x
                            Position.Bottom -> circleCenter.y - dragOffset.y
                        }
                        val neighbor = when (curPosition) {
                            Position.Left, Position.Right -> circleCenter.y - dragOffset.y
                            Position.Bottom -> circleCenter.x - dragOffset.x
                        }
                        val radians = atan(opposite / neighbor)
                        var newDegree = Math.toDegrees(radians.toDouble())
                        if (newDegree < 0f) {
                            newDegree = 90f + (newDegree + 90f)
                        }
                        val newAngle = curAngle.copyNew(
                            fieldName = target.name,
                            newP = (newDegree / GESTURE_ANGLE_BASE).toFloat(),
                            minGapP = minGapP
                        )
                        curOnAngleChange(newAngle)
                    },
                    onDragEnd = {
                        dragOffset = Offset.Zero
                        property = null
                    },
                    onDragCancel = {
                        dragOffset = Offset.Zero
                        property = null
                    }
                )
            }
        }
    ) {
        val radius = when (position) {
            Position.Left, Position.Right -> size.minDimension / 2.25f
            Position.Bottom -> size.minDimension / 2.45f
        }
        val myCenter = when (position) {
            Position.Left -> center.copy(x = 0f)
            Position.Right -> center.copy(x = size.width)
            Position.Bottom -> center.copy(y = size.height)
        }
        circleRadius = radius
        circleCenter = myCenter
        viewBounds = Rect(offset = Offset.Zero, size = size)
        val lineWidthPx = lineWidth.toPx()
        val pointRadiusPx = dragHandleRadius.toPx()

        clipRect {
            drawCircle(color = color, radius = radius, center = myCenter, alpha = 0.08f)
            drawCircle(
                color = color,
                radius = radius,
                center = myCenter,
                alpha = 0.35f,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        degrees.fastForEach { degree ->
            val offset = calcDragHandleOffset(position, myCenter, radius, degree)
            drawLine(color = color, start = myCenter, end = offset, strokeWidth = lineWidthPx)
            drawCircle(color = Color.White, radius = pointRadiusPx + 3.dp.toPx(), center = offset)
            drawCircle(color = color, radius = pointRadiusPx, center = offset)
        }

        drawCircle(color = color, radius = lineWidthPx, center = myCenter)

        arcDegrees.fastForEachIndexed { index, arcDegree ->
            val degree = degrees.getOrNull(index) ?: GESTURE_ANGLE_BASE
            val (textX, textY) = calcDragHandleOffset(
                position = position,
                circleCenter = myCenter,
                circleRadius = radius + 38.dp.toPx(),
                pDegree = degree - (arcDegree / 2f)
            )
            val hint = gestureHint(context, index, position)
            val displayText = "$hint ${arcDegree.roundToInt()}°"
            val measured = textMeasurer.measure(displayText)
            val x = when (position) {
                Position.Left, Position.Bottom -> textX - measured.size.width / 2f
                Position.Right -> textX - measured.size.width
            }.coerceIn(0f, size.width - measured.size.width)
            val y = when (position) {
                Position.Left, Position.Right -> textY - measured.size.height / 2f
                Position.Bottom -> textY - measured.size.height
            }.coerceIn(0f, size.height - measured.size.height)
            drawText(
                textMeasurer = textMeasurer,
                text = displayText,
                topLeft = Offset(x = x, y = y),
                style = TextStyle.Default.copy(
                    color = color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

private fun gestureHint(context: android.content.Context, index: Int, position: Position): String {
    return when (index) {
        0 -> when (position) {
            Position.Left, Position.Right -> context.getString(R.string.gesture_to_top)
            Position.Bottom -> context.getString(R.string.gesture_to_left)
        }
        1 -> when (position) {
            Position.Left -> context.getString(R.string.gesture_to_right_top)
            Position.Right -> context.getString(R.string.gesture_to_left_top)
            Position.Bottom -> context.getString(R.string.gesture_to_top_left)
        }
        2 -> when (position) {
            Position.Left -> context.getString(R.string.gesture_to_right)
            Position.Right -> context.getString(R.string.gesture_to_left)
            Position.Bottom -> context.getString(R.string.gesture_to_top)
        }
        3 -> when (position) {
            Position.Left -> context.getString(R.string.gesture_to_right_bottom)
            Position.Right -> context.getString(R.string.gesture_to_left_bottom)
            Position.Bottom -> context.getString(R.string.gesture_to_top_right)
        }
        4 -> when (position) {
            Position.Left, Position.Right -> context.getString(R.string.gesture_to_bottom)
            Position.Bottom -> context.getString(R.string.gesture_to_right)
        }
        else -> ""
    }
}

private fun calcDragHandleOffset(
    position: Position,
    circleCenter: Offset,
    circleRadius: Float,
    pDegree: Float
): Offset {
    val transformedDegree = when (pDegree > 90f) {
        true -> GESTURE_ANGLE_BASE - pDegree
        else -> pDegree
    }
    val radians = Math.toRadians(transformedDegree.toDouble())
    val sin = sin(radians)
    val opposite = circleRadius * sin
    val neighbor = sqrt(circleRadius.pow(2) - opposite.pow(2))
    val x = when (position) {
        Position.Left -> circleCenter.x + opposite.toFloat()
        Position.Right -> circleCenter.x - opposite.toFloat()
        Position.Bottom -> when (pDegree > 90f) {
            true -> circleCenter.x + neighbor.toFloat()
            else -> circleCenter.x - neighbor.toFloat()
        }
    }
    val y = when (position) {
        Position.Left, Position.Right -> when (pDegree > 90f) {
            true -> circleCenter.y + neighbor.toFloat()
            else -> circleCenter.y - neighbor.toFloat()
        }
        Position.Bottom -> circleCenter.y - opposite.toFloat()
    }
    return Offset(x = x, y = y)
}
