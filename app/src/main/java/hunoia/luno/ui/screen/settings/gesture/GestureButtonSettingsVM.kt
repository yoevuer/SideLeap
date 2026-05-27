package hunoia.luno.ui.screen.settings.gesture

import android.os.Build

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinGestureButtonLength
import hunoia.luno.gesture.GestureAngle
import hunoia.luno.gesture.GestureButton
import hunoia.luno.gesture.TriggerDirection
import hunoia.luno.ui.navigation.GestureButtonSettings
import hunoia.luno.gesture.fraction
import hunoia.luno.settings.model.ActionPanelStyles
import hunoia.luno.system.vibration.VibrationEffects
import hunoia.luno.system.window.rootSize
import hunoia.luno.ui.screen.settings.gesture.GestureButtonSettingsVM.UiEvent
import hunoia.luno.ui.screen.settings.gesture.GestureButtonSettingsVM.UiState
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.core.DensityProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/28
 */
class GestureButtonSettingsVM(savedStateHandle: SavedStateHandle) : BaseComposeVM<UiState, UiEvent>() {

    private val gestureButtonSettings = savedStateHandle.toRoute<GestureButtonSettings>()

    override val initialState: UiState = UiState(gestureButtonSettings)

    val colorPickerDialog = ColorPickerDialog()

    private val maxExcludeSystemGestureFraction: Float by lazy {
        val rootSize = rootSize
        val maxExcludeSystemGestureHeight = DensityProvider.dp2px(200f)
        maxExcludeSystemGestureHeight.toFloat() / rootSize.height.toFloat()
    }

    private var loadDataJob: Job? = null

    init {
        loadData()
    }

    fun showDeleteWarningDialog(show: Boolean) {
        updateUiState {
            it.copy(showDeleteWarningDialog = show)
        }
    }

    fun showCopyAnotherSideGestureButtonDialog(show: Boolean) {
        updateUiState {
            it.copy(showCopyAnotherSideGestureButtonDialog = show)
        }
    }

    fun deleteGestureButton() {
        viewModelScope.launch {
            loadDataJob?.cancel()
            if (gestureButtonSettings.isSideButton) {
                SettingsProvider.updateSideGestureButtons {
                    it.toMutableList().apply {
                        removeAll { item ->
                            item.id == uiState.gestureButton?.id
                        }
                    }
                }
            } else {
                SettingsProvider.updateBottomGestureButtons {
                    it.toMutableList().apply {
                        removeAll { item ->
                            item.id == uiState.gestureButton?.id
                        }
                    }
                }
            }
        }.invokeOnCompletion {
            finish()
        }
    }

    fun copyAnotherSideGestureButton() {
        updateUiState {
            val curButton = it.gestureButton ?: return@updateUiState it
            val l = it.gestureButtons.toMutableList().also { list ->
                val anotherSideButton = list.find { b ->
                    b.id == curButton.id && b.position != curButton.position
                }
                if (anotherSideButton != null) {
                    val index = list.indexOf(anotherSideButton)
                    list[index] = curButton.copy(position = anotherSideButton.position)
                }
            }
            it.copy(gestureButtons = l)
        }
        saveSettings()
    }

    fun onGestureButtonWidthChange(width: Float) {
        updateUiState {
            val l = it.gestureButtons.toMutableList().also { list ->
                list.forEachIndexed { index, b ->
                    if (b.id != gestureButtonSettings.buttonId) {
                        return@forEachIndexed
                    }
                    if (b.position == gestureButtonSettings.position || it.alignRegion) {
                        list[index] = b.copy(width = width.toInt())
                    }
                }
            }
            it.copy(
                gestureButtons = l,
                isGestureButtonAdjusting = true
            )
        }
    }

    fun onGestureButtonPositionChange(start: Float, end: Float) {
        val button = uiState.gestureButton ?: return
        val fraction = end - start
        if (fraction < MinGestureButtonLength ||
            (button.excludeSystemGestureRects &&
                    button.limitMaxExcludeSystemGestureLength &&
                    fraction > maxExcludeSystemGestureFraction)
        ) {
            return
        }
        updateUiState {
            val l = it.gestureButtons.toMutableList().also { list ->
                list.forEachIndexed { index, b ->
                    if (b.id != gestureButtonSettings.buttonId) {
                        return@forEachIndexed
                    }
                    if (b.position == gestureButtonSettings.position || it.alignRegion) {
                        list[index] = b.copy(
                            start = start,
                            end = end
                        )
                    }
                }
            }
            it.copy(
                gestureButtons = l,
                isGestureButtonAdjusting = true
            )
        }
    }

    fun onGestureButtonAdjustFinish() {
        updateUiState {
            it.copy(isGestureButtonAdjusting = false)
        }
        saveSettings()
    }

