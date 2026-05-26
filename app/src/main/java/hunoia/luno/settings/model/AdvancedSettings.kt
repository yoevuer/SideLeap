package hunoia.luno.settings.model

import androidx.annotation.Keep
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.ActionPanelAppLongPressLaunchPopup
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.ActionPanelStyles
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.AnimationStyles
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.ClipApps
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.ClipShortcuts
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.DayNightMode
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.DynamicColor
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.ExcludeApps
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.ExcludeFromRecents
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.FitSoftKeyboard
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.HideHomeScreen
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.HideLandscape
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.HideScreenLock
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.MiniWindowHorizontalBias
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.MiniWindowVerticalBias
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.MiniWindowVerticalEdgeMarginFraction
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.MiniWindowVerticalOffsetFraction
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.QuickLauncherAppLongPressLaunchPopup
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class AdvancedSettings(
    val excludeApps: List<String> = ExcludeApps,
    val animationStyles: AnimationStyles = AnimationStyles,
    val actionPanelStyles: ActionPanelStyles = ActionPanelStyles,
    val fitSoftKeyboard: Boolean = FitSoftKeyboard,
    val actionPanelAppLongPressLaunchPopup: Boolean = ActionPanelAppLongPressLaunchPopup,
    val quickLauncherAppLongPressLaunchPopup: Boolean = QuickLauncherAppLongPressLaunchPopup,
    val miniWindowHorizontalBias: Float = MiniWindowHorizontalBias,
    val miniWindowVerticalBias: Float = MiniWindowVerticalBias,
    val miniWindowVerticalEdgeMarginFraction: Float = MiniWindowVerticalEdgeMarginFraction,
    val miniWindowVerticalOffsetFraction: Float = MiniWindowVerticalOffsetFraction,
    val hideLandscape: Boolean = HideLandscape,
    val hideScreenLock: Boolean = HideScreenLock,
    val hideHomeScreen: Boolean = HideHomeScreen,
    val excludeFromRecents: Boolean = ExcludeFromRecents,
    val dynamicColor: Boolean = DynamicColor,
    val dayNightMode: DayNightMode = DayNightMode,
    val clipApps: Map<String, Float> = ClipApps,
    val clipShortcuts: Map<String, Float> = ClipShortcuts
)
