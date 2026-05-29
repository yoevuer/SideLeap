package hunoia.luno.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import hunoia.luno.config.model.AnimationStyle
import hunoia.luno.config.model.BubbleStyle
import hunoia.luno.config.model.CapsuleStyle
import hunoia.luno.config.model.LineStyle
import hunoia.luno.config.model.SnapBackDefaults
import hunoia.luno.config.model.SnapBackType
import hunoia.luno.config.model.WaveStyle
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.Position

@Composable
fun GestureAnimation(
    animationStyle: AnimationStyle,
    SideGestureState: SideGestureState,
    modifier: Modifier = Modifier
) {
    val rawFingerX = SideGestureState.fingerXAnimVal
    val rawFingerY = SideGestureState.fingerYAnimVal
    val rawOriginX = SideGestureState.originXAnimVal
    val rawOriginY = SideGestureState.originYAnimVal
    val isActive = !rawFingerX.isNaN()

    var lastFingerX by remember { mutableStateOf(Float.NaN) }
    var lastFingerY by remember { mutableStateOf(Float.NaN) }
    var lastOriginX by remember { mutableStateOf(Float.NaN) }
    var lastOriginY by remember { mutableStateOf(Float.NaN) }

    if (isActive) {
        lastFingerX = rawFingerX
        lastFingerY = rawFingerY
        lastOriginX = rawOriginX
        lastOriginY = rawOriginY
    }

    val snapParams = remember(animationStyle) {
        val type = when (animationStyle) {
            is WaveStyle -> animationStyle.snapBackType
            is CapsuleStyle -> animationStyle.snapBackType
            is BubbleStyle -> animationStyle.snapBackType
            is LineStyle -> animationStyle.snapBackType
            else -> SnapBackType.SPRING
        }
        val stiffness = when (animationStyle) {
            is WaveStyle -> animationStyle.snapBackSpringStiffness
            is CapsuleStyle -> animationStyle.snapBackSpringStiffness
            is BubbleStyle -> animationStyle.snapBackSpringStiffness
            is LineStyle -> animationStyle.snapBackSpringStiffness
            else -> SnapBackDefaults.SpringStiffness
        }
        val damping = when (animationStyle) {
            is WaveStyle -> animationStyle.snapBackSpringDamping
            is CapsuleStyle -> animationStyle.snapBackSpringDamping
            is BubbleStyle -> animationStyle.snapBackSpringDamping
            is LineStyle -> animationStyle.snapBackSpringDamping
            else -> SnapBackDefaults.SpringDamping
        }
        val duration = when (animationStyle) {
            is WaveStyle -> animationStyle.snapBackEaseOutDurationMs
            is CapsuleStyle -> animationStyle.snapBackEaseOutDurationMs
            is BubbleStyle -> animationStyle.snapBackEaseOutDurationMs
            is LineStyle -> animationStyle.snapBackEaseOutDurationMs
            else -> SnapBackDefaults.EaseOutDurationMs
        }
        val elastic = when (animationStyle) {
            is WaveStyle -> animationStyle.snapBackElasticCoefficient
            is CapsuleStyle -> animationStyle.snapBackElasticCoefficient
            is BubbleStyle -> animationStyle.snapBackElasticCoefficient
            is LineStyle -> animationStyle.snapBackElasticCoefficient
            else -> SnapBackDefaults.ElasticCoefficient
        }
        val fling = when (animationStyle) {
            is WaveStyle -> animationStyle.snapBackFlingDecay
            is CapsuleStyle -> animationStyle.snapBackFlingDecay
            is BubbleStyle -> animationStyle.snapBackFlingDecay
            is LineStyle -> animationStyle.snapBackFlingDecay
            else -> SnapBackDefaults.FlingDecay
        }
        SnapBackParams(type, stiffness, damping, duration, elastic, fling)
    }
    val snapProgress = remember { Animatable(0f) }
    var snapCompleted by remember { mutableStateOf(false) }
    val curSnapParams by rememberUpdatedState(snapParams)
    LaunchedEffect(isActive) {
        if (isActive) {
            snapCompleted = false
            snapProgress.snapTo(0f)
        } else if (!lastFingerX.isNaN()) {
            runSnapBack(snapProgress, curSnapParams) {
                snapCompleted = true
            }
        }
    }

    if (!isActive && snapCompleted) return

    val progress = if (isActive) 1f else snapProgress.value
    val isFling = curSnapParams.type == SnapBackType.FLING
    val easedProgress = if (isFling) 1f - progress else (1f - progress).coerceAtLeast(0f)
    val position = SideGestureState.button?.position ?: Position.Left
    val displayFingerX = if (isActive) rawFingerX else {
        if (position != Position.Bottom) lastFingerX * easedProgress else lastFingerX
    }
    val displayFingerY = if (isActive) rawFingerY else {
        if (position == Position.Bottom) lastFingerY * easedProgress else lastFingerY
    }
    val displayOriginX = if (isActive) rawOriginX else lastOriginX
    val displayOriginY = if (isActive) rawOriginY else lastOriginY

    when (animationStyle) {
        is WaveStyle -> WaveGestureAnimation(
            modifier = modifier,
            animationStyle = animationStyle,
            sideGestureState = SideGestureState,
            displayOriginX = displayOriginX,
            displayOriginY = displayOriginY,
            displayFingerX = displayFingerX,
            displayFingerY = displayFingerY,
        )
        is CapsuleStyle -> CapsuleGestureAnimation(
            modifier = modifier,
            animationStyle = animationStyle,
            sideGestureState = SideGestureState,
            displayOriginX = displayOriginX,
            displayOriginY = displayOriginY,
            displayFingerX = displayFingerX,
            displayFingerY = displayFingerY,
        )
        is BubbleStyle -> BubbleGestureAnimation(
            modifier = modifier,
            animationStyle = animationStyle,
            sideGestureState = SideGestureState,
            displayOriginX = displayOriginX,
            displayOriginY = displayOriginY,
            displayFingerX = displayFingerX,
            displayFingerY = displayFingerY,
        )
        is LineStyle -> LineGestureAnimation(
            modifier = modifier,
            animationStyle = animationStyle,
            sideGestureState = SideGestureState,
            displayOriginX = displayOriginX,
            displayOriginY = displayOriginY,
            displayFingerX = displayFingerX,
            displayFingerY = displayFingerY,
        )
    }
}