    fun updateGestureButtonAngle(angle: GestureAngle) {
        updateUiState {
            val l = it.gestureButtons.toMutableList().also { list ->
                list.forEachIndexed { index, b ->
                    if (b.id == gestureButtonSettings.buttonId && b.position == gestureButtonSettings.position) {
                        list[index] = b.copy(angle = angle)
                    }
                }
            }
            it.copy(gestureButtons = l)
        }
        saveSettings()
    }

    fun updateLongSlideActionPanelStyle(direction: TriggerDirection, style: ActionPanelStyles) {
        updateUiState {
            val l = it.gestureButtons.toMutableList().also { list ->
                list.forEachIndexed { index, b ->
                    if (b.id == gestureButtonSettings.buttonId && b.position == gestureButtonSettings.position) {
                        val styles = b.longSlideActionPanelStyles
                        val newStyles = when (direction) {
                            TriggerDirection.Center -> styles.copy(center = style)
                            TriggerDirection.Up -> styles.copy(up = style)
                            TriggerDirection.Down -> styles.copy(down = style)
                            TriggerDirection.Up2 -> styles.copy(up2 = style)
                            TriggerDirection.Down2 -> styles.copy(down2 = style)
                            TriggerDirection.Center2 -> styles
                        }
                        list[index] = b.copy(longSlideActionPanelStyles = newStyles)
                    }
                }
            }
            it.copy(gestureButtons = l)
        }
        saveSettings()
    }

    private fun updateButton(fieldUpdate: GestureButton.() -> GestureButton) {
        updateUiState {
            val l = it.gestureButtons.toMutableList().also { list ->
                list.forEachIndexed { index, b ->
                    if (b.id == gestureButtonSettings.buttonId && b.position == gestureButtonSettings.position) {
                        list[index] = b.fieldUpdate()
                    }
                }
            }
            it.copy(gestureButtons = l)
        }
        saveSettings()
    }

    fun onFitSoftKeyboardChange(value: Boolean) = updateButton { copy(fitSoftKeyboard = value) }
    fun onPreciseSlideTypeChange(value: Boolean) = updateButton { copy(isPreciseSlideType = value) }
    fun onHideLandscapeChange(value: Boolean) = updateButton { copy(hideLandscape = value) }
    fun onHideScreenLockChange(value: Boolean) = updateButton { copy(hideScreenLock = value) }
    fun onHideHomeScreenChange(value: Boolean) = updateButton { copy(hideHomeScreen = value) }
    fun onSlideVibrateChange(value: Boolean) = updateButton { copy(slideVibrate = value) }
    fun onLongSlideVibrateChange(value: Boolean) = updateButton { copy(longSlideVibrate = value) }
    fun onTapVibrateChange(value: Boolean) = updateButton { copy(tapVibrate = value) }
    fun onLongPressVibrateChange(value: Boolean) = updateButton { copy(longPressVibrate = value) }
    fun onVibrateImmediatelyChange(value: Boolean) = updateButton { copy(vibrateImmediately = value) }
    fun onVibrationEffectChange(value: VibrationEffects) = updateButton { copy(vibrationEffect = value) }
    fun onCustomVibrationMsChange(value: Float) = updateButton { copy(customVibrationMs = value.toLong()) }
    fun onSlideTriggerDistanceChange(value: Float) = updateButton { copy(slideTriggerDistance = value.toInt()) }
    fun onLongSlideTriggerDistanceChange(value: Float) = updateButton { copy(longSlideTriggerDistance = value.toInt()) }
    fun onLongPressTriggerDelayMsChange(value: Float) = updateButton { copy(longPressTriggerDelayMs = value.toLong()) }
    fun onLongSlideTriggerImmediatelyChange(value: Boolean) = updateButton { copy(longSlideTriggerImmediately = value) }
    fun onLongSlideTriggerDelayMsChange(value: Float) = updateButton { copy(longSlideTriggerDelayMs = value.toLong()) }

    fun onGestureButtonAlignChange(value: Boolean) {
        updateUiState {
            val button = it.gestureButton
            val list = if (button == null) it.gestureButtons else {
                it.gestureButtons.toMutableList().apply {
                    forEachIndexed { index, b ->
                        if (button.id == b.id) {
                            if (value) {
                                val newB = b.copy(
                                    width = button.width,
                                    start = button.start,
                                    end = button.end,
                                    alignRegion = true
                                )
                                set(index, newB)
                            } else {
                                val newB = b.copy(alignRegion = false)
                                set(index, newB)
                            }
                        }
                    }
                }
            }
            it.copy(gestureButtons = list)
        }
        saveSettings()
    }

