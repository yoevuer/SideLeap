package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import hunoia.sideleap.settings.AdvancedSettingsDefaults.ActionPanelAppLongPressLaunchPopup
import hunoia.sideleap.settings.AdvancedSettingsDefaults.ActionPanelStyles
import hunoia.sideleap.settings.AdvancedSettingsDefaults.AnimationStyles
import hunoia.sideleap.settings.AdvancedSettingsDefaults.ClipApps
import hunoia.sideleap.settings.AdvancedSettingsDefaults.ClipShortcuts
import hunoia.sideleap.settings.AdvancedSettingsDefaults.DayNightMode
import hunoia.sideleap.settings.AdvancedSettingsDefaults.DynamicColor
import hunoia.sideleap.settings.AdvancedSettingsDefaults.ExcludeApps
import hunoia.sideleap.settings.AdvancedSettingsDefaults.ExcludeFromRecents
import hunoia.sideleap.settings.AdvancedSettingsDefaults.FitSoftKeyboard
import hunoia.sideleap.settings.AdvancedSettingsDefaults.HideHomeScreen
import hunoia.sideleap.settings.AdvancedSettingsDefaults.HideLandscape
import hunoia.sideleap.settings.AdvancedSettingsDefaults.HideScreenLock
import hunoia.sideleap.settings.AdvancedSettingsDefaults.HideTemporary
import hunoia.sideleap.settings.AdvancedSettingsDefaults.QuickLauncherAppLongPressLaunchPopup
import hunoia.sideleap.settings.AdvancedSettingsDefaults.VolumeButtonSwitchSong
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class AdvancedSettings(
    val excludeApps: List<String> = ExcludeApps,
    val animationStyles: AnimationStyles = AnimationStyles,
    val actionPanelStyles: ActionPanelStyles = ActionPanelStyles,
    val volumeButtonSwitchSong: Boolean = VolumeButtonSwitchSong,
    val fitSoftKeyboard: Boolean = FitSoftKeyboard,
    val actionPanelAppLongPressLaunchPopup: Boolean = ActionPanelAppLongPressLaunchPopup,
    val quickLauncherAppLongPressLaunchPopup: Boolean = QuickLauncherAppLongPressLaunchPopup,
    val hideLandscape: Boolean = HideLandscape,
    val hideScreenLock: Boolean = HideScreenLock,
    val hideHomeScreen: Boolean = HideHomeScreen,
    val hideTemporary: Boolean = HideTemporary,
    val excludeFromRecents: Boolean = ExcludeFromRecents,
    val dynamicColor: Boolean = DynamicColor,
    val dayNightMode: DayNightMode = DayNightMode,
    val clipApps: Map<String, Float> = ClipApps,
    val clipShortcuts: Map<String, Float> = ClipShortcuts
)
