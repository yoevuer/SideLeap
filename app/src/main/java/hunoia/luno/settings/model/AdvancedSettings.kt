package hunoia.luno.settings.model

import androidx.annotation.Keep
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.ActionPanelStyles
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.AnimationStyles
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.ClipApps
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.ClipShortcuts

import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.ExcludeApps
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.MiniWindowHorizontalBias
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.MiniWindowVerticalBias
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.MiniWindowVerticalEdgeMarginFraction
import hunoia.luno.settings.defaults.AdvancedSettingsDefaults.MiniWindowVerticalOffsetFraction
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class AdvancedSettings(
    val excludeApps: List<String> = ExcludeApps,
    val animationStyles: AnimationStyles = AnimationStyles,
    val actionPanelStyles: ActionPanelStyles = ActionPanelStyles,
    val miniWindowHorizontalBias: Float = MiniWindowHorizontalBias,
    val miniWindowVerticalBias: Float = MiniWindowVerticalBias,
    val miniWindowVerticalEdgeMarginFraction: Float = MiniWindowVerticalEdgeMarginFraction,
    val miniWindowVerticalOffsetFraction: Float = MiniWindowVerticalOffsetFraction,
    val clipApps: Map<String, Float> = ClipApps,
    val clipShortcuts: Map<String, Float> = ClipShortcuts
)