    fun onExcludeSystemGestureRectsChange(value: Boolean) {
        updateUiState {
            val l = it.gestureButtons.toMutableList().also { list ->
                list.forEachIndexed { index, b ->
                    if (b.id != gestureButtonSettings.buttonId) {
                        return@forEachIndexed
                    }
                    if (b.position == gestureButtonSettings.position || it.alignRegion) {
                        val (start, end) = if (value && b.limitMaxExcludeSystemGestureLength) {
                            val maxFraction = maxExcludeSystemGestureFraction
                            val half = maxFraction / 2f
                            val center = b.start + (b.fraction / 2f)
                            val st = center - half
                            val ed = center + half
                            Pair(st, ed)
                        } else {
                            Pair(b.start, b.end)
                        }
                        list[index] = b.copy(
                            start = start,
                            end = end,
                            excludeSystemGestureRects = value
                        )
                    }
                }
            }
            it.copy(gestureButtons = l)
        }
        saveSettings()
    }

    fun onLimitMaxExcludeSystemGestureLengthChange(value: Boolean) {
        updateUiState {
            val l = it.gestureButtons.toMutableList().also { list ->
                list.forEachIndexed { index, b ->
                    if (b.id != gestureButtonSettings.buttonId) {
                        return@forEachIndexed
                    }
                    if (b.position == gestureButtonSettings.position || it.alignRegion) {
                        val (start, end) = if (value) {
                            val maxFraction = maxExcludeSystemGestureFraction
                            val half = maxFraction / 2f
                            val center = b.start + (b.fraction / 2f)
                            val st = center - half
                            val ed = center + half
                            Pair(st, ed)
                        } else {
                            Pair(b.start, b.end)
                        }
                        list[index] = b.copy(
                            start = start,
                            end = end,
                            limitMaxExcludeSystemGestureLength = value
                        )
                    }
                }
            }
            it.copy(gestureButtons = l)
        }
        saveSettings()
    }

    fun saveSettings() {
        viewModelScope.launch {
            launch {
                if (gestureButtonSettings.isSideButton) {
                    SettingsProvider.updateSideGestureButtons {
                        uiState.gestureButtons
                    }
                } else {
                    SettingsProvider.updateBottomGestureButtons {
                        uiState.gestureButtons
                    }
                }
            }
        }
    }

    private fun loadData() {
        val gestureButtonSettings = gestureButtonSettings
        loadDataJob = viewModelScope.launch {
            updateUiState {
                val canShowExcludeSystemGestureRects =
                    gestureButtonSettings.isSideButton &&
                    gestureButtonSettings.buttonId == GestureButton.ID_DEFAULT
                it.copy(
                    canShowExcludeSystemGestureRects = canShowExcludeSystemGestureRects
                )
            }
            launch {
                if (gestureButtonSettings.isSideButton) {
                    SettingsProvider
                        .sideGestureButtons
                        .collectLatest { items ->
                            val button = items.find {
                                it.id == gestureButtonSettings.buttonId &&
                                        it.position == gestureButtonSettings.position
                            }
                            updateUiState {
                                it.copy(
                                    gestureButtons = items,
                                    alignRegion = button?.alignRegion ?: true
                                )
                            }
                        }
                } else {
                    SettingsProvider
                        .bottomGestureButtons
                        .collectLatest { items ->
                            updateUiState {
                                it.copy(gestureButtons = items)
                            }
                        }
                }
            }
        }
    }

    inner class ColorPickerDialog {

        fun show(show: Boolean) {
            updateUiState {
                val color = it.gestureButton?.let { b -> Color(b.color) } ?: it.colorPickerDialog.second
                it.copy(
                    colorPickerDialog = it.colorPickerDialog.copy(first = show, second = color)
                )
            }
        }

        fun onColorChange(color: Color) {
            updateUiState {
                it.copy(colorPickerDialog = it.colorPickerDialog.copy(second = color))
            }
        }

        fun confirm() {
            updateUiState {
                val pickedColor = it.colorPickerDialog.second
                val l = it.gestureButtons.toMutableList().also { list ->
                    val gestureButtonSettings = gestureButtonSettings
                    list.forEachIndexed { index, b ->
                        if (b.id != gestureButtonSettings.buttonId) {
                            return@forEachIndexed
                        }
                        if (b.position == gestureButtonSettings.position || it.alignRegion) {
                            list[index] = b.copy(color = pickedColor.toArgb())
                        }
                    }
                }
                it.copy(gestureButtons = l)
            }
            saveSettings()
        }
    }

    data class UiState(
        val gestureButtonSettings: GestureButtonSettings,
        val gestureButtons: List<GestureButton> = emptyList(),
        val alignRegion: Boolean = true,
        val showDeleteWarningDialog: Boolean = false,
        val colorPickerDialog: Pair<Boolean, Color> = Pair(false, Color.Transparent),
        val isGestureButtonAdjusting: Boolean = false,
        val canShowExcludeSystemGestureRects: Boolean = false,
        val showCopyAnotherSideGestureButtonDialog: Boolean = false
    ) {
        val gestureButton: GestureButton? = gestureButtons.find {
            it.id == gestureButtonSettings.buttonId &&
                    it.position == gestureButtonSettings.position
        }
    }

    sealed interface UiEvent
}
