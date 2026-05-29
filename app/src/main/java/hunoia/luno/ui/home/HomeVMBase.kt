package hunoia.luno.ui.home

import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.R
import hunoia.luno.core.AppContext
import hunoia.luno.config.ConfigProvider
import hunoia.luno.config.model.AdvancedSettings
import hunoia.luno.config.model.FrozenAppSettings
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.config.model.GestureSettings.PointerTrailStyle
import hunoia.luno.config.model.InitialSettings
import hunoia.luno.freeze.FreezeUseCase
import hunoia.luno.bridge.isAccessibilitySettingsOn
import hunoia.luno.bridge.isIgnoringBatteryOptimizations
import hunoia.luno.bridge.feedback.showToast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

abstract class HomeVMBase : BaseComposeVM<UiState, UiEvent>() {

    fun onPointerChange(value: GestureSettings.Pointer) {
        updateUiState { it.copy(pointer = value) }
    }

    fun savePointerSettings() {
        viewModelScope.launch {
            ConfigProvider.updateGestureSettings { it.copy(pointer = uiState.pointer) }
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
        viewModelScope.launch(
            CoroutineExceptionHandler { _, _ ->
                toast(R.string.bulk_freeze_failed)
            }
        ) {
            val result = FreezeUseCase.oneKeyFreeze()
            updateUiState { it.copy(frozenAppCount = result.totalAfter) }
            showToast(AppContext.get().getString(R.string.bulk_frozen_count, result.changed))
        }
    }

    fun oneKeyUnfreeze() {
        viewModelScope.launch(
            CoroutineExceptionHandler { _, _ ->
                toast(R.string.bulk_unfreeze_failed)
            }
        ) {
            val result = FreezeUseCase.oneKeyUnfreeze()
            updateUiState { it.copy(frozenAppCount = result.totalAfter) }
            showToast(AppContext.get().getString(R.string.bulk_unfrozen_count, result.changed))
        }
    }

    fun saveDisplaySettings() {
        viewModelScope.launch {
            ConfigProvider.updateAdvancedSettings {
                it.copy(
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
            val app = AppContext.get()
            val isGestureEnabled = ConfigProvider.getInitialSettings().gestureEnabled
            @Suppress("UNCHECKED_CAST")
            val clazz = Class.forName("hunoia.luno.service.SideGestureService") as Class<out android.accessibilityservice.AccessibilityService?>
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
            ConfigProvider.resetAll()
        }
    }

    protected fun loadFrozenCount() {
        viewModelScope.launch {
            val count = FreezeUseCase.queryFrozenCount()
            updateUiState { it.copy(frozenAppCount = count) }
        }
    }

    protected fun saveSettings() {
        viewModelScope.launch {
            launch {
                ConfigProvider.updateInitialSettings {
                    it.copy(gestureEnabled = uiState.isGestureEnabled)
                }
            }
            launch {
                ConfigProvider.updateSideGestureButtons { uiState.sideGestureButtons }
            }
            launch {
                ConfigProvider.updateBottomGestureButtons { uiState.bottomGestureButtons }
            }
            launch {
                ConfigProvider.updateSubGestureSettings {
                    hunoia.luno.config.model.SubGestureSettings(subGestures = uiState.subGestures)
                }
            }
        }
    }

    protected fun loadData() {
        viewModelScope.launch {
            combine(
                ConfigProvider.initialSettings,
                ConfigProvider.sideGestureButtons,
                ConfigProvider.bottomGestureButtons,
                ConfigProvider.subGestureSettings,
                ConfigProvider.gestureSettings,
                ConfigProvider.advancedSettings,
                ConfigProvider.frozenAppSettings,
            ) { values ->
                val initial = values[0] as InitialSettings
                @Suppress("UNCHECKED_CAST")
                val sideButtons = values[1] as List<hunoia.luno.config.model.GestureButton>
                @Suppress("UNCHECKED_CAST")
                val bottomButtons = values[2] as List<hunoia.luno.config.model.GestureButton>
                val subGestureSettings = values[3] as hunoia.luno.config.model.SubGestureSettings
                val gestureSettings = values[4] as GestureSettings
                val advancedSettings = values[5] as AdvancedSettings
                val frozenAppSettings = values[6] as FrozenAppSettings
                uiState.copy(
                    isGestureEnabled = initial.gestureEnabled,
                    sideGestureButtons = sideButtons.sortedBy { it.id },
                    bottomGestureButtons = bottomButtons.sortedBy { it.id },
                    subGestures = subGestureSettings.subGestures,
                    pointer = gestureSettings.pointer,
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
}
