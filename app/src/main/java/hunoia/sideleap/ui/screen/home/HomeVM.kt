package hunoia.sideleap.ui.screen.home

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import com.blankj.utilcode.util.ColorUtils
import hunoia.sideleap.R
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.settings.model.SubGesture
import hunoia.sideleap.settings.model.SubGestureSettings
import hunoia.sideleap.ui.screen.home.HomeVM.UiEvent
import hunoia.sideleap.ui.screen.home.HomeVM.UiState
import hunoia.sideleap.settings.backup.BackupHelper
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.settings.model.GestureSettings.VirtualMouseTrailStyle
import hunoia.sideleap.settings.model.DayNightMode
import hunoia.sideleap.system.permission.isAccessibilitySettingsOn
import hunoia.sideleap.system.permission.isIgnoringBatteryOptimizations
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/22
 */
class HomeVM : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState()

    init {
        loadData()
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
                    name = "子手势 ${settings.subGestures.size + 1}",
                    color = ColorUtils.getRandomColor(false)
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

    fun showBackupRestoreDialog(show: Boolean) {
        updateUiState {
            it.copy(showBackupRestoreDialog = show)
        }
    }

    fun showMoreMenu(show: Boolean, delayBlock: (() -> Unit)? = null) {
        viewModelScope.launch {
            updateUiState {
                it.copy(showMoreMenu = show)
            }
            if (delayBlock != null) {
                delay(100)
                delayBlock()
            }
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
        fun cleanIfSubGesture(action: hunoia.sideleap.action.Action?): hunoia.sideleap.action.Action? {
            if (action == null) return null
            if (action.value == hunoia.sideleap.action.GlobalActions.SUB_GESTURE) {
                val data = try {
                    kotlinx.serialization.json.Json.decodeFromString<hunoia.sideleap.action.payload.SubGestureActionData>(action.data)
                } catch (_: Exception) { null }
                if (data?.id == deletedId) return null
            }
            val cleanedLongPress = cleanIfSubGesture(action.longPressAction)
            return if (cleanedLongPress != action.longPressAction) {
                action.copy(longPressAction = cleanedLongPress)
            } else {
                action
            }
        }
        fun cleanActions(buttons: List<hunoia.sideleap.gesture.GestureButton>): List<hunoia.sideleap.gesture.GestureButton> {
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
            val cleanedSubGestures = settings.subGestures.map { gesture ->
                gesture.copy(
                    upAction = cleanIfSubGesture(gesture.upAction),
                    downAction = cleanIfSubGesture(gesture.downAction),
                    leftAction = cleanIfSubGesture(gesture.leftAction),
                    rightAction = cleanIfSubGesture(gesture.rightAction),
                    upRightAction = cleanIfSubGesture(gesture.upRightAction),
                    downRightAction = cleanIfSubGesture(gesture.downRightAction),
                    downLeftAction = cleanIfSubGesture(gesture.downLeftAction),
                    upLeftAction = cleanIfSubGesture(gesture.upLeftAction),
                )
            }
            settings.copy(subGestures = cleanedSubGestures)
        }
    }

    fun onVirtualMouseChange(value: GestureSettings.VirtualMouse) {
        updateUiState { it.copy(virtualMouse = value) }
    }

    fun saveVirtualMouseSettings() {
        viewModelScope.launch {
            SettingsProvider.updateGestureSettings {
                it.copy(virtualMouse = uiState.virtualMouse)
            }
        }
    }

    fun onVirtualMouseContinuousModeChange(value: Boolean) {
        onVirtualMouseChange(uiState.virtualMouse.copy(continuousMode = value))
        saveVirtualMouseSettings()
    }

    fun onVirtualMouseContinuousModeTimeoutChange(value: Long) {
        onVirtualMouseChange(uiState.virtualMouse.copy(continuousModeTimeoutMs = value))
        saveVirtualMouseSettings()
    }

    fun onVirtualMouseClickAnimationChange(value: Boolean) {
        onVirtualMouseChange(uiState.virtualMouse.copy(clickAnimationEnabled = value))
        saveVirtualMouseSettings()
    }

    fun onVirtualMouseTrailStyleChange(value: VirtualMouseTrailStyle) {
        onVirtualMouseChange(uiState.virtualMouse.copy(trailStyle = value))
        saveVirtualMouseSettings()
    }

    fun onVirtualMouseLongPressEnabledChange(value: Boolean) {
        onVirtualMouseChange(uiState.virtualMouse.copy(longPressEnabled = value))
        saveVirtualMouseSettings()
    }

    fun onVirtualMouseLongPressDelayChange(value: Long) {
        onVirtualMouseChange(uiState.virtualMouse.copy(longPressDelayMs = value))
        saveVirtualMouseSettings()
    }

    fun onShowAnimation(showAnimation: Boolean) {
        updateUiState { it.copy(showAnimation = showAnimation) }
        saveDisplaySettings()
    }

    fun showDayNightModeDropdownMenu(show: Boolean) {
        updateUiState { it.copy(showDayNightModeDropdownMenu = show) }
    }

    fun onDynamicColorChange(value: Boolean) {
        updateUiState { it.copy(dynamicColor = value) }
        saveDisplaySettings()
    }

    fun onDayNightModeChange(dayNightMode: DayNightMode) {
        updateUiState { it.copy(dayNightMode = dayNightMode) }
        saveDisplaySettings()
    }

    fun onMiniWindowHorizontalBiasChange(value: Float) {
        updateUiState { it.copy(miniWindowHorizontalBias = value.coerceIn(0f, 1f)) }
    }

    fun onMiniWindowVerticalBiasChange(value: Float) {
        updateUiState { it.copy(miniWindowVerticalBias = value.coerceIn(0f, 1f)) }
    }

    fun onMiniWindowVerticalEdgeMarginChange(value: Float) {
        updateUiState { it.copy(miniWindowVerticalEdgeMarginFraction = value.coerceIn(0f, 0.2f)) }
    }

    fun onMiniWindowVerticalOffsetChange(value: Float) {
        updateUiState { it.copy(miniWindowVerticalOffsetFraction = value.coerceIn(-0.3f, 0.3f)) }
    }

    fun saveDisplaySettings() {
        viewModelScope.launch {
            SettingsProvider.updateAdvancedSettings {
                it.copy(
                    animationStyles = it.animationStyles.copy(isAnimationEnabled = uiState.showAnimation),
                    dynamicColor = uiState.dynamicColor,
                    dayNightMode = uiState.dayNightMode,
                    miniWindowHorizontalBias = uiState.miniWindowHorizontalBias,
                    miniWindowVerticalBias = uiState.miniWindowVerticalBias,
                    miniWindowVerticalEdgeMarginFraction = uiState.miniWindowVerticalEdgeMarginFraction,
                    miniWindowVerticalOffsetFraction = uiState.miniWindowVerticalOffsetFraction,
                )
            }
        }
    }

    fun updatePermissionState() {
        viewModelScope.launch {
            val app = hunoia.sideleap.core.AppContext.get()
            val isGestureEnabled = SettingsProvider.getInitialSettings().gestureEnabled
            val clazz = Class.forName("hunoia.sideleap.SideGestureService") as Class<out android.accessibilityservice.AccessibilityService?>
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
            launch {
                SettingsProvider.initialSettings.collectLatest { initialSettings ->
                    updateUiState {
                        it.copy(isGestureEnabled = initialSettings.gestureEnabled)
                    }
                }
            }
            launch {
                SettingsProvider.sideGestureButtons.collectLatest { buttons ->
                    updateUiState {
                        it.copy(sideGestureButtons = buttons.sortedBy { b -> b.id })
                    }
                }
            }
            launch {
                SettingsProvider.bottomGestureButtons.collectLatest { buttons ->
                    updateUiState {
                        it.copy(bottomGestureButtons = buttons.sortedBy { b -> b.id })
                    }
                }
            }
            launch {
                SettingsProvider.subGestureSettings.collectLatest { settings ->
                    updateUiState {
                        it.copy(subGestures = settings.subGestures)
                    }
                }
            }
            launch {
                SettingsProvider.gestureSettings.collectLatest { settings ->
                    updateUiState {
                        it.copy(virtualMouse = settings.virtualMouse)
                    }
                }
            }
            launch {
                SettingsProvider.advancedSettings.collectLatest { item ->
                    updateUiState {
                        it.copy(
                            showAnimation = item.animationStyles.isAnimationEnabled,
                            dynamicColor = item.dynamicColor,
                            dayNightMode = item.dayNightMode,
                            miniWindowHorizontalBias = item.miniWindowHorizontalBias,
                            miniWindowVerticalBias = item.miniWindowVerticalBias,
                            miniWindowVerticalEdgeMarginFraction = item.miniWindowVerticalEdgeMarginFraction,
                            miniWindowVerticalOffsetFraction = item.miniWindowVerticalOffsetFraction,
                        )
                    }
                }
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
        val showMoreMenu: Boolean = false,
        val showResetWarningDialog: Boolean = false,
        val showBackupRestoreDialog: Boolean = false,
        val virtualMouse: GestureSettings.VirtualMouse = GestureSettings.VirtualMouse(),
        val showAnimation: Boolean = false,
        val dynamicColor: Boolean = false,
        val dayNightMode: DayNightMode = DayNightMode.Auto,
        val showDynamicColorOption: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
        val showDayNightModeDropdownMenu: Boolean = false,
        val miniWindowHorizontalBias: Float = 0.5f,
        val miniWindowVerticalBias: Float = 0.7f,
        val miniWindowVerticalEdgeMarginFraction: Float = 0.05f,
        val miniWindowVerticalOffsetFraction: Float = 0f,
    )

    sealed interface UiEvent {

        data object ScrollToBottom : UiEvent
        data class ScrollToEvent(val offsetY: Int) : UiEvent
    }
}
