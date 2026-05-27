package hunoia.luno.ui.screen.settings

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.R
import hunoia.luno.ui.screen.settings.AdvancedSettingsVM.UiEvent
import hunoia.luno.ui.screen.settings.AdvancedSettingsVM.UiState
import hunoia.luno.settings.SettingsProvider
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

    fun onPreciseSlideTypeChange(value: Boolean) {
        updateUiState {
            it.copy(isPreciseSlideTypeEnabled = value)
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

    fun saveSettings() {
        viewModelScope.launch {
            val s = uiState
            launch {
                SettingsProvider.updateAdvancedSettings {
                    it.copy(
                        fitSoftKeyboard = s.fitSoftKeyboard,
                        actionPanelAppLongPressLaunchPopup = s.actionPanelAppLongPressLaunchPopup,
                        quickLauncherAppLongPressLaunchPopup = s.quickLauncherAppLongPressLaunchPopup,
                        hideLandscape = s.hideLandscape,
                        hideScreenLock = s.hideScreenLock,
                        hideHomeScreen = s.hideHomeScreen,
                        excludeFromRecents = s.excludeFromRecents,
                    )
                }
            }
            launch {
                SettingsProvider.updateGestureSettings {
                    it.copy(isPreciseSlideType = s.isPreciseSlideTypeEnabled)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                SettingsProvider
                    .advancedSettings
                    .take(1)
                    .collectLatest { item ->
                        updateUiState {
                            it.copy(
                                fitSoftKeyboard = item.fitSoftKeyboard,
                                actionPanelAppLongPressLaunchPopup = item.actionPanelAppLongPressLaunchPopup,
                                quickLauncherAppLongPressLaunchPopup = item.quickLauncherAppLongPressLaunchPopup,
                                hideLandscape = item.hideLandscape,
                                hideScreenLock = item.hideScreenLock,
                                hideHomeScreen = item.hideHomeScreen,
                                excludeFromRecents = item.excludeFromRecents,
                            )
                        }
                    }
            }
            launch {
                SettingsProvider
                    .gestureSettings
                    .take(1)
                    .collectLatest { item ->
                        updateUiState {
                            it.copy(isPreciseSlideTypeEnabled = item.isPreciseSlideType)
                        }
                    }
            }
        }
    }

    data class UiState(
        val fitSoftKeyboard: Boolean = false,
        val actionPanelAppLongPressLaunchPopup: Boolean = false,
        val quickLauncherAppLongPressLaunchPopup: Boolean = false,
        val hideLandscape: Boolean = false,
        val hideScreenLock: Boolean = false,
        val hideHomeScreen: Boolean = false,
        val excludeFromRecents: Boolean = false,
        val isPreciseSlideTypeEnabled: Boolean = false,
    )

    sealed interface UiEvent
}
