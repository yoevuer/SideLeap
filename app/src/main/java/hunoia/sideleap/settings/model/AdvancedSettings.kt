package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.ActionPanelAppLongPressLaunchPopup
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.ActionPanelStyles
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.AnimationStyles
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.ClipApps
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.ClipShortcuts
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.DayNightMode
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.DynamicColor
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.ExcludeApps
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.ExcludeFromRecents
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.FitSoftKeyboard
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.HideHomeScreen
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.HideLandscape
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.HideScreenLock
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.MiniWindowHorizontalBias
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.MiniWindowVerticalBias
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.MiniWindowVerticalEdgeMarginFraction
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.MiniWindowVerticalOffsetFraction
import hunoia.sideleap.settings.defaults.AdvancedSettingsDefaults.QuickLauncherAppLongPressLaunchPopup
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
