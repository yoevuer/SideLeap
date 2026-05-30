package hunoia.luno.config

import androidx.datastore.core.DataStore
import hunoia.luno.BuildConfig
import hunoia.luno.config.store.DataStoreFiles
import hunoia.luno.config.model.ActionSettings
import hunoia.luno.config.model.AdvancedSettings
import hunoia.luno.config.model.Backup
import hunoia.luno.config.model.FrozenAppSettings
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.config.model.SubGestureSettings
import hunoia.luno.config.model.InitialSettings
import hunoia.luno.config.model.QuickAppLauncherSettings
import hunoia.luno.core.AppContext
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.store.dataStore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ConfigProvider {

    private val _initialSettings: DataStore<InitialSettings> by lazy {
        AppContext.get().dataStore(DataStoreFiles.INITIAL_SETTINGS, InitialSettings())
    }
    private val _advancedSettings: DataStore<AdvancedSettings> by lazy {
        AppContext.get().dataStore(DataStoreFiles.ADVANCED_SETTINGS, AdvancedSettings())
    }
    private val _gestureSettings: DataStore<GestureSettings> by lazy {
        AppContext.get().dataStore(DataStoreFiles.GESTURE_SETTINGS, GestureSettings())
    }
    private val _actionSettings: DataStore<ActionSettings> by lazy {
        AppContext.get().dataStore(DataStoreFiles.ACTION_SETTINGS, ActionSettings())
    }
    private val _sideGestureButtons: DataStore<List<GestureButton>> by lazy {
        AppContext.get().dataStore(DataStoreFiles.SIDE_GESTURE_BUTTONS, GestureButton.SideDefaults)
    }
    private val _bottomGestureButtons: DataStore<List<GestureButton>> by lazy {
        AppContext.get().dataStore(DataStoreFiles.BOTTOM_GESTURE_BUTTONS, GestureButton.BottomDefaults)
    }
    private val _quickAppLauncherSettings: DataStore<QuickAppLauncherSettings> by lazy {
        AppContext.get().dataStore(DataStoreFiles.QUICK_APP_LAUNCHER, QuickAppLauncherSettings())
    }
    private val _frozenAppSettings: DataStore<FrozenAppSettings> by lazy {
        AppContext.get().dataStore(DataStoreFiles.FROZEN_APP_SETTINGS, FrozenAppSettings())
    }
    private val _subGestureSettings: DataStore<SubGestureSettings> by lazy {
        AppContext.get().dataStore(DataStoreFiles.SUB_GESTURE_SETTINGS, SubGestureSettings())
    }

    val initialSettings: Flow<InitialSettings> = _initialSettings.data
    val advancedSettings: Flow<AdvancedSettings> = _advancedSettings.data
    val gestureSettings: Flow<GestureSettings> = _gestureSettings.data
    val actionSettings: Flow<ActionSettings> = _actionSettings.data
    val sideGestureButtons: Flow<List<GestureButton>> = _sideGestureButtons.data
    val bottomGestureButtons: Flow<List<GestureButton>> = _bottomGestureButtons.data
    val quickAppLauncherSettings: Flow<QuickAppLauncherSettings> = _quickAppLauncherSettings.data
    val frozenAppSettings: Flow<FrozenAppSettings> = _frozenAppSettings.data
    val subGestureSettings: Flow<SubGestureSettings> = _subGestureSettings.data

    suspend fun getInitialSettings(): InitialSettings = _initialSettings.data.first()
    suspend fun getAdvancedSettings(): AdvancedSettings = _advancedSettings.data.first()
    suspend fun getGestureSettings(): GestureSettings = _gestureSettings.data.first()
    suspend fun getActionSettings(): ActionSettings = _actionSettings.data.first()
    suspend fun getSideGestureButtons(): List<GestureButton> = _sideGestureButtons.data.first()
    suspend fun getBottomGestureButtons(): List<GestureButton> = _bottomGestureButtons.data.first()
    suspend fun getQuickAppLauncherSettings(): QuickAppLauncherSettings = _quickAppLauncherSettings.data.first()
    suspend fun getFrozenAppSettings(): FrozenAppSettings = _frozenAppSettings.data.first()
    suspend fun getSubGestureSettings(): SubGestureSettings = _subGestureSettings.data.first()

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
                panelHorizontalBias = layout.panelHorizontalBias,
                gridColumns = layout.gridColumns,
                keyHeightDp = layout.keyHeightDp,
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
                panelHorizontalBias = QuickAppLauncherSettings().panelHorizontalBias,
                gridColumns = QuickAppLauncherSettings().gridColumns,
                keyHeightDp = QuickAppLauncherSettings().keyHeightDp,
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
    suspend fun updateSubGestureSettings(transform: suspend (SubGestureSettings) -> SubGestureSettings) {
        _subGestureSettings.updateData(transform)
    }

    suspend fun snapshotAll(): Backup = coroutineScope {
        val initialDeferred = async { getInitialSettings() }
        val advancedDeferred = async { getAdvancedSettings() }
        val gestureDeferred = async { getGestureSettings() }
        val actionDeferred = async { getActionSettings() }
        val buttonsDeferred = async { getSideGestureButtons() }
        val bottomButtonsDeferred = async { getBottomGestureButtons() }
        val qlaDeferred = async { getQuickAppLauncherSettings() }
        val frozenDeferred = async { getFrozenAppSettings() }
        val subGestureDeferred = async { getSubGestureSettings() }
        Backup(
            initialSettings = initialDeferred.await(),
            advancedSettings = advancedDeferred.await(),
            gestureSettings = gestureDeferred.await(),
            actionSettings = actionDeferred.await(),
            gestureButtons = buttonsDeferred.await(),
            bottomGestureButtons = bottomButtonsDeferred.await(),
            quickAppLauncherSettings = qlaDeferred.await(),
            frozenAppSettings = frozenDeferred.await(),
            subGestureSettings = subGestureDeferred.await(),
            timestamp = System.currentTimeMillis(),
            version = BuildConfig.VERSION_NAME
        )
    }

    suspend fun restoreAll(backup: Backup) = coroutineScope {
        launch { backup.initialSettings?.let { v -> _initialSettings.updateData { v } } }
        launch { backup.advancedSettings?.let { v -> _advancedSettings.updateData { v } } }
        launch { backup.gestureSettings?.let { v -> _gestureSettings.updateData { v } } }
        launch { backup.actionSettings?.let { v -> _actionSettings.updateData { v } } }
        launch { backup.gestureButtons?.let { v -> _sideGestureButtons.updateData { v } } }
        launch { backup.bottomGestureButtons?.let { v -> _bottomGestureButtons.updateData { v } } }
        launch { backup.quickAppLauncherSettings?.let { v -> _quickAppLauncherSettings.updateData { v } } }
        launch { backup.frozenAppSettings?.let { v -> _frozenAppSettings.updateData { v } } }
        launch { backup.subGestureSettings?.let { v -> _subGestureSettings.updateData { v } } }
    }

    suspend fun resetAll() = coroutineScope {
        launch { _initialSettings.updateData { InitialSettings() } }
        launch { _advancedSettings.updateData { AdvancedSettings() } }
        launch { _gestureSettings.updateData { GestureSettings() } }
        launch { _actionSettings.updateData { ActionSettings() } }
        launch { _sideGestureButtons.updateData { GestureButton.SideDefaults } }
        launch { _bottomGestureButtons.updateData { GestureButton.BottomDefaults } }
        launch { _quickAppLauncherSettings.updateData { QuickAppLauncherSettings() } }
        launch { _frozenAppSettings.updateData { FrozenAppSettings() } }
        launch { _subGestureSettings.updateData { SubGestureSettings() } }
    }
}
