package hunoia.luno.ui.screen.settings.gesture

import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.settings.model.AnimationStyles
import hunoia.luno.settings.model.ColorSource
import hunoia.luno.settings.model.ThemeColorKey
import hunoia.luno.settings.model.WaveStyle
import hunoia.luno.ui.screen.settings.gesture.WaveStyleVM.UiEvent
import hunoia.luno.ui.screen.settings.gesture.WaveStyleVM.UiState
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.core.JsonHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlin.reflect.KCallable


class WaveStyleVM : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState()

    val colorPickerDialog = ColorPickerDialog()

    init {
        loadData()
    }

    fun onCustomIconExpandedChange(isExpanded: Boolean) {
        updateUiState {
            it.copy(isCustomIconExpanded = isExpanded)
        }
        sendUiEvent(UiEvent.ScrollToBottom)
    }

    fun onStrokeWidthChange(width: Float) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(strokeWidth = width.toInt()))
        }
    }

    fun onWidthChange(width: Float) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(width = width.toInt()))
        }
    }

    fun onStickySlideChange(value: Boolean) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(stickySlideEnabled = value))
        }
        saveSettings()
    }

    fun onLengthHalfRatioChange(ratio: Float) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(bezierLengthHalfRatio = ratio))
        }
    }

    fun onSafeBoundsChange(value: Boolean) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(safeBounds = value))
        }
        saveSettings()
    }

    fun onTransformEnabledChange(value: Boolean) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(transformEnabled = value))
        }
        saveSettings()
    }

    fun onIconScaleChange(scale: Float) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(iconScale = scale))
        }
    }

    fun onIconTypeChange(iconType: Int) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(iconType = iconType))
        }
        saveSettings()
    }

    fun onShapeTypeChange(shapeType: Int) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(shapeType = shapeType))
        }
        saveSettings()
    }

    fun onBackgroundColorSourceChange(source: ColorSource) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(backgroundColorSource = source))
        }
        saveSettings()
    }

    fun onStrokeColorSourceChange(source: ColorSource) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(strokeColorSource = source))
        }
        saveSettings()
    }

    fun onIconColorSourceChange(source: ColorSource) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(iconColorSource = source))
        }
        saveSettings()
    }

    fun onBackgroundColorThemeKeyChange(key: ThemeColorKey) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(backgroundColorThemeKey = key))
        }
        saveSettings()
    }

    fun onStrokeColorThemeKeyChange(key: ThemeColorKey) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(strokeColorThemeKey = key))
        }
        saveSettings()
    }

    fun onIconColorThemeKeyChange(key: ThemeColorKey) {
        updateUiState {
            it.copy(animationStyle = it.animationStyle.copy(iconColorThemeKey = key))
        }
        saveSettings()
    }

    fun saveSettings() {
        viewModelScope.launch {
            SettingsProvider.updateAdvancedSettings {
                it.copy(
                    animationStyles = it.animationStyles.copy(
                        type = AnimationStyles.TYPE_WAVE,
                        json = JsonHelper.encodeToString(uiState.animationStyle)
                    )
                )
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            SettingsProvider
                .advancedSettings
                .take(1)
                .collectLatest { advancedSettings ->
                    updateUiState {
                        val waveStyle = advancedSettings.animationStyles.value as WaveStyle
                        it.copy(
                            animationStyle = waveStyle,
                            isCustomIconExpanded = it.isCustomIconExpanded || waveStyle.iconType != WaveStyle.ICON_TYPE_ARROW
                        )
                    }
                }
        }
    }

    inner class ColorPickerDialog {

        private var belongsTo: KCallable<Any>? = null

        fun show(
            show: Boolean,
            color: Int = 0,
            belongsTo: KCallable<Any>? = null
        ) {
            this.belongsTo = belongsTo
            updateUiState {
                it.copy(colorPickerDialog = Pair(show, color))
            }
        }

        fun onColorChange(color: Int) {
            updateUiState {
                it.copy(colorPickerDialog = it.colorPickerDialog.copy(second = color))
            }
        }

        fun confirm() {
            updateUiState {
                val pickedColor = it.colorPickerDialog.second
                it.copy(
                    colorPickerDialog = it.colorPickerDialog.copy(first = false),
                    animationStyle = it.animationStyle.copy(
                        backgroundColor = when (belongsTo == it.animationStyle::backgroundColor) {
                            true -> pickedColor
                            else -> it.animationStyle.backgroundColor
                        },
                        strokeColor = when (belongsTo == it.animationStyle::strokeColor) {
                            true -> pickedColor
                            else -> it.animationStyle.strokeColor
                        },
                        iconColor = when (belongsTo == it.animationStyle::iconColor) {
                            true -> pickedColor
                            else -> it.animationStyle.iconColor
                        }
                    )
                )
            }
            belongsTo = null
            saveSettings()
        }
    }

    data class UiState(
        val animationStyle: WaveStyle = WaveStyle(),
        val isCustomIconExpanded: Boolean = false,
        val colorPickerDialog: Pair<Boolean, Int> = Pair(false, 0)
    )

    sealed interface UiEvent {

        data object ScrollToBottom : UiEvent
    }
}
