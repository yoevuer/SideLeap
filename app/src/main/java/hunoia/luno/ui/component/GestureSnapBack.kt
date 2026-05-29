package hunoia.luno.ui.component

import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import hunoia.luno.config.model.SnapBackDefaults
import hunoia.luno.config.model.SnapBackType

data class SnapBackParams(
    val type: SnapBackType = SnapBackType.SPRING,
    val springStiffness: Float = SnapBackDefaults.SpringStiffness,
    val springDamping: Float = SnapBackDefaults.SpringDamping,
    val easeOutDurationMs: Int = SnapBackDefaults.EaseOutDurationMs,
    val elasticCoefficient: Float = SnapBackDefaults.ElasticCoefficient,
    val flingDecay: Float = SnapBackDefaults.FlingDecay,
)

suspend fun runSnapBack(
    snapProgress: Animatable<Float, AnimationVector1D>,
    params: SnapBackParams,
    onBeforeAnimate: () -> Unit = {},
    onAfterAnimate: () -> Unit = {},
) {
    onBeforeAnimate()
    snapProgress.snapTo(0f)

    when (params.type) {
        SnapBackType.SPRING -> {
            val stiffness = mapSpringStiffness(params.springStiffness)
            val damping = mapSpringDamping(params.springDamping)
            snapProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = damping, stiffness = stiffness)
            )
            snapProgress.animateTo(targetValue = 1.15f, animationSpec = tween(100))
        }
        SnapBackType.EASE_OUT -> {
            snapProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = params.easeOutDurationMs.coerceIn(100, 1000),
                    easing = FastOutSlowInEasing
                )
            )
        }
        SnapBackType.SNAP -> {}
        SnapBackType.ELASTIC -> {
            snapProgress.animateTo(
                targetValue = 0.85f,
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = Spring.StiffnessMedium
                )
            )
            snapProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        SnapBackType.FLING -> {
            val duration = (300f * params.flingDecay).toInt().coerceIn(100, 1000)
            snapProgress.animateTo(
                targetValue = 2f,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = FastOutLinearInEasing
                )
            )
        }
    }
    onAfterAnimate()
}

private fun mapSpringStiffness(value: Float): Float {
    val min = Spring.StiffnessLow
    val max = Spring.StiffnessHigh
    return min + (max - min) * value.coerceIn(0f, 1f)
}

private fun mapSpringDamping(value: Float): Float {
    val min = 0.2f
    val max = 1f
    return min + (max - min) * value.coerceIn(0f, 1f)
}
