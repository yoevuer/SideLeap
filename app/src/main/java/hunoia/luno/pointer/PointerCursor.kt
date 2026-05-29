package hunoia.luno.pointer
import hunoia.luno.ui.theme.*

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import hunoia.luno.ui.theme.AnimRipple
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import hunoia.luno.action.Action
import hunoia.luno.action.pointerSettings
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.config.model.GestureSettings.PointerTrailStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PointerCursor(
    position: Offset,
    modifier: Modifier = Modifier,
    settings: GestureSettings.Pointer = GestureSettings.Pointer(),
    clickPulseKey: Int = 0,
) {
    val trail = remember { mutableStateListOf<Offset>() }
    var pulse by remember { mutableStateOf(0f) }
    val rippleAnim = remember { Animatable(0f) }
    val accentColor = MaterialTheme.colorScheme.primary
    val trailStrength = settings.trailStrength.coerceIn(0.5f, 2f)
    val trailAlpha = settings.trailAlpha.coerceIn(0.2f, 1f)
    val maxTrailSize = (12 * trailStrength).toInt().coerceIn(6, 24)
    LaunchedEffect(position, settings.trailStyle, maxTrailSize) {
        if (settings.trailStyle != PointerTrailStyle.None && position.x.isFinite() && position.y.isFinite()) {
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
    LaunchedEffect(clickPulseKey, settings.clickAnimationEnabled) {
        if (!settings.clickAnimationEnabled || clickPulseKey == 0) return@LaunchedEffect
        rippleAnim.snapTo(0f)
        rippleAnim.animateTo(1f, animationSpec = tween(durationMillis = AnimRipple.toInt(), easing = LinearEasing))
        rippleAnim.snapTo(0f)
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        if (!position.x.isFinite() || !position.y.isFinite()) return@Canvas
        val baseColor = accentColor.copy(alpha = settings.cursorAlpha)
        val radius = settings.cursorSizeDp.dp.toPx() / 2f
        val ringRadius = radius * (1f - pulse * 0.12f)
        if (settings.trailStyle == PointerTrailStyle.Dots) {
            val dotRadius = radius * (0.18f + 0.16f * trailStrength)
            trail.dropLast(1).forEachIndexed { index, offset ->
                val progress = (index + 1).toFloat() / trail.size
                val alpha = progress * trailAlpha * settings.cursorAlpha * 0.55f
                drawCircle(color = baseColor.copy(alpha = alpha), radius = dotRadius, center = offset)
            }
        } else if (settings.trailStyle == PointerTrailStyle.LightBand) {
            val wideStroke = radius * (0.7f + 0.35f * trailStrength)
            val coreStroke = radius * (0.22f + 0.16f * trailStrength)
            trail.zipWithNext().forEachIndexed { index, (start, end) ->
                val progress = (index + 1).toFloat() / trail.size
                val alpha = progress * trailAlpha * settings.cursorAlpha
                drawLine(color = baseColor.copy(alpha = alpha * 0.18f), start = start, end = end, strokeWidth = wideStroke, cap = StrokeCap.Round)
                drawLine(color = baseColor.copy(alpha = alpha * 0.46f), start = start, end = end, strokeWidth = coreStroke, cap = StrokeCap.Round)
            }
        }
        if (pulse > 0f) {
            drawCircle(
                color = baseColor.copy(alpha = pulse * 0.26f),
                radius = radius * (1.25f + (1f - pulse) * 1.1f),
                center = position,
                style = Stroke(width = Spacing2.toPx()),
            )
        }
        drawCircle(
            color = Color.Black.copy(alpha = 0.75f * settings.cursorAlpha),
            radius = ringRadius,
            center = position,
            style = Stroke(width = Spacing4.toPx()),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f * settings.cursorAlpha),
            radius = ringRadius,
            center = position,
            style = Stroke(width = Spacing2.toPx()),
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
        if (rippleAnim.value > 0f) {
            drawCircle(
                color = baseColor.copy(alpha = (1f - rippleAnim.value) * 0.4f),
                radius = radius * (1f + rippleAnim.value * 3f),
                center = position,
                style = Stroke(width = Spacing2.toPx()),
            )
        }
    }
}

class PointerHandle(
    internal val isActiveState: MutableState<Boolean>,
    internal val start: (Action, Offset, Offset) -> Boolean,
    internal val onDrag: (Offset) -> Boolean,
    internal val onDragEnd: () -> Unit,
    internal val onDragCancel: () -> Unit,
) {
    val isActive: Boolean get() = isActiveState.value
}

@Composable
internal fun rememberPointerHandle(
    gestureSettings: GestureSettings,
    modifier: Modifier = Modifier,
    onPointerStart: () -> Boolean,
    onPointerEnd: () -> Unit,
    onPointerSettingsUpdate: (GestureSettings.Pointer) -> Unit,
    pointerPreviousPosition: () -> Offset,
    onPointerActionAtPosition: (Int, Int, Boolean, PointerAction) -> Unit,
): PointerHandle {
    val coroutineScope = rememberCoroutineScope()
    val isActive = remember { mutableStateOf(false) }
    val cursorPosition = remember { mutableStateOf(pointerInitialPosition(gestureSettings.pointer)) }
    val touchPosition = remember { mutableStateOf(Offset.Unspecified) }
    val leftCancelEdge = remember { mutableStateOf(false) }
    val clickPulseKey = remember { mutableStateOf(0) }
    val longPressJob = remember { mutableStateOf<Job?>(null) }
    val longPressTriggered = remember { mutableStateOf(false) }
    val longPressAnchor = remember { mutableStateOf(Offset.Unspecified) }
    val pSettings = remember { mutableStateOf(gestureSettings.pointer) }

    LaunchedEffect(gestureSettings.pointer) {
        pSettings.value = gestureSettings.pointer
    }

    fun scheduleLongPress() {
        longPressJob.value?.cancel()
        longPressJob.value = null
        val s = pSettings.value
        if (!s.longPressEnabled || s.longPressDelayMs <= 0L || longPressTriggered.value) return
        longPressAnchor.value = touchPosition.value
        longPressJob.value = coroutineScope.launch {
            delay(s.longPressDelayMs)
            if (!isActive.value || longPressTriggered.value) return@launch
            if (!isPointerWithinLongPressTolerance(longPressAnchor.value, touchPosition.value, s)) return@launch
            val target = cursorPosition.value
            longPressTriggered.value = true
            clickPulseKey.value += 1
            onPointerActionAtPosition(
                target.x.roundToInt(),
                target.y.roundToInt(),
                s.continuousMode,
                PointerAction.LongPress,
            )
        }
    }

    fun finish(click: Boolean) {
        if (!isActive.value) return
        longPressJob.value?.cancel()
        longPressJob.value = null
        val target = cursorPosition.value
        isActive.value = false
        if (click && !longPressTriggered.value) {
            clickPulseKey.value += 1
            onPointerActionAtPosition(
                target.x.roundToInt(),
                target.y.roundToInt(),
                pSettings.value.continuousMode,
                PointerAction.Click,
            )
        } else if (!longPressTriggered.value) {
            onPointerEnd()
        }
        touchPosition.value = Offset.Unspecified
        leftCancelEdge.value = false
        longPressTriggered.value = false
        longPressAnchor.value = Offset.Unspecified
    }

    if (isActive.value) {
        PointerCursor(
            position = cursorPosition.value,
            modifier = modifier.fillMaxSize(),
            settings = pSettings.value,
            clickPulseKey = clickPulseKey.value,
        )
    }

    fun handleStart(action: Action, fingerPos: Offset, prevPos: Offset): Boolean {
        if (isActive.value) return false
        pSettings.value = action.pointerSettings(pSettings.value)
        onPointerSettingsUpdate(pSettings.value)
        cursorPosition.value = pointerInitialPosition(pSettings.value, prevPos)
        touchPosition.value = fingerPos
        leftCancelEdge.value = false
        longPressTriggered.value = false
        longPressAnchor.value = Offset.Unspecified
        isActive.value = true
        scheduleLongPress()
        return true
    }

    fun handleOnDrag(dragAmount: Offset): Boolean {
        touchPosition.value += dragAmount
        if (!longPressTriggered.value) {
            val still = isPointerWithinLongPressTolerance(
                longPressAnchor.value, touchPosition.value, pSettings.value
            )
            if (!still) scheduleLongPress()
        }
        val inCancelEdge = pSettings.value.continuousMode &&
            isPointerCancelGesture(touchPosition.value, pSettings.value)
        if (!inCancelEdge) {
            leftCancelEdge.value = true
        } else if (leftCancelEdge.value) {
            finish(click = false)
            return false
        }
        cursorPosition.value = movePointerCursor(cursorPosition.value, dragAmount, pSettings.value)
        return true
    }

    return remember(coroutineScope) {
        PointerHandle(
            isActiveState = isActive,
            start = ::handleStart,
            onDrag = ::handleOnDrag,
            onDragEnd = { finish(click = true) },
            onDragCancel = { finish(click = false) },
        )
    }
}
