@file:Suppress("ConstPropertyName")

package hunoia.sideleap.settings.defaults

import hunoia.sideleap.settings.model.ActionPanelStyles
import hunoia.sideleap.settings.model.AnimationStyles
import com.blankj.utilcode.util.ConvertUtils
import hunoia.sideleap.system.vibration.Vibrations as VibrationModel

object AdvancedSettingsDefaults {

    val ExcludeApps = emptyList<String>()
    val AnimationStyles = AnimationStyles()
    val ActionPanelStyles = ActionPanelStyles()
    const val FitSoftKeyboard = true
    const val ActionPanelAppLongPressLaunchPopup = false
    const val QuickLauncherAppLongPressLaunchPopup = false
    const val MiniWindowHorizontalBias = 0.5f
    const val MiniWindowVerticalBias = 0.7f
    const val MiniWindowVerticalEdgeMarginFraction = 0.05f
    const val MiniWindowVerticalOffsetFraction = 0f
    const val HideLandscape = false
    const val HideScreenLock = false
    const val HideHomeScreen = false
    const val ExcludeFromRecents = false
    const val DynamicColor = false
    val DayNightMode = hunoia.sideleap.settings.model.DayNightMode.Auto
    val ClipApps = emptyMap<String, Float>()
    val ClipShortcuts = emptyMap<String, Float>()
}

object GestureSettingsDefaults {

    val SlideTriggerDistance = ConvertUtils.dp2px(30f)
    val LongSlideTriggerDistance = ConvertUtils.dp2px(100f)
    const val LongPressTriggerDelayMs = 250L
    const val LongSlideTriggerImmediately = true
    const val LongSlideTriggerDelayMs = 100L
    const val IsCustomVibration = false
    const val SubGestureTimeoutMs = 5000L
    const val IsPreciseSlideType = true
    const val VirtualMouseSensitivityX = 1.6f
    const val VirtualMouseSensitivityY = 1.6f
    const val VirtualMouseAcceleration = 0.8f
    const val VirtualMouseInitialYRatio = 0.35f
    const val VirtualMouseEdgeCancelThresholdDp = 24
    const val VirtualMouseContinuousMode = false
    const val VirtualMouseContinuousModeTimeoutMs = 5000L
    const val VirtualMouseCursorSizeDp = 28
    const val VirtualMouseCursorAlpha = 0.9f
    const val VirtualMouseClickAnimationEnabled = true
    const val VirtualMouseMovementDeadZoneDp = 3
    const val VirtualMouseLongPressEnabled = false
    const val VirtualMouseLongPressDelayMs = 800L
    const val VirtualMouseLongPressMoveToleranceDp = 6
    const val VirtualMouseTrailStrength = 1.2f
    const val VirtualMouseTrailAlpha = 0.7f
    val Vibrations = VibrationModel()
}

object InitialSettingsDefaults {

    const val GestureEnabled = true
    const val Unlocked = false
}

object ActionSettingsDefaults {

    const val MoveScreenRate = 2f
    const val MoveScreenHoverDelayMs = 600L
    const val GotoBottomStrength = 10
    const val PasswordMinLength = 4
    const val PasswordMaxLength = 32
    const val PasswordDefaultLength = 16
    const val PasswordLowercaseEnabled = true
    const val PasswordUppercaseEnabled = true
    const val PasswordDigitsEnabled = true
    const val PasswordSymbolsEnabled = true
    const val HideGestureButtonDelayMs = 1000L
}
