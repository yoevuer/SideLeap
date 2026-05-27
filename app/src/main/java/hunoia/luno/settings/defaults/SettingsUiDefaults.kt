@file:Suppress("ConstPropertyName")

package hunoia.luno.settings.defaults

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import hunoia.luno.core.DensityProvider
import hunoia.luno.R
import hunoia.luno.system.vibration.VibrationEffects

object SettingsUiDefaults {

    val MinGestureButtonWidth = DensityProvider.dp2px(1f)
    val MaxGestureButtonWidth = DensityProvider.dp2px(60f)
    val MinSlideTriggerDistance = DensityProvider.dp2px(24f)
    val MaxSlideTriggerDistance = DensityProvider.dp2px(40f)
    val MinLongSlideTriggerDistance = DensityProvider.dp2px(80f)
    val MaxLongSlideTriggerDistance = DensityProvider.dp2px(100f)
    const val MinBezierStrokeWidth = 0
    val MaxBezierStrokeWidth = DensityProvider.dp2px(MaxBezierStrokeWidthValue.toFloat())
    val MinBezierWidth = DensityProvider.dp2px(10f)
    val MaxBezierWidth = DensityProvider.dp2px(80f)
    const val MinBezierLength = 1.0f
    const val MaxBezierLength = 6.0f
    const val MinIconScale = 0.0f
    const val MaxIconScale = 1.0f
    const val MinMoveScreenRate = 1f
    const val MaxMoveScreenRate = 3f
    const val MinMoveScreenHover = 300f
    const val MaxMoveScreenHover = 1000f
    const val MinGotoBottomStrength = 1f
    const val MaxGotoBottomStrength = 20f
    const val MinGestureButtonPosition = 0f
    const val MaxGestureButtonPosition = 1f
    const val MinGestureButtonLength = 0.1f
    const val MinLongSlideTriggerDelayMs = 0L
    const val MaxLongSlideTriggerDelayMs = 250L
    const val MinLongPressTriggerDelayMs = 100L
    const val MaxLongPressTriggerDelayMs = 1000L
    const val MinSubGestureTimeoutMs = 1000L
    const val MaxSubGestureTimeoutMs = 15000L
    val MinSubGestureTriggerDistance = DensityProvider.dp2px(16f)
    val MaxSubGestureTriggerDistance = DensityProvider.dp2px(60f)
    const val GestureButtonColorAlpha = 0.36f
    const val DisabledAlpha = 0.36f
    const val DimAlpha = 0.5f
    const val MaxBezierStrokeWidthValue = 8

    val MinCapsuleThickness = DensityProvider.dp2px(20f)
    val MaxCapsuleThickness = DensityProvider.dp2px(56f)
    val MinCapsuleLength = DensityProvider.dp2px(40f)
    val MaxCapsuleLength = DensityProvider.dp2px(120f)
    val MinCapsuleCornerRadius = DensityProvider.dp2px(8f)
    val MaxCapsuleCornerRadius = DensityProvider.dp2px(32f)

    val MinBubbleDiameter = DensityProvider.dp2px(28f)
    val MaxBubbleDiameter = DensityProvider.dp2px(72f)
    val MinBubbleOffset = DensityProvider.dp2px(20f)
    val MaxBubbleOffset = DensityProvider.dp2px(120f)

    val MinLineWidth = DensityProvider.dp2px(2f)
    val MaxLineWidth = DensityProvider.dp2px(16f)
    val MinLineLength = DensityProvider.dp2px(20f)
    val MaxLineLength = DensityProvider.dp2px(120f)
    val MinLineOffset = DensityProvider.dp2px(10f)
    val MaxLineOffset = DensityProvider.dp2px(100f)
    val MinLineCornerRadius = 0
    val MaxLineCornerRadius = DensityProvider.dp2px(32f)

    val MinGridColumns = 2
    val MaxGridColumns = 8
    val MinGridRows = 1
    val MaxGridRows = 6
    val MinGridCornerRadius = DensityProvider.dp2px(4f)
    val MaxGridCornerRadius = DensityProvider.dp2px(40f)

    const val MinArcLength = 30
    const val MaxArcLength = 270
    const val MinSpacing = 0.8f
    const val MaxSpacing = 2.0f
    val MinItemSize = DensityProvider.dp2px(24f)
    val MaxItemSize = DensityProvider.dp2px(72f)

    @Composable
    fun getPredefinedVibrationEffectText(effect: VibrationEffects): String {
        return when (effect) {
            VibrationEffects.None -> stringResource(id = R.string.custom)
            VibrationEffects.Tick -> stringResource(id = R.string.vibration_tick)
            VibrationEffects.Click -> stringResource(id = R.string.vibration_click)
            VibrationEffects.HeavyClick -> stringResource(id = R.string.vibration_heavy_click)
        }
    }

}
