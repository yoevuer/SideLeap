package hunoia.luno.ui.screen.home

import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.settings.model.AnimationStyles
import hunoia.luno.settings.model.BubbleStyle
import hunoia.luno.settings.model.CapsuleStyle
import hunoia.luno.settings.model.ColorSource
import hunoia.luno.settings.model.LineStyle
import hunoia.luno.settings.model.SnapBackType
import hunoia.luno.settings.model.ThemeColorKey
import hunoia.luno.settings.model.WaveStyle
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.core.serialization.JsonHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlin.reflect.KCallable

class AnimationStyleVM : BaseComposeVM<AnimationStyleVM.UiState, AnimationStyleVM.UiEvent>() {

    override val initialState: UiState = UiState()

    val colorPickerDialog = ColorPickerDialog()

    init {
        loadData()
    }

    fun onTabSelected(type: Int) {
        if (type == uiState.currentType) return
        viewModelScope.launch {
            SettingsProvider.updateAdvancedSettings { advanced ->
                val updatedStyles = advanced.animationStyles.selectType(type)
                advanced.copy(animationStyles = updatedStyles)
            }
        }
        updateUiState { it.copy(currentType = type) }
    }

    fun onWaveStyleChange(transform: (WaveStyle) -> WaveStyle) {
        val old = uiState.waveStyle
        val new = transform(old)
        updateUiState { it.copy(waveStyle = new) }
    }

    fun onCapsuleStyleChange(transform: (CapsuleStyle) -> CapsuleStyle) {
        val old = uiState.capsuleStyle
        val new = transform(old)
        updateUiState { it.copy(capsuleStyle = new) }
    }

    fun onBubbleStyleChange(transform: (BubbleStyle) -> BubbleStyle) {
        val old = uiState.bubbleStyle
        val new = transform(old)
        updateUiState { it.copy(bubbleStyle = new) }
    }

    fun onLineStyleChange(transform: (LineStyle) -> LineStyle) {
        val old = uiState.lineStyle
        val new = transform(old)
        updateUiState { it.copy(lineStyle = new) }
    }

    fun onWaveCustomIconExpandedChange(isExpanded: Boolean) {
        updateUiState { it.copy(waveCustomIconExpanded = isExpanded) }
        sendUiEvent(UiEvent.ScrollToBottom)
    }

    fun onCapsuleCustomIconExpandedChange(isExpanded: Boolean) {
        updateUiState { it.copy(capsuleCustomIconExpanded = isExpanded) }
        sendUiEvent(UiEvent.ScrollToBottom)
    }

    fun onBubbleCustomIconExpandedChange(isExpanded: Boolean) {
        updateUiState { it.copy(bubbleCustomIconExpanded = isExpanded) }
        sendUiEvent(UiEvent.ScrollToBottom)
    }

    fun onLineCustomIconExpandedChange(isExpanded: Boolean) {
        updateUiState { it.copy(lineCustomIconExpanded = isExpanded) }
        sendUiEvent(UiEvent.ScrollToBottom)
    }

    fun saveWaveSettings() {
        viewModelScope.launch {
            SettingsProvider.updateAdvancedSettings { advanced ->
                val payload = JsonHelper.encodeToString(uiState.waveStyle)
                advanced.copy(
                    animationStyles = advanced.animationStyles.updateStyle(AnimationStyles.TYPE_WAVE, payload)
                )
            }
        }
    }

    fun saveCapsuleSettings() {
        viewModelScope.launch {
            SettingsProvider.updateAdvancedSettings { advanced ->
                val payload = JsonHelper.encodeToString(uiState.capsuleStyle)
                advanced.copy(
                    animationStyles = advanced.animationStyles.updateStyle(AnimationStyles.TYPE_CAPSULE, payload)
                )
            }
        }
    }

    fun saveBubbleSettings() {
        viewModelScope.launch {
            SettingsProvider.updateAdvancedSettings { advanced ->
                val payload = JsonHelper.encodeToString(uiState.bubbleStyle)
                advanced.copy(
                    animationStyles = advanced.animationStyles.updateStyle(AnimationStyles.TYPE_BUBBLE, payload)
                )
            }
        }
    }

    fun saveCurrentStyle(type: Int) {
        when (type) {
            AnimationStyles.TYPE_WAVE -> saveWaveSettings()
            AnimationStyles.TYPE_CAPSULE -> saveCapsuleSettings()
            AnimationStyles.TYPE_BUBBLE -> saveBubbleSettings()
            AnimationStyles.TYPE_LINE -> saveLineSettings()
        }
    }

    fun saveLineSettings() {
        viewModelScope.launch {
            SettingsProvider.updateAdvancedSettings { advanced ->
                val payload = JsonHelper.encodeToString(uiState.lineStyle)
                advanced.copy(
                    animationStyles = advanced.animationStyles.updateStyle(AnimationStyles.TYPE_LINE, payload)
                )
            }
        }
    }

