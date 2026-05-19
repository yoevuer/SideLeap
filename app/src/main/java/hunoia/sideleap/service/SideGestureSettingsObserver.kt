package hunoia.sideleap.service

import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.settings.model.ActionSettings
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.settings.model.InitialSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

class SideGestureSettingsObserver(
    private val scope: CoroutineScope,
    private val onInitialSettings: (InitialSettings) -> Unit,
    private val onAdvancedSettings: (AdvancedSettings) -> Unit,
    private val onGestureSettings: (GestureSettings) -> Unit,
    private val onActionSettings: (ActionSettings) -> Unit,
    private val onGestureButtons: (Collection<GestureButton>) -> Unit,
    private val onRefreshGestureButtons: () -> Unit
) {
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch(Dispatchers.Main.immediate) {
            observeLatestSettings()
            observeGestureButtonChanges()
            observeGestureVisibilityChanges()
            observeKeyboardInputSettingChanges()
        }
    }

    private fun CoroutineScope.observeLatestSettings() {
        launch { SettingsProvider.initialSettings.collectLatest(onInitialSettings) }
        launch { SettingsProvider.advancedSettings.collectLatest(onAdvancedSettings) }
        launch { SettingsProvider.gestureSettings.collectLatest(onGestureSettings) }
        launch { SettingsProvider.actionSettings.collectLatest(onActionSettings) }
    }

    private fun CoroutineScope.observeGestureButtonChanges() {
        launch {
            SettingsProvider
                .sideGestureButtons
                .combine(SettingsProvider.bottomGestureButtons) { side, bottom -> side + bottom }
                .collectLatest(onGestureButtons)
        }
    }

    private fun CoroutineScope.observeGestureVisibilityChanges() {
        launch {
            SettingsProvider
                .initialSettings
                .distinctUntilChangedBy { it.gestureEnabled }
                .collectLatest { onRefreshGestureButtons() }
        }
    }

    private fun CoroutineScope.observeKeyboardInputSettingChanges() {
        launch {
            SettingsProvider
                .advancedSettings
                .distinctUntilChangedBy { it.fitSoftKeyboard }
                .collectLatest { onRefreshGestureButtons() }
        }
    }
}
