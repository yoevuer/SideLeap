@file:Suppress("ConstPropertyName")

package hunoia.sideleap.settings

import hunoia.sideleap.entity.ActionPanelStyles
import hunoia.sideleap.entity.AnimationStyles
import com.blankj.utilcode.util.ConvertUtils

object AdvancedSettingsDefaults {

    val ExcludeApps = emptyList<String>()
    val AnimationStyles = AnimationStyles()
    val ActionPanelStyles = ActionPanelStyles()
    const val VolumeButtonSwitchSong = false
    const val FitSoftKeyboard = true
    const val ActionPanelAppLongPressLaunchPopup = false
    const val QuickLauncherAppLongPressLaunchPopup = false
    const val HideLandscape = false
    const val HideScreenLock = false
    const val HideHomeScreen = false
    const val HideTemporary = false
    const val ExcludeFromRecents = false
    const val DynamicColor = false
    val DayNightMode = hunoia.sideleap.entity.DayNightMode.Auto
    val ClipApps = emptyMap<String, Float>()
    val ClipShortcuts = emptyMap<String, Float>()
}

object GestureSettingsDefaults {

    val Angles = hunoia.sideleap.gesture.GestureAngles()
    val SlideTriggerDistance = ConvertUtils.dp2px(30f)
    val LongSlideTriggerDistance = ConvertUtils.dp2px(100f)
    const val LongPressTriggerDelayMs = 250L
    const val LongSlideTriggerImmediately = true
    const val LongSlideTriggerDelayMs = 100L
    const val IsCustomVibration = false
    const val IsPreciseSlideType = true
    val Vibrations = hunoia.sideleap.entity.Vibrations()
}

object InitialSettingsDefaults {

    const val GestureEnabled = true
    const val Unlocked = false
}

object ActionSettingsDefaults {

    const val MoveScreenRate = 2f
    const val MoveScreenHoverDelayMs = 600L
    const val GotoBottomStrength = 10
}