        private fun loadData() {
        viewModelScope.launch {
            SettingsProvider
                .advancedSettings
                .take(1)
                .collectLatest { advanced ->
                    val styles = advanced.animationStyles
                    val waveStyle = styles.payloadOf(AnimationStyles.TYPE_WAVE).let {
                        if (it.isEmpty()) WaveStyle() else JsonHelper.decodeFromString<WaveStyle>(it)
                    }
                    val capsuleStyle = styles.payloadOf(AnimationStyles.TYPE_CAPSULE).let {
                        if (it.isEmpty()) CapsuleStyle() else JsonHelper.decodeFromString<CapsuleStyle>(it)
                    }
                    val bubbleStyle = styles.payloadOf(AnimationStyles.TYPE_BUBBLE).let {
                        if (it.isEmpty()) BubbleStyle() else JsonHelper.decodeFromString<BubbleStyle>(it)
                    }
                    val lineStyle = styles.payloadOf(AnimationStyles.TYPE_LINE).let {
                        if (it.isEmpty()) LineStyle() else JsonHelper.decodeFromString<LineStyle>(it)
                    }
                    updateUiState {
                        it.copy(
                            currentType = styles.type,
                            waveStyle = waveStyle,
                            capsuleStyle = capsuleStyle,
                            bubbleStyle = bubbleStyle,
                            lineStyle = lineStyle,
                            waveCustomIconExpanded = it.waveCustomIconExpanded || waveStyle.iconType != WaveStyle.ICON_TYPE_ARROW,
                            capsuleCustomIconExpanded = it.capsuleCustomIconExpanded || capsuleStyle.iconType != CapsuleStyle.ICON_TYPE_ARROW,
                            bubbleCustomIconExpanded = it.bubbleCustomIconExpanded || bubbleStyle.iconType != BubbleStyle.ICON_TYPE_ARROW,
                            lineCustomIconExpanded = it.lineCustomIconExpanded || lineStyle.iconType != LineStyle.ICON_TYPE_ARROW
                        )
                    }
                }
        }
    }

    inner class ColorPickerDialog {

        private var belongsTo: KCallable<Any>? = null
        private var targetType: Int = AnimationStyles.TYPE_WAVE

        fun show(
            show: Boolean,
            color: Int = 0,
            belongsTo: KCallable<Any>? = null,
            targetType: Int = AnimationStyles.TYPE_WAVE
        ) {
            this.belongsTo = belongsTo
            this.targetType = targetType
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
            val pickedColor = uiState.colorPickerDialog.second
            updateUiState {
                when (targetType) {
                    AnimationStyles.TYPE_WAVE -> {
                        it.copy(
                            colorPickerDialog = it.colorPickerDialog.copy(first = false),
                            waveStyle = it.waveStyle.copy(
                                backgroundColor = when (belongsTo == it.waveStyle::backgroundColor) {
                                    true -> pickedColor
                                    else -> it.waveStyle.backgroundColor
                                },
                                strokeColor = when (belongsTo == it.waveStyle::strokeColor) {
                                    true -> pickedColor
                                    else -> it.waveStyle.strokeColor
                                },
                                iconColor = when (belongsTo == it.waveStyle::iconColor) {
                                    true -> pickedColor
                                    else -> it.waveStyle.iconColor
                                }
                            )
                        )
                    }
                    AnimationStyles.TYPE_CAPSULE -> {
                        it.copy(
                            colorPickerDialog = it.colorPickerDialog.copy(first = false),
                            capsuleStyle = it.capsuleStyle.copy(
                                backgroundColor = when (belongsTo == it.capsuleStyle::backgroundColor) {
                                    true -> pickedColor
                                    else -> it.capsuleStyle.backgroundColor
                                },
                                strokeColor = when (belongsTo == it.capsuleStyle::strokeColor) {
                                    true -> pickedColor
                                    else -> it.capsuleStyle.strokeColor
                                },
                                iconColor = when (belongsTo == it.capsuleStyle::iconColor) {
                                    true -> pickedColor
                                    else -> it.capsuleStyle.iconColor
                                }
                            )
                        )
                    }
                    AnimationStyles.TYPE_BUBBLE -> {
                        it.copy(
                            colorPickerDialog = it.colorPickerDialog.copy(first = false),
                            bubbleStyle = it.bubbleStyle.copy(
                                backgroundColor = when (belongsTo == it.bubbleStyle::backgroundColor) {
                                    true -> pickedColor
                                    else -> it.bubbleStyle.backgroundColor
                                },
                                strokeColor = when (belongsTo == it.bubbleStyle::strokeColor) {
                                    true -> pickedColor
                                    else -> it.bubbleStyle.strokeColor
                                },
                                iconColor = when (belongsTo == it.bubbleStyle::iconColor) {
                                    true -> pickedColor
                                    else -> it.bubbleStyle.iconColor
                                }
                            )
                        )
                    }
                    else -> it
                }
            }
            belongsTo = null
            when (targetType) {
                AnimationStyles.TYPE_WAVE -> saveWaveSettings()
                AnimationStyles.TYPE_CAPSULE -> saveCapsuleSettings()
                AnimationStyles.TYPE_BUBBLE -> saveBubbleSettings()
            }
        }
    }

    data class UiState(
        val currentType: Int = AnimationStyles.TYPE_WAVE,
        val waveStyle: WaveStyle = WaveStyle(),
        val capsuleStyle: CapsuleStyle = CapsuleStyle(),
        val bubbleStyle: BubbleStyle = BubbleStyle(),
        val lineStyle: LineStyle = LineStyle(),
        val waveCustomIconExpanded: Boolean = false,
        val capsuleCustomIconExpanded: Boolean = false,
        val bubbleCustomIconExpanded: Boolean = false,
        val lineCustomIconExpanded: Boolean = false,
        val colorPickerDialog: Pair<Boolean, Int> = Pair(false, 0)
    )

    sealed interface UiEvent {
        data object ScrollToBottom : UiEvent
    }
}
