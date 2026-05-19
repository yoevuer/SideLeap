package hunoia.sideleap.ui.screen.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.R
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.settings.model.SubGesture
import hunoia.sideleap.settings.model.SubGestureSettings
import hunoia.sideleap.ui.screen.home.HomeVM.UiEvent
import hunoia.sideleap.ui.screen.home.HomeVM.UiState
import hunoia.sideleap.settings.backup.BackupHelper
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.system.permission.isAccessibilitySettingsOn
import hunoia.sideleap.system.permission.isIgnoringBatteryOptimizations
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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
                    name = "子手势 ${settings.subGestures.size + 1}"
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
                it.copy(subGestures = list.toMutableList().apply {
                    set(index, gesture.copy(enabled = enabled))
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
                it.toMutableList().apply {
                    add(GestureButton.createBottom())
                }
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
                it.toMutableList().apply {
                    addAll(GestureButton.createSidePair())
                }
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
                isSideGestureButtonListExpanded = it.isSideGestureButtonListExpanded && !expanded
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
                isBottomGestureButtonListExpanded = it.isBottomGestureButtonListExpanded && !expanded
            )
        }
        if (expanded && scrollOffset != Int.MAX_VALUE) {
            sendUiEvent(UiEvent.ScrollToEvent(scrollOffset))
        }
    }

    fun onAppGestureEnabledChange(enabled: Boolean) {
        if (!uiState.isAccessibilityEnabled) {
            toast(R.string.please_enable_accessibility_service_first)
            return
        }
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
                val list = buttons.toMutableList().apply {
                    set(index, button.copy(enabled = enabled))
                }
                it.copy(bottomGestureButtons = list)
            }
        }
        saveSettings()
    }

    fun onSideGestureButtonEnabledChange(button: GestureButton, enabled: Boolean) {
        updateUiState {
            val buttons = it.sideGestureButtons
            val index = buttons.indexOf(button)
            if (index < 0) it else {
                val list = buttons.toMutableList().apply {
                    set(index, button.copy(enabled = enabled))
                }
                it.copy(sideGestureButtons = list)
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
        }
    }

    data class UiState(
        val sideGestureButtons: List<GestureButton> = emptyList(),
        val bottomGestureButtons: List<GestureButton> = emptyList(),
        val subGestures: List<SubGesture> = emptyList(),
        val isGestureEnabled: Boolean = false,
        val isAccessibilityEnabled: Boolean = false,
        val isIgnoringBatteryOptimizations: Boolean = false,
        val isDrawOverlayEnabled: Boolean = false,
        val isPopBackgroundEnabled: Boolean = false,
        val isSubGestureListExpanded: Boolean = false,
        val isBottomGestureButtonListExpanded: Boolean = false,
        val isSideGestureButtonListExpanded: Boolean = false,
        val showMoreMenu: Boolean = false,
        val showResetWarningDialog: Boolean = false,
        val showBackupRestoreDialog: Boolean = false
    )

    sealed interface UiEvent {

        data object ScrollToBottom : UiEvent
        data class ScrollToEvent(val offsetY: Int) : UiEvent
        data class NavigateToSubGestureEditor(val subGestureId: String) : UiEvent
    }
}
