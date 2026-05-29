package hunoia.luno.config.selector
import hunoia.luno.config.ConfigProvider

import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.ActionSettings
import hunoia.luno.config.model.AdvancedSettings
import hunoia.luno.config.model.GestureSettings
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
        ConfigProvider.sideGestureButtons,
        ConfigProvider.bottomGestureButtons,
        ConfigProvider.advancedSettings,
        ConfigProvider.gestureSettings,
        ConfigProvider.actionSettings,
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
