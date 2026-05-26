package hunoia.luno.settings.selector
import hunoia.luno.settings.SettingsProvider

import hunoia.luno.gesture.GestureButton
import hunoia.luno.settings.model.ActionSettings
import hunoia.luno.settings.model.AdvancedSettings
import hunoia.luno.settings.model.GestureSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class GestureRuntimeSettings(
    val sideButtons: List<GestureButton>,
    val bottomButtons: List<GestureButton>,
    val advancedSettings: AdvancedSettings,
    val gestureSettings: GestureSettings,
    val actionSettings: ActionSettings,
)

object GestureRuntimeSettingsProvider {

    val flow: Flow<GestureRuntimeSettings> = combine(
        SettingsProvider.sideGestureButtons,
        SettingsProvider.bottomGestureButtons,
        SettingsProvider.advancedSettings,
        SettingsProvider.gestureSettings,
        SettingsProvider.actionSettings,
    ) { side, bottom, advanced, gesture, action ->
        GestureRuntimeSettings(
            sideButtons = side,
            bottomButtons = bottom,
            advancedSettings = advanced,
            gestureSettings = gesture,
            actionSettings = action,
        )
    }
}
