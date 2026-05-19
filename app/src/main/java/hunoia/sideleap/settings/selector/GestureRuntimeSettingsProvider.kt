package hunoia.sideleap.settings.selector
import hunoia.sideleap.settings.SettingsProvider

import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.settings.model.ActionSettings
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.settings.model.GestureSettings
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
