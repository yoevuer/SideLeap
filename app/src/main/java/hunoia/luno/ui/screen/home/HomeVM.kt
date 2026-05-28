package hunoia.luno.ui.screen.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM

import hunoia.luno.R
import hunoia.luno.core.AppContext
import hunoia.luno.gesture.GestureButton
import hunoia.luno.settings.model.SubGesture
import hunoia.luno.settings.model.SubGestureSettings
import hunoia.luno.ui.screen.home.HomeVM.UiEvent
import hunoia.luno.ui.screen.home.HomeVM.UiState
import hunoia.luno.settings.backup.BackupHelper
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.settings.model.AdvancedSettings
import hunoia.luno.settings.model.FrozenAppSettings
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.freeze.FreezeFacade
import hunoia.luno.settings.model.InitialSettings
import hunoia.luno.settings.model.GestureSettings.PointerTrailStyle
import hunoia.luno.system.permission.isAccessibilitySettingsOn
import hunoia.luno.system.permission.isIgnoringBatteryOptimizations
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/22
 */
class HomeVM : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState()

    init {
        loadData()
        loadFrozenCount()
    }

    fun backup(context: Context, saveTo: Uri) {
        viewModelScope.launchWithLoading(
            Dispatchers.IO + CoroutineExceptionHandler { _, _ ->
                toast(R.string.backup_failed)
            },
            cancelable = false
        ) {
            BackupHelper.backup(context, saveTo)
            toast(R.string.backup_success)
        }
    }

    fun restore(context: Context, restoreFrom: Uri) {
        viewModelScope.launchWithLoading(
            Dispatchers.IO + CoroutineExceptionHandler { _, _ ->
                toast(R.string.restore_failed)
            },
            cancelable = false
        ) {
            BackupHelper.restore(context, restoreFrom)
            toast(R.string.restore_success)
        }
    }

    fun addSubGesture(id: String) {
        viewModelScope.launch {
            SettingsProvider.updateSubGestureSettings { settings ->
                val newGesture = SubGesture(
                    id = id,
                    name = AppContext.get().getString(R.string.sub_gesture_default_name, settings.subGestures.size + 1),
                    color = android.graphics.Color.argb(255, kotlin.random.Random.nextInt(256), kotlin.random.Random.nextInt(256), kotlin.random.Random.nextInt(256))
                )
                settings.copy(subGestures = settings.subGestures + newGesture)
            }
        }
    }

    fun onSubGestureEnabledChange(gesture: SubGesture, enabled: Boolean) {
        updateUiState {
            val list = it.subGestures
            val index = list.indexOf(gesture)
            if (index < 0) it else {
                it.copy(subGestures = list.mapIndexed { i, g ->
                    if (i == index) g.copy(enabled = enabled) else g
                })
            }
        }
        saveSettings()
    }

    fun expandSubGestureList(expanded: Boolean, scrollOffset: Int = Int.MAX_VALUE) {
        updateUiState {
            it.copy(
                isSubGestureListExpanded = expanded,
                isBottomGestureButtonListExpanded = it.isBottomGestureButtonListExpanded && !expanded,
                isSideGestureButtonListExpanded = it.isSideGestureButtonListExpanded && !expanded
            )
        }
        if (expanded && scrollOffset != Int.MAX_VALUE) {
            sendUiEvent(UiEvent.ScrollToEvent(scrollOffset))
        }
    }

    fun addBottomGestureButton() {
        if (uiState.bottomGestureButtons.size >= 10) {
            toast(R.string.gesture_button_size_max)
            return
        }
        viewModelScope.launch {
            SettingsProvider.updateBottomGestureButtons {
                it + GestureButton.createBottom()
            }
            delay(50)
            sendUiEvent(UiEvent.ScrollToBottom)
        }
    }

    fun addSideGestureButton() {
        if (uiState.sideGestureButtons.size >= 20) {
            toast(R.string.gesture_button_size_max)
            return
        }
        viewModelScope.launch {
            SettingsProvider.updateSideGestureButtons {
                it + GestureButton.createSidePair()
            }
            delay(50)
            sendUiEvent(UiEvent.ScrollToBottom)
        }
    }

    fun showResetWarningDialog(show: Boolean) {
        updateUiState {
            it.copy(showResetWarningDialog = show)
        }
    }

    fun expandBottomGestureButtonList(expanded: Boolean, scrollOffset: Int = Int.MAX_VALUE) {
        updateUiState {
            it.copy(
                isBottomGestureButtonListExpanded = expanded,
                isSideGestureButtonListExpanded = it.isSideGestureButtonListExpanded && !expanded,
                isSubGestureListExpanded = it.isSubGestureListExpanded && !expanded
            )
        }
        if (expanded && scrollOffset != Int.MAX_VALUE) {
            sendUiEvent(UiEvent.ScrollToEvent(scrollOffset))
        }
    }

    fun expandSideGestureButtonList(expanded: Boolean, scrollOffset: Int = Int.MAX_VALUE) {
        updateUiState {
            it.copy(
                isSideGestureButtonListExpanded = expanded,
                isBottomGestureButtonListExpanded = it.isBottomGestureButtonListExpanded && !expanded,
                isSubGestureListExpanded = it.isSubGestureListExpanded && !expanded
            )
        }
        if (expanded && scrollOffset != Int.MAX_VALUE) {
            sendUiEvent(UiEvent.ScrollToEvent(scrollOffset))
        }
    }

    fun onAppGestureEnabledChange(enabled: Boolean) {
        updateUiState {
            it.copy(isGestureEnabled = enabled)
        }
        saveSettings()
    }

    fun onBottomGestureButtonEnabledChange(button: GestureButton, enabled: Boolean) {
        updateUiState {
            val buttons = it.bottomGestureButtons
            val index = buttons.indexOf(button)
            if (index < 0) it else {
                it.copy(bottomGestureButtons = buttons.mapIndexed { i, b ->
                    if (i == index) b.copy(enabled = enabled) else b
                })
            }
        }
        saveSettings()
    }

    fun onSideGestureButtonEnabledChange(button: GestureButton, enabled: Boolean) {
        updateUiState {
            val buttons = it.sideGestureButtons
            val index = buttons.indexOf(button)
            if (index < 0) it else {
                it.copy(sideGestureButtons = buttons.mapIndexed { i, b ->
                    if (i == index) b.copy(enabled = enabled) else b
                })
            }
        }
        saveSettings()
    }

    fun deleteSubGesture(gesture: SubGesture) {
        viewModelScope.launch {
            SettingsProvider.updateSubGestureSettings { settings ->
                val remainingIds = settings.subGestures
                    .filter { it.id != gesture.id }
                    .map { it.id }
                    .toSet()
                val cleanedGestureList = settings.subGestures
                    .filter { it.id != gesture.id }
                    .map { cleanedGestures ->
                        cleanedGestures
                    }
                settings.copy(subGestures = cleanedGestureList)
            }
            cleanSubGestureReferences(gesture.id)
            delay(50)
        }
    }

    private suspend fun cleanSubGestureReferences(deletedId: String) {
        val sideButtons = SettingsProvider.getSideGestureButtons()
        val bottomButtons = SettingsProvider.getBottomGestureButtons()
        val subSettings = SettingsProvider.getSubGestureSettings()
        fun cleanIfSubGesture(action: hunoia.luno.action.Action?): hunoia.luno.action.Action? {
            if (action == null) return null
            if (action.value == hunoia.luno.action.GlobalActions.SUB_GESTURE) return null
            val cleanedLongPress = cleanIfSubGesture(action.longPressAction)
            return if (cleanedLongPress != action.longPressAction) {
                action.copy(longPressAction = cleanedLongPress)
            } else {
                action
            }
        }
        fun cleanActions(buttons: List<hunoia.luno.gesture.GestureButton>): List<hunoia.luno.gesture.GestureButton> {
            return buttons.map { button ->
                button.copy(
                    slideActions = button.slideActions.copy(
                        center = button.slideActions.center.mapNotNull { cleanIfSubGesture(it) },
                        up = button.slideActions.up.mapNotNull { cleanIfSubGesture(it) },
                        down = button.slideActions.down.mapNotNull { cleanIfSubGesture(it) },
                        center2 = button.slideActions.center2.mapNotNull { cleanIfSubGesture(it) },
                        up2 = button.slideActions.up2.mapNotNull { cleanIfSubGesture(it) },
                        down2 = button.slideActions.down2.mapNotNull { cleanIfSubGesture(it) },
                    ),
                    longSlideActions = button.longSlideActions.copy(
                        center = button.longSlideActions.center.mapNotNull { cleanIfSubGesture(it) },
                        up = button.longSlideActions.up.mapNotNull { cleanIfSubGesture(it) },
                        down = button.longSlideActions.down.mapNotNull { cleanIfSubGesture(it) },
                        center2 = button.longSlideActions.center2.mapNotNull { cleanIfSubGesture(it) },
                        up2 = button.longSlideActions.up2.mapNotNull { cleanIfSubGesture(it) },
                        down2 = button.longSlideActions.down2.mapNotNull { cleanIfSubGesture(it) },
                    ),
                    tapActions = button.tapActions.copy(
                        center = button.tapActions.center.mapNotNull { cleanIfSubGesture(it) },
                        up = button.tapActions.up.mapNotNull { cleanIfSubGesture(it) },
                        down = button.tapActions.down.mapNotNull { cleanIfSubGesture(it) },
                        center2 = button.tapActions.center2.mapNotNull { cleanIfSubGesture(it) },
                        up2 = button.tapActions.up2.mapNotNull { cleanIfSubGesture(it) },
                        down2 = button.tapActions.down2.mapNotNull { cleanIfSubGesture(it) },
                    )
                )
            }
        }
        SettingsProvider.updateSideGestureButtons { cleanActions(it) }
        SettingsProvider.updateBottomGestureButtons { cleanActions(it) }
        SettingsProvider.updateSubGestureSettings { settings ->
            fun clean(id: String?) = if (id == deletedId || id == hunoia.luno.action.GlobalActions.SUB_GESTURE) null else id
            val cleanedSubGestures = settings.subGestures.map { gesture ->
                gesture.copy(
                    upActionId = clean(gesture.upActionId),
                    downActionId = clean(gesture.downActionId),
                    leftActionId = clean(gesture.leftActionId),
                    rightActionId = clean(gesture.rightActionId),
                    upRightActionId = clean(gesture.upRightActionId),
                    downRightActionId = clean(gesture.downRightActionId),
                    downLeftActionId = clean(gesture.downLeftActionId),
                    upLeftActionId = clean(gesture.upLeftActionId),
                )
            }
            settings.copy(subGestures = cleanedSubGestures)
        }
    }

    fun onPointerChange(value: GestureSettings.Pointer) {
        updateUiState { it.copy(pointer = value) }
    }

    fun savePointerSettings() {
        viewModelScope.launch {
            SettingsProvider.updateGestureSettings {
                it.copy(pointer = uiState.pointer)
            }
        }
    }

    fun onPointerContinuousModeChange(value: Boolean) {
        onPointerChange(uiState.pointer.copy(continuousMode = value))
        savePointerSettings()
    }

    fun onPointerContinuousModeTimeoutChange(value: Long) {
        onPointerChange(uiState.pointer.copy(continuousModeTimeoutMs = value))
    }

    fun onPointerClickAnimationChange(value: Boolean) {
        onPointerChange(uiState.pointer.copy(clickAnimationEnabled = value))
        savePointerSettings()
    }

    fun onPointerTrailStyleChange(value: PointerTrailStyle) {
        onPointerChange(uiState.pointer.copy(trailStyle = value))
        savePointerSettings()
    }

    fun onPointerLongPressEnabledChange(value: Boolean) {
        onPointerChange(uiState.pointer.copy(longPressEnabled = value))
        savePointerSettings()
    }

    fun onPointerLongPressDelayChange(value: Long) {
        onPointerChange(uiState.pointer.copy(longPressDelayMs = value))
        savePointerSettings()
    }

    fun onShowAnimation(showAnimation: Boolean) {
        updateUiState { it.copy(showAnimation = showAnimation) }
        saveDisplaySettings()
    }

    fun onMiniWindowHorizontalBiasChange(value: Float) {
        updateUiState { it.copy(miniWindowHorizontalBias = value.coerceIn(-1f, 1f)) }
    }

    fun onMiniWindowVerticalBiasChange(value: Float) {
        updateUiState { it.copy(miniWindowVerticalBias = value.coerceIn(-1f, 1f)) }
    }

    fun onMiniWindowVerticalOffsetChange(value: Float) {
        updateUiState { it.copy(miniWindowVerticalOffsetFraction = value.coerceIn(-0.3f, 0.3f)) }
    }

    fun onMiniWindowWidthFractionChange(value: Float) {
        updateUiState { it.copy(miniWindowWidthFraction = value.coerceIn(0.2f, 1.5f)) }
    }

    fun onMiniWindowHeightFractionChange(value: Float) {
        updateUiState { it.copy(miniWindowHeightFraction = value.coerceIn(0.2f, 1.5f)) }
    }

    fun onMiniWindowOverrideBoundsChange(value: Boolean) {
        updateUiState { it.copy(miniWindowOverrideBounds = value) }
    }

    fun oneKeyFreeze() {
        viewModelScope.launch {
            FreezeFacade.oneKeyFreeze(AppContext.get())
            val count = FreezeFacade.queryFrozenAppsOnIo(AppContext.get()).size
            updateUiState { it.copy(frozenAppCount = count) }
        }
    }

    fun oneKeyUnfreeze() {
        viewModelScope.launch {
            val frozenApps = FreezeFacade.queryFrozenAppsOnIo(AppContext.get())
            val targets = frozenApps.map { it.packageName }
            FreezeFacade.oneKeyUnfreeze(AppContext.get(), targets)
            val count = FreezeFacade.queryFrozenAppsOnIo(AppContext.get()).size
            updateUiState { it.copy(frozenAppCount = count) }
        }
    }

    private fun loadFrozenCount() {
        viewModelScope.launch {
            val frozenApps = FreezeFacade.queryFrozenAppsOnIo(AppContext.get())
            updateUiState { it.copy(frozenAppCount = frozenApps.size) }
        }
    }

    fun saveDisplaySettings() {
        viewModelScope.launch {
            SettingsProvider.updateAdvancedSettings {
                it.copy(
                    animationStyles = it.animationStyles.copy(isAnimationEnabled = uiState.showAnimation),
                    miniWindowHorizontalBias = uiState.miniWindowHorizontalBias,
                    miniWindowVerticalBias = uiState.miniWindowVerticalBias,
                    miniWindowVerticalOffsetFraction = uiState.miniWindowVerticalOffsetFraction,
                    miniWindowWidthFraction = uiState.miniWindowWidthFraction,
                    miniWindowHeightFraction = uiState.miniWindowHeightFraction,
                    miniWindowOverrideBounds = uiState.miniWindowOverrideBounds,
                )
            }
        }
    }

    fun updatePermissionState() {
        viewModelScope.launch {
            val app = hunoia.luno.core.AppContext.get()
            val isGestureEnabled = SettingsProvider.getInitialSettings().gestureEnabled
            val clazz = Class.forName("hunoia.luno.SideGestureService") as Class<out android.accessibilityservice.AccessibilityService?>
            val isAccessibilityEnabled = app.isAccessibilitySettingsOn(clazz)
            val isIgnoringBatteryOptimizations = app.isIgnoringBatteryOptimizations()
            updateUiState {
                it.copy(
                    isGestureEnabled = isAccessibilityEnabled && isGestureEnabled,
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations
                )
            }
        }
    }

    fun reset() {
        viewModelScope.launch {
            SettingsProvider.resetAll()
        }
    }

    private fun saveSettings() {
        viewModelScope.launch {
            launch {
                SettingsProvider.updateInitialSettings {
                    it.copy(gestureEnabled = uiState.isGestureEnabled)
                }
            }
            launch {
                SettingsProvider.updateSideGestureButtons {
                    uiState.sideGestureButtons
                }
            }
            launch {
                SettingsProvider.updateBottomGestureButtons {
                    uiState.bottomGestureButtons
                }
            }
            launch {
                SettingsProvider.updateSubGestureSettings {
                    SubGestureSettings(subGestures = uiState.subGestures)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                SettingsProvider.initialSettings,
                SettingsProvider.sideGestureButtons,
                SettingsProvider.bottomGestureButtons,
                SettingsProvider.subGestureSettings,
                SettingsProvider.gestureSettings,
                SettingsProvider.advancedSettings,
                SettingsProvider.frozenAppSettings,
            ) { values ->
                val initial = values[0] as InitialSettings
                val sideButtons = values[1] as List<GestureButton>
                val bottomButtons = values[2] as List<GestureButton>
                val subGestureSettings = values[3] as SubGestureSettings
                val gestureSettings = values[4] as GestureSettings
                val advancedSettings = values[5] as AdvancedSettings
                val frozenAppSettings = values[6] as FrozenAppSettings
                uiState.copy(
                    isGestureEnabled = initial.gestureEnabled,
                    sideGestureButtons = sideButtons.sortedBy { it.id },
                    bottomGestureButtons = bottomButtons.sortedBy { it.id },
                    subGestures = subGestureSettings.subGestures,
                    pointer = gestureSettings.pointer,
                    showAnimation = advancedSettings.animationStyles.isAnimationEnabled,
                    miniWindowHorizontalBias = advancedSettings.miniWindowHorizontalBias,
                    miniWindowVerticalBias = advancedSettings.miniWindowVerticalBias,
                    miniWindowVerticalOffsetFraction = advancedSettings.miniWindowVerticalOffsetFraction,
                    miniWindowWidthFraction = advancedSettings.miniWindowWidthFraction,
                    miniWindowHeightFraction = advancedSettings.miniWindowHeightFraction,
                    miniWindowOverrideBounds = advancedSettings.miniWindowOverrideBounds,
                    excludedAppCount = advancedSettings.excludeApps.size,
                    selectedFrozenAppCount = frozenAppSettings.oneKeyPackageNames.size,
                )
            }.collectLatest { state ->
                updateUiState { state }
            }
        }
    }

    data class UiState(
        val sideGestureButtons: List<GestureButton> = emptyList(),
        val bottomGestureButtons: List<GestureButton> = emptyList(),
        val subGestures: List<SubGesture> = emptyList(),
        val isGestureEnabled: Boolean = false,
        val isAccessibilityEnabled: Boolean = false,
        val isIgnoringBatteryOptimizations: Boolean = false,
        val isSubGestureListExpanded: Boolean = false,
        val isBottomGestureButtonListExpanded: Boolean = false,
        val isSideGestureButtonListExpanded: Boolean = false,
        val showResetWarningDialog: Boolean = false,
        val pointer: GestureSettings.Pointer = GestureSettings.Pointer(),
        val showAnimation: Boolean = false,
        val miniWindowHorizontalBias: Float = 0f,
        val miniWindowVerticalBias: Float = 0f,
        val miniWindowVerticalOffsetFraction: Float = 0f,
        val miniWindowWidthFraction: Float = 0.46f,
        val miniWindowHeightFraction: Float = 0.74f,
        val miniWindowOverrideBounds: Boolean = false,
        val excludedAppCount: Int = 0,
        val frozenAppCount: Int = 0,
        val selectedFrozenAppCount: Int = 0,
    )

    sealed interface UiEvent {

        data object ScrollToBottom : UiEvent
        data class ScrollToEvent(val offsetY: Int) : UiEvent
    }
}
