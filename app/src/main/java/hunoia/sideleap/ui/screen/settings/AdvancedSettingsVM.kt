package hunoia.sideleap.ui.screen.settings

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.R
import hunoia.sideleap.ui.screen.settings.AdvancedSettingsVM.UiEvent
import hunoia.sideleap.ui.screen.settings.AdvancedSettingsVM.UiState
import hunoia.sideleap.settings.SettingsProvider
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
            SettingsProvider.updateAdvancedSettings {
                val uiState = uiState
                it.copy(
                    fitSoftKeyboard = uiState.fitSoftKeyboard,
                    actionPanelAppLongPressLaunchPopup = uiState.actionPanelAppLongPressLaunchPopup,
                    quickLauncherAppLongPressLaunchPopup = uiState.quickLauncherAppLongPressLaunchPopup,
                    miniWindowHorizontalBias = uiState.miniWindowHorizontalBias,
                    miniWindowVerticalBias = uiState.miniWindowVerticalBias,
                    miniWindowVerticalEdgeMarginFraction = uiState.miniWindowVerticalEdgeMarginFraction,
                    miniWindowVerticalOffsetFraction = uiState.miniWindowVerticalOffsetFraction,
                    hideLandscape = uiState.hideLandscape,
                    hideScreenLock = uiState.hideScreenLock,
                    hideHomeScreen = uiState.hideHomeScreen,
                    excludeFromRecents = uiState.excludeFromRecents,
                )
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
                            fitSoftKeyboard = item.fitSoftKeyboard,
                            actionPanelAppLongPressLaunchPopup = item.actionPanelAppLongPressLaunchPopup,
                            quickLauncherAppLongPressLaunchPopup = item.quickLauncherAppLongPressLaunchPopup,
                            miniWindowHorizontalBias = item.miniWindowHorizontalBias,
                            miniWindowVerticalBias = item.miniWindowVerticalBias,
                            miniWindowVerticalEdgeMarginFraction = item.miniWindowVerticalEdgeMarginFraction,
                            miniWindowVerticalOffsetFraction = item.miniWindowVerticalOffsetFraction,
                            hideLandscape = item.hideLandscape,
                            hideScreenLock = item.hideScreenLock,
                            hideHomeScreen = item.hideHomeScreen,
                            excludeFromRecents = item.excludeFromRecents,
                        )
                    }
                }
        }
    }

    data class UiState(
        val fitSoftKeyboard: Boolean = false,
        val actionPanelAppLongPressLaunchPopup: Boolean = false,
        val quickLauncherAppLongPressLaunchPopup: Boolean = false,
        val miniWindowHorizontalBias: Float = 0.5f,
        val miniWindowVerticalBias: Float = 0.7f,
        val miniWindowVerticalEdgeMarginFraction: Float = 0.05f,
        val miniWindowVerticalOffsetFraction: Float = 0f,
        val hideLandscape: Boolean = false,
        val hideScreenLock: Boolean = false,
        val hideHomeScreen: Boolean = false,
        val excludeFromRecents: Boolean = false,
    )

    sealed interface UiEvent
}
