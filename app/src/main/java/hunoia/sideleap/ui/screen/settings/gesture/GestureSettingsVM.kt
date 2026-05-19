package hunoia.sideleap.ui.screen.settings.gesture

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.ui.screen.settings.gesture.GestureSettingsVM.UiEvent
import hunoia.sideleap.ui.screen.settings.gesture.GestureSettingsVM.UiState
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.settings.model.GestureSettings.VirtualMouseTrailStyle
import hunoia.sideleap.system.vibration.VibrationEffects
import hunoia.sideleap.system.vibration.Vibrations
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/23
 */
class GestureSettingsVM : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState()

    init {
        loadData()
    }

    fun saveSettings() {
        viewModelScope.launch {
            val uiState = uiState
            launch {
                SettingsProvider.updateGestureSettings {
                    it.copy(
                        slideTriggerDistance = uiState.slideTriggerDistance.toInt(),
                        longPressTriggerDelayMs = uiState.longPressTriggerDelayMs,
                        longSlideTriggerImmediately = uiState.longSlideTriggerImmediately,
                        longSlideTriggerDistance = uiState.longSlideTriggerDistance.toInt(),
                        longSlideTriggerDelayMs = uiState.longSlideTriggerDelayMs,
                        isCustomVibration = uiState.isCustomVibration,
                        vibrations = uiState.vibrations,
                        isPreciseSlideType = uiState.isPreciseSlideTypeEnabled,
                        virtualMouse = uiState.virtualMouse
                    )
                }
            }
        }
    }

    fun updatePredefinedVibration(effect: VibrationEffects) {
        updateUiState {
            it.copy(vibrations = it.vibrations.copy(predefinedEffect = effect))
        }
        saveSettings()
    }

    fun showPredefinedVibrationDropdown(show: Boolean) {
        updateUiState {
            it.copy(showPredefinedVibrationDropdown = show)
        }
    }

    fun onPreciseSlideTypeChange(value: Boolean) {
        updateUiState {
            it.copy(isPreciseSlideTypeEnabled = value)
        }
        saveSettings()
    }

    fun onCustomVibrationMsChange(value: Float) {
        updateUiState {
            it.copy(vibrations = it.vibrations.copy(customVibrationMs = value.toLong()))
        }
    }

    fun onSlideTriggerDistanceChange(value: Float) {
        updateUiState {
            it.copy(slideTriggerDistance = value)
        }
    }

    fun onLongPressTriggerDelayMsChange(value: Float) {
        updateUiState {
            it.copy(longPressTriggerDelayMs = value.toLong())
        }
    }

    fun onLongSlideTriggerDistanceChange(value: Float) {
        updateUiState {
            it.copy(longSlideTriggerDistance = value)
        }
    }

    fun onLongSlideTriggerDelayMsChange(value: Float) {
        updateUiState {
            it.copy(longSlideTriggerDelayMs = value.toLong())
        }
    }

    fun onLongSlideTriggerImmediatelyChange(value: Boolean) {
        updateUiState {
            it.copy(longSlideTriggerImmediately = value)
        }
        saveSettings()
    }

    fun onVibrateForSlide(value: Boolean) {
        updateUiState {
            it.copy(vibrations = it.vibrations.copy(slideEnabled = value))
        }
        saveSettings()
    }

    fun onVibrateForLongSlide(value: Boolean) {
        updateUiState {
            it.copy(vibrations = it.vibrations.copy(longSlideEnabled = value))
        }
        saveSettings()
    }

    fun onVibrateImmediatelyChange(value: Boolean) {
        updateUiState {
            it.copy(vibrations = it.vibrations.copy(vibrateImmediately = value))
        }
        saveSettings()
    }

    fun onCustomVibrationChange(value: Boolean) {
        updateUiState {
            it.copy(isCustomVibration = value)
        }
        saveSettings()
        sendUiEvent(UiEvent.ScrollToBottom)
    }

    fun onVirtualMouseChange(value: GestureSettings.VirtualMouse) {
        updateUiState { it.copy(virtualMouse = value) }
    }

    fun onVirtualMouseContinuousModeChange(value: Boolean) {
        onVirtualMouseChange(uiState.virtualMouse.copy(continuousMode = value))
        saveSettings()
    }

    fun onVirtualMouseContinuousModeTimeoutChange(value: Long) {
        onVirtualMouseChange(uiState.virtualMouse.copy(continuousModeTimeoutMs = value))
        saveSettings()
    }

    fun onVirtualMouseClickAnimationChange(value: Boolean) {
        onVirtualMouseChange(uiState.virtualMouse.copy(clickAnimationEnabled = value))
        saveSettings()
    }

    fun onVirtualMouseTrailStyleChange(value: VirtualMouseTrailStyle) {
        onVirtualMouseChange(uiState.virtualMouse.copy(trailStyle = value))
        saveSettings()
    }

    fun onVirtualMouseLongPressEnabledChange(value: Boolean) {
        onVirtualMouseChange(uiState.virtualMouse.copy(longPressEnabled = value))
        saveSettings()
    }

    fun onVirtualMouseLongPressDelayChange(value: Long) {
        onVirtualMouseChange(uiState.virtualMouse.copy(longPressDelayMs = value))
        saveSettings()
    }

    private fun loadData() {
        viewModelScope.launch {
            SettingsProvider
                .gestureSettings
                .collectLatest { item ->
                    updateUiState {
                        it.copy(
                            slideTriggerDistance = item.slideTriggerDistance.toFloat(),
                            longPressTriggerDelayMs = item.longPressTriggerDelayMs,
                            longSlideTriggerImmediately = item.longSlideTriggerImmediately,
                            longSlideTriggerDistance = item.longSlideTriggerDistance.toFloat(),
                            longSlideTriggerDelayMs = item.longSlideTriggerDelayMs,
                            isCustomVibration = item.isCustomVibration,
                            vibrations = item.vibrations,
                            isPreciseSlideTypeEnabled = item.isPreciseSlideType,
                            virtualMouse = item.virtualMouse
                        )
                    }
                }
        }
    }

    data class UiState(
        val slideTriggerDistance: Float = 0f,
        val longPressTriggerDelayMs: Long = 0L,
        val longSlideTriggerImmediately: Boolean = true,
        val longSlideTriggerDistance: Float = 0f,
        val longSlideTriggerDelayMs: Long = 0L,
        val canShowPredefinedVibration: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
        val isCustomVibration: Boolean = false,
        val vibrations: Vibrations = Vibrations(),
        val showPredefinedVibrationDropdown: Boolean = false,
        val isPreciseSlideTypeEnabled: Boolean = false,
        val virtualMouse: GestureSettings.VirtualMouse = GestureSettings.VirtualMouse(),
    )

    sealed interface UiEvent {

        data object ScrollToBottom : UiEvent
    }
}
