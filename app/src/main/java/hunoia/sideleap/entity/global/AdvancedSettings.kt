package hunoia.sideleap.entity.global

import androidx.annotation.Keep
import hunoia.sideleap.constant.AdvancedSettingsDefaults.ActionPanelAppLongPressLaunchPopup
import hunoia.sideleap.constant.AdvancedSettingsDefaults.ActionPanelStyles
import hunoia.sideleap.constant.AdvancedSettingsDefaults.AnimationStyles
import hunoia.sideleap.constant.AdvancedSettingsDefaults.ClipApps
import hunoia.sideleap.constant.AdvancedSettingsDefaults.ClipShortcuts
import hunoia.sideleap.constant.AdvancedSettingsDefaults.DayNightMode
import hunoia.sideleap.constant.AdvancedSettingsDefaults.DynamicColor
import hunoia.sideleap.constant.AdvancedSettingsDefaults.ExcludeApps
import hunoia.sideleap.constant.AdvancedSettingsDefaults.ExcludeFromRecents
import hunoia.sideleap.constant.AdvancedSettingsDefaults.FitSoftKeyboard
import hunoia.sideleap.constant.AdvancedSettingsDefaults.HideHomeScreen
import hunoia.sideleap.constant.AdvancedSettingsDefaults.HideLandscape
import hunoia.sideleap.constant.AdvancedSettingsDefaults.HideScreenLock
import hunoia.sideleap.constant.AdvancedSettingsDefaults.HideTemporary
import hunoia.sideleap.constant.AdvancedSettingsDefaults.QuickLauncherAppLongPressLaunchPopup
import hunoia.sideleap.constant.AdvancedSettingsDefaults.VolumeButtonSwitchSong
import hunoia.sideleap.entity.ActionPanelStyles
import hunoia.sideleap.entity.AnimationStyles
import hunoia.sideleap.entity.DayNightMode
import kotlinx.serialization.Serializable

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/25
 */
@Serializable
@Keep
data class AdvancedSettings(
    // packageName
    val excludeApps: List<String> = ExcludeApps,
    val animationStyles: AnimationStyles = AnimationStyles,
    val actionPanelStyles: ActionPanelStyles = ActionPanelStyles,
    val volumeButtonSwitchSong: Boolean = VolumeButtonSwitchSong,
    val fitSoftKeyboard: Boolean = FitSoftKeyboard,
    val actionPanelAppLongPressLaunchPopup: Boolean = ActionPanelAppLongPressLaunchPopup,
    val quickLauncherAppLongPressLaunchPopup: Boolean = QuickLauncherAppLongPressLaunchPopup,
    val hideLandscape: Boolean = HideLandscape,
    // field 8 was hideQuickPanel, do not reuse
    val hideScreenLock: Boolean = HideScreenLock,
    val hideHomeScreen: Boolean = HideHomeScreen,
    val hideTemporary: Boolean = HideTemporary,
    val excludeFromRecents: Boolean = ExcludeFromRecents,
    val dynamicColor: Boolean = DynamicColor,
    val dayNightMode: DayNightMode = DayNightMode,
    // qualifiedName
    val clipApps: Map<String, Float> = ClipApps,
    // qualifiedNameWithIntents
    val clipShortcuts: Map<String, Float> = ClipShortcuts
)
