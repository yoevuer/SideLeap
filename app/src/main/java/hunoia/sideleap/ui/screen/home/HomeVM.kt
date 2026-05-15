package hunoia.sideleap.ui.screen.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.App
import hunoia.sideleap.R
import hunoia.sideleap.SideGestureService
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.ui.screen.home.HomeVM.UiEvent
import hunoia.sideleap.ui.screen.home.HomeVM.UiState
import hunoia.sideleap.settings.BackupHelper
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
            Dispatchers.IO + CoroutineExceptionHandler { _, ex ->
                ex.printStackTrace()
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
            Dispatchers.IO + CoroutineExceptionHandler { _, ex ->
                ex.printStackTrace()
                toast(R.string.restore_failed)
            },
            cancelable = false
        ) {
            BackupHelper.restore(context, restoreFrom)
            toast(R.string.restore_success)
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

    fun updatePermissionState() {
        viewModelScope.launch {
            val app = App.getContext()
            val isGestureEnabled = SettingsProvider.getInitialSettings().gestureEnabled
            val isAccessibilityEnabled = app.isAccessibilitySettingsOn(SideGestureService::class.java)
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
        }
    }

    data class UiState(
        val sideGestureButtons: List<GestureButton> = emptyList(),
        val bottomGestureButtons: List<GestureButton> = emptyList(),
        val isGestureEnabled: Boolean = false,
        val isAccessibilityEnabled: Boolean = false,
        val isIgnoringBatteryOptimizations: Boolean = false,
        val isDrawOverlayEnabled: Boolean = false,
        val isPopBackgroundEnabled: Boolean = false,
        val isBottomGestureButtonListExpanded: Boolean = false,
        val isSideGestureButtonListExpanded: Boolean = false,
        val showMoreMenu: Boolean = false,
        val showResetWarningDialog: Boolean = false,
        val showBackupRestoreDialog: Boolean = false
    )

    sealed interface UiEvent {

        data object ScrollToBottom : UiEvent
        data class ScrollToEvent(val offsetY: Int) : UiEvent
    }
}
