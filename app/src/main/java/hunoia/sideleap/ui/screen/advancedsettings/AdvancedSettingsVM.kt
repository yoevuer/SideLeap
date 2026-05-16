package hunoia.sideleap.ui.screen.advancedsettings

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.R
import hunoia.sideleap.ui.screen.advancedsettings.AdvancedSettingsVM.UiEvent
import hunoia.sideleap.ui.screen.advancedsettings.AdvancedSettingsVM.UiState
import hunoia.sideleap.settings.api.SettingsProvider
import hunoia.sideleap.settings.model.DayNightMode
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/23
 */
class AdvancedSettingsVM : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState()

    init {
        loadData()
    }

    fun onShowAnimation(showAnimation: Boolean) {
        updateUiState {
            it.copy(showAnimation = showAnimation)
        }
        saveSettings()
    }

    fun showDayNightModeDropdownMenu(show: Boolean) {
        updateUiState {
            it.copy(showDayNightModeDropdownMenu = show)
        }
    }

    fun onFitSoftKeyboardChange(value: Boolean) {
        updateUiState {
            it.copy(fitSoftKeyboard = value)
        }
        saveSettings()
    }

    fun onActionPanelAppLongPressLaunchPopupChanged(value: Boolean) {
        updateUiState {
            it.copy(actionPanelAppLongPressLaunchPopup = value)
        }
        saveSettings()
    }

    fun onQuickLauncherAppLongPressLaunchPopupChanged(value: Boolean) {
        updateUiState {
            it.copy(quickLauncherAppLongPressLaunchPopup = value)
        }
        saveSettings()
    }

    fun onExcludeFromRecentsChange(value: Boolean) {
        updateUiState {
            it.copy(excludeFromRecents = value)
        }
        saveSettings()
    }

    fun onHideLandscapeChange(value: Boolean) {
        updateUiState {
            it.copy(hideLandscape = value)
        }
        saveSettings()
    }

    fun onHideScreenLockChange(value: Boolean) {
        updateUiState {
            it.copy(hideScreenLock = value)
        }
        saveSettings()
    }

    fun onHideHomeScreenChange(value: Boolean) {
        updateUiState {
            it.copy(hideHomeScreen = value)
        }
        saveSettings()
    }

    fun onHideTemporaryChange(value: Boolean) {
        updateUiState {
            it.copy(hideTemporary = value)
        }
        saveSettings()
    }

    fun onDynamicColorChange(value: Boolean) {
        updateUiState {
            it.copy(dynamicColor = value)
        }
        saveSettings()
    }

    fun onDayNightModeChange(dayNightMode: DayNightMode) {
        updateUiState {
            it.copy(dayNightMode = dayNightMode)
        }
        saveSettings()
    }

    fun onShowSystemAppsChange(value: Boolean) {
        updateUiState { it.copy(showSystemApps = value) }
        saveQuickAppSettings()
    }

    fun clearQuickAppLauncherStats() {
        viewModelScope.launch {
            SettingsProvider.updateQuickAppLauncherSettings {
                it.copy(recentLaunchTime = emptyMap(), launchCount = emptyMap())
            }
        }
    }

    fun clearQuickAppLauncherStatsConfirmed() {
        clearQuickAppLauncherStats()
        toast(R.string.clear_quick_app_stats_done)
    }

    private fun saveSettings() {
        viewModelScope.launch {
            SettingsProvider.updateAdvancedSettings {
                val uiState = uiState
                it.copy(
                    animationStyles = it.animationStyles.copy(isAnimationEnabled = uiState.showAnimation),
                    fitSoftKeyboard = uiState.fitSoftKeyboard,
                    actionPanelAppLongPressLaunchPopup = uiState.actionPanelAppLongPressLaunchPopup,
                    quickLauncherAppLongPressLaunchPopup = uiState.quickLauncherAppLongPressLaunchPopup,
                    hideLandscape = uiState.hideLandscape,
                    hideScreenLock = uiState.hideScreenLock,
                    hideHomeScreen = uiState.hideHomeScreen,
                    hideTemporary = uiState.hideTemporary,
                    excludeFromRecents = uiState.excludeFromRecents,
                    dynamicColor = uiState.dynamicColor,
                    dayNightMode = uiState.dayNightMode
                )
            }
        }
    }

    private fun saveQuickAppSettings() {
        viewModelScope.launch {
            SettingsProvider.updateQuickAppLauncherSettings {
                it.copy(showSystemApps = uiState.showSystemApps)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            SettingsProvider
                .advancedSettings
                .take(1)
                .collectLatest { item ->
                    updateUiState {
                        it.copy(
                            showAnimation = item.animationStyles.isAnimationEnabled,
                            fitSoftKeyboard = item.fitSoftKeyboard,
                            actionPanelAppLongPressLaunchPopup = item.actionPanelAppLongPressLaunchPopup,
                            quickLauncherAppLongPressLaunchPopup = item.quickLauncherAppLongPressLaunchPopup,
                            hideLandscape = item.hideLandscape,
                            hideScreenLock = item.hideScreenLock,
                            hideHomeScreen = item.hideHomeScreen,
                            hideTemporary = item.hideTemporary,
                            excludeFromRecents = item.excludeFromRecents,
                            dynamicColor = item.dynamicColor,
                            dayNightMode = item.dayNightMode
                        )
                    }
                }
        }
        viewModelScope.launch {
            SettingsProvider.quickAppLauncherSettings.take(1).collectLatest { item ->
                updateUiState { it.copy(showSystemApps = item.showSystemApps) }
            }
        }
    }

    data class UiState(
        val showAnimation: Boolean = false,
        val fitSoftKeyboard: Boolean = false,
        val actionPanelAppLongPressLaunchPopup: Boolean = false,
        val quickLauncherAppLongPressLaunchPopup: Boolean = false,
        val hideLandscape: Boolean = false,
        val hideScreenLock: Boolean = false,
        val hideHomeScreen: Boolean = false,
        val hideTemporary: Boolean = false,
        val excludeFromRecents: Boolean = false,
        val dynamicColor: Boolean = false,
        val dayNightMode: DayNightMode = DayNightMode.Auto,
        val showSystemApps: Boolean = true,
        val showDynamicColorOption: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
        val showDayNightModeDropdownMenu: Boolean = false
    )

    sealed interface UiEvent
}
