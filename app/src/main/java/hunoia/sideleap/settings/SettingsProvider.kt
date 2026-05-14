package hunoia.sideleap.settings

import androidx.datastore.core.DataStore
import hunoia.sideleap.App
import hunoia.sideleap.BuildConfig
import hunoia.sideleap.constant.DataStoreFiles
import hunoia.sideleap.entity.QuickAppLauncherSettings
import hunoia.sideleap.entity.global.ActionSettings
import hunoia.sideleap.entity.global.AdvancedSettings
import hunoia.sideleap.entity.global.Backup
import hunoia.sideleap.entity.global.FrozenAppSettings
import hunoia.sideleap.entity.global.GestureSettings
import hunoia.sideleap.entity.global.InitialSettings
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.ktx.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

object SettingsProvider {

    private val _initialSettings: DataStore<InitialSettings> by lazy {
        App.getContext().dataStore(DataStoreFiles.INITIAL_SETTINGS, InitialSettings())
    }
    private val _advancedSettings: DataStore<AdvancedSettings> by lazy {
        App.getContext().dataStore(DataStoreFiles.ADVANCED_SETTINGS, AdvancedSettings())
    }
    private val _gestureSettings: DataStore<GestureSettings> by lazy {
        App.getContext().dataStore(DataStoreFiles.GESTURE_SETTINGS, GestureSettings())
    }
    private val _actionSettings: DataStore<ActionSettings> by lazy {
        App.getContext().dataStore(DataStoreFiles.ACTION_SETTINGS, ActionSettings())
    }
    private val _sideGestureButtons: DataStore<List<GestureButton>> by lazy {
        App.getContext().dataStore(DataStoreFiles.SIDE_GESTURE_BUTTONS, GestureButton.SideDefaults)
    }
    private val _bottomGestureButtons: DataStore<List<GestureButton>> by lazy {
        App.getContext().dataStore(DataStoreFiles.BOTTOM_GESTURE_BUTTONS, GestureButton.BottomDefaults)
    }
    private val _quickAppLauncherSettings: DataStore<QuickAppLauncherSettings> by lazy {
        App.getContext().dataStore(DataStoreFiles.QUICK_APP_LAUNCHER, QuickAppLauncherSettings())
    }
    private val _frozenAppSettings: DataStore<FrozenAppSettings> by lazy {
        App.getContext().dataStore(DataStoreFiles.FROZEN_APP_SETTINGS, FrozenAppSettings())
    }

    val initialSettings: Flow<InitialSettings> = _initialSettings.data
    val advancedSettings: Flow<AdvancedSettings> = _advancedSettings.data
    val gestureSettings: Flow<GestureSettings> = _gestureSettings.data
    val actionSettings: Flow<ActionSettings> = _actionSettings.data
    val sideGestureButtons: Flow<List<GestureButton>> = _sideGestureButtons.data
    val bottomGestureButtons: Flow<List<GestureButton>> = _bottomGestureButtons.data
    val quickAppLauncherSettings: Flow<QuickAppLauncherSettings> = _quickAppLauncherSettings.data
    val frozenAppSettings: Flow<FrozenAppSettings> = _frozenAppSettings.data

    suspend fun getInitialSettings(): InitialSettings = _initialSettings.data.first()
    suspend fun getAdvancedSettings(): AdvancedSettings = _advancedSettings.data.first()
    suspend fun getGestureSettings(): GestureSettings = _gestureSettings.data.first()
    suspend fun getActionSettings(): ActionSettings = _actionSettings.data.first()
    suspend fun getSideGestureButtons(): List<GestureButton> = _sideGestureButtons.data.first()
    suspend fun getBottomGestureButtons(): List<GestureButton> = _bottomGestureButtons.data.first()
    suspend fun getQuickAppLauncherSettings(): QuickAppLauncherSettings = _quickAppLauncherSettings.data.first()
    suspend fun getFrozenAppSettings(): FrozenAppSettings = _frozenAppSettings.data.first()

    suspend fun updateInitialSettings(transform: suspend (InitialSettings) -> InitialSettings) {
        _initialSettings.updateData(transform)
    }
    suspend fun updateAdvancedSettings(transform: suspend (AdvancedSettings) -> AdvancedSettings) {
        _advancedSettings.updateData(transform)
    }
    suspend fun updateGestureSettings(transform: suspend (GestureSettings) -> GestureSettings) {
        _gestureSettings.updateData(transform)
    }
    suspend fun updateActionSettings(transform: suspend (ActionSettings) -> ActionSettings) {
        _actionSettings.updateData(transform)
    }
    suspend fun updateSideGestureButtons(transform: suspend (List<GestureButton>) -> List<GestureButton>) {
        _sideGestureButtons.updateData(transform)
    }
    suspend fun updateBottomGestureButtons(transform: suspend (List<GestureButton>) -> List<GestureButton>) {
        _bottomGestureButtons.updateData(transform)
    }
    suspend fun updateQuickAppLauncherSettings(transform: suspend (QuickAppLauncherSettings) -> QuickAppLauncherSettings) {
        _quickAppLauncherSettings.updateData(transform)
    }
    suspend fun updateFrozenAppSettings(transform: suspend (FrozenAppSettings) -> FrozenAppSettings) {
        _frozenAppSettings.updateData(transform)
    }

    suspend fun snapshotAll(): Backup = Backup(
        initialSettings = getInitialSettings(),
        advancedSettings = getAdvancedSettings(),
        gestureSettings = getGestureSettings(),
        actionSettings = getActionSettings(),
        gestureButtons = getSideGestureButtons(),
        bottomGestureButtons = getBottomGestureButtons(),
        quickAppLauncherSettings = getQuickAppLauncherSettings(),
        frozenAppSettings = getFrozenAppSettings(),
        timestamp = System.currentTimeMillis(),
        version = BuildConfig.VERSION_NAME
    )

    suspend fun restoreAll(backup: Backup) {
        backup.initialSettings?.let { _initialSettings.updateData { it } }
        backup.advancedSettings?.let { _advancedSettings.updateData { it } }
        backup.gestureSettings?.let { _gestureSettings.updateData { it } }
        backup.actionSettings?.let { _actionSettings.updateData { it } }
        backup.gestureButtons?.let { _sideGestureButtons.updateData { it } }
        backup.bottomGestureButtons?.let { _bottomGestureButtons.updateData { it } }
        backup.quickAppLauncherSettings?.let { _quickAppLauncherSettings.updateData { it } }
        backup.frozenAppSettings?.let { _frozenAppSettings.updateData { it } }
    }

    suspend fun resetAll() {
        _initialSettings.updateData { InitialSettings() }
        _advancedSettings.updateData { AdvancedSettings() }
        _gestureSettings.updateData { GestureSettings() }
        _actionSettings.updateData { ActionSettings() }
        _sideGestureButtons.updateData { GestureButton.SideDefaults }
        _bottomGestureButtons.updateData { GestureButton.BottomDefaults }
        _quickAppLauncherSettings.updateData { QuickAppLauncherSettings() }
        _frozenAppSettings.updateData { FrozenAppSettings() }
    }
}
