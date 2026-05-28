package hunoia.luno.ui.dialog

import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.settings.model.ActionSettings
import hunoia.luno.ui.dialog.ActionSettingsVM.UiEvent
import hunoia.luno.ui.dialog.ActionSettingsVM.UiState
import hunoia.luno.settings.SettingsProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class ActionSettingsVM : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState()

    init {
        loadData()
    }

    fun onPreviousAppOperation(pkgName: String, add: Boolean) {
        updateUiState {
            val pkgNames = it.actionSettings.previousApp.packageNames
            val newPkgNames = if (add) {
                pkgNames + pkgName
            } else {
                pkgNames - pkgName
            }
            it.copy(
                actionSettings = it.actionSettings.copy(
                    previousApp = it.actionSettings.previousApp.copy(packageNames = newPkgNames)
                )
            )
        }
        saveSettings()
    }

    fun onGotoBottomStrengthChange(strength: Float) {
        updateUiState {
            it.copy(
                actionSettings = it.actionSettings.copy(
                    gotoBottom = it.actionSettings.gotoBottom.copy(strength = strength.roundToInt())
                )
            )
        }
    }

    fun onHideGestureButtonDelayChange(delayMs: Float) {
        updateUiState {
            it.copy(
                actionSettings = it.actionSettings.copy(
                    hideGestureButton = it.actionSettings.hideGestureButton.copy(delayMs = delayMs.toLong())
                )
            )
        }
    }

    fun onVolumeScrubHorizontalEnabledChange(value: Boolean) {
        updateUiState {
            it.copy(
                actionSettings = it.actionSettings.copy(
                    volumeScrub = it.actionSettings.volumeScrub.copy(horizontalEnabled = value)
                )
            )
        }
        saveSettings()
    }

    fun onVolumeScrubStepThresholdChange(value: Float) {
        updateUiState {
            it.copy(
                actionSettings = it.actionSettings.copy(
                    volumeScrub = it.actionSettings.volumeScrub.copy(stepThresholdDp = value.roundToInt())
                )
            )
        }
    }

    fun saveSettings() {
        viewModelScope.launchWithLoading {
            SettingsProvider.updateActionSettings {
                uiState.actionSettings
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            SettingsProvider
                .actionSettings
                .take(1)
                .collectLatest { actionSettings ->
                    updateUiState {
                        it.copy(actionSettings = actionSettings)
                    }
                }
        }
    }

    data class UiState(
        val actionSettings: ActionSettings = ActionSettings()
    )

    sealed interface UiEvent
}