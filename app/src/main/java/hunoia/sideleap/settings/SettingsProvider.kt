package hunoia.sideleap.settings

import androidx.datastore.core.DataStore
import hunoia.sideleap.App
import hunoia.sideleap.BuildConfig
import hunoia.sideleap.settings.store.DataStoreFiles
import hunoia.sideleap.settings.model.ActionSettings
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.settings.model.Backup
import hunoia.sideleap.settings.model.FrozenAppSettings
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.settings.model.InitialSettings
import hunoia.sideleap.settings.model.QuickAppLauncherSettings
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.settings.store.dataStore
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
    suspend fun updateQuickAppLauncherLayout(layout: QuickAppLauncherSettings) {
        _quickAppLauncherSettings.updateData { old ->
            old.copy(
                panelHeightFraction = layout.panelHeightFraction,
                contentHeightFraction = layout.contentHeightFraction,
                candidateRows = layout.candidateRows,
                panelWidthFraction = layout.panelWidthFraction,
                panelHorizontalBias = layout.panelHorizontalBias
            )
        }
    }
    suspend fun resetQuickAppLauncherLayout() {
        _quickAppLauncherSettings.updateData { old ->
            old.copy(
                panelHeightFraction = QuickAppLauncherSettings().panelHeightFraction,
                contentHeightFraction = QuickAppLauncherSettings().contentHeightFraction,
                candidateRows = QuickAppLauncherSettings().candidateRows,
                panelWidthFraction = QuickAppLauncherSettings().panelWidthFraction,
                panelHorizontalBias = QuickAppLauncherSettings().panelHorizontalBias
            )
        }
    }
    suspend fun recordQuickAppLaunch(appKey: String) {
        _quickAppLauncherSettings.updateData { old ->
            old.copy(
                recentLaunchTime = old.recentLaunchTime + (appKey to System.currentTimeMillis()),
                launchCount = old.launchCount + (appKey to ((old.launchCount[appKey] ?: 0L) + 1L))
            )
        }
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
        backup.initialSettings?.let { value -> _initialSettings.updateData { value } }
        backup.advancedSettings?.let { value -> _advancedSettings.updateData { value } }
        backup.gestureSettings?.let { value -> _gestureSettings.updateData { value } }
        backup.actionSettings?.let { value -> _actionSettings.updateData { value } }
        backup.gestureButtons?.let { value -> _sideGestureButtons.updateData { value } }
        backup.bottomGestureButtons?.let { value -> _bottomGestureButtons.updateData { value } }
        backup.quickAppLauncherSettings?.let { value -> _quickAppLauncherSettings.updateData { value } }
        backup.frozenAppSettings?.let { value -> _frozenAppSettings.updateData { value } }
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
