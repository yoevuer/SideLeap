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
import hunoia.sideleap.R
import hunoia.sideleap.settings.defaults.SettingsUiDefaults
import hunoia.sideleap.settings.model.SubGestureAngle
import hunoia.sideleap.settings.model.copyNewNoGap
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVertical
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.SectionPadding
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SubGestureAngleContent(
    angle: SubGestureAngle,
    onDismiss: () -> Unit,
    onSave: (SubGestureAngle) -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    var draftAngle by remember(angle) { mutableStateOf(angle) }
    val names = listOf("右", "右上", "上", "左上", "左", "左下", "下", "右下")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPaddingHorizontal, vertical = ContentPaddingVertical)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.sub_gesture_angles),
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = { draftAngle = SubGestureAngle() }) {
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
                SubGestureAngleDial(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    angle = draftAngle,
                    onAngleChange = { draftAngle = it },
                    color = color
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                                text = "${(draftAngle.boundaries[index] * 360f).roundToInt()}°",
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
                onClick = { onSave(draftAngle) }
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        }
    }
}

@Composable
private fun SubGestureAngleDial(
    onAngleChange: (SubGestureAngle) -> Unit,
    angle: SubGestureAngle,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val dragHandleRadius = 10.dp
    val dragHitRadius = 26.dp
    var circleRadius by remember { mutableStateOf(0f) }
    var circleCenter by remember { mutableStateOf(Offset.Zero) }
    var viewBounds by remember { mutableStateOf(Rect.Zero) }

    Canvas(
        modifier = modifier.let {
            val curOnAngleChange by rememberUpdatedState(newValue = onAngleChange)
            val curAngle by rememberUpdatedState(newValue = angle)
            it.pointerInput(dragHitRadius) {
                var dragOffset = Offset.Zero
                var candidates = emptyList<Int>()
                var candidateIndex: Int? = null
                detectDragGestures(
                    onDragStart = { offset ->
                        dragOffset = offset
                        val norm = normalizedAngle(offset, circleCenter)
                        candidates = curAngle.boundaries.indices.filter { i ->
                            val boundNorm = curAngle.boundaries[i]
                            val diff = normalizedDiff(norm, boundNorm)
                            val hitPx = (minOf(circleRadius * PI.toFloat() * 2f, circleRadius) * 0.12f)
                                .coerceAtLeast(dragHitRadius.toPx())
                            diff * 360f < hitPx.coerceAtMost(circleRadius * 0.5f)
                        }
                        candidateIndex = candidates.singleOrNull()
                    },
                    onDrag = onDrag@{ _, dragAmount ->
                        dragOffset += dragAmount
                        if (!viewBounds.contains(dragOffset)) return@onDrag
                        val norm = normalizedAngle(dragOffset, circleCenter)
                        val target = candidateIndex
                            ?: selectDragTarget(curAngle, candidates, norm)
                            ?: return@onDrag
                        candidateIndex = target
                        val newAngle = curAngle.copyNewNoGap(target, norm)
                        curOnAngleChange(newAngle)
                    },
                    onDragEnd = {
                        dragOffset = Offset.Zero
                        candidates = emptyList()
                        candidateIndex = null
                    },
                    onDragCancel = {
                        dragOffset = Offset.Zero
                        candidates = emptyList()
                        candidateIndex = null
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
        val lineWidthPx = 3.dp.toPx()
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

        angle.boundaries.forEachIndexed { index, bound ->
            val angleRad = bound * 2f * PI.toFloat()
            val offset = Offset(
                x = myCenter.x + lineRadius * cos(angleRad),
                y = myCenter.y - lineRadius * sin(angleRad)
            )
            drawLine(color = color, start = myCenter, end = offset, strokeWidth = lineWidthPx)
            drawCircle(color = Color.White, radius = pointRadiusPx + 2.dp.toPx(), center = offset)
            drawCircle(color = color, radius = pointRadiusPx, center = offset)
        }

        drawCircle(color = color, radius = 7.dp.toPx(), center = myCenter)
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = myCenter)
    }
}

private fun normalizedAngle(offset: Offset, center: Offset): Float {
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    val angle = atan2(-dy.toDouble(), dx.toDouble())
    var norm = (angle / (2 * PI)).toFloat()
    if (norm < 0f) norm += 1f
    return norm
}

private fun normalizedDiff(a: Float, b: Float): Float {
    val diff = a - b
    return minOf(kotlin.math.abs(diff), kotlin.math.abs(diff - 1f), kotlin.math.abs(diff + 1f))
}

private fun selectDragTarget(
    angle: SubGestureAngle,
    candidates: List<Int>,
    newP: Float
): Int? {
    if (candidates.isEmpty()) return null
    if (candidates.size == 1) return candidates.first()
    val baseline = candidates.map { angle.boundaries[it] }.average().toFloat()
    return if (newP >= baseline) {
        candidates.maxBy { angle.boundaries[it] }
    } else {
        candidates.minBy { angle.boundaries[it] }
    }
}
