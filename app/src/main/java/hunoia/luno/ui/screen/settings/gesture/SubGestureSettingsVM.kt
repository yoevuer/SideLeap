package hunoia.luno.ui.screen.settings.gesture

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.R
import hunoia.luno.action.Action
import hunoia.luno.core.AppContext
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.payload.SubGestureActionData
import hunoia.luno.core.JsonHelper
import hunoia.luno.gesture.SubGestureDirection
import hunoia.luno.ui.navigation.SubGestureEditor
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.settings.model.SubGesture
import hunoia.luno.settings.model.SubGestureAngle
import hunoia.luno.settings.model.SubGestureSettings
import hunoia.luno.system.vibration.VibrationEffects
import hunoia.luno.ui.screen.settings.gesture.SubGestureSettingsVM.UiEvent
import hunoia.luno.ui.screen.settings.gesture.SubGestureSettingsVM.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SubGestureSettingsVM(savedStateHandle: SavedStateHandle) : BaseComposeVM<UiState, UiEvent>() {

    private val subGestureEditor = savedStateHandle.toRoute<SubGestureEditor>()

    override val initialState: UiState = UiState()

    init {
        loadData()
    }

    fun showDeleteWarningDialog(show: Boolean) {
        updateUiState { it.copy(showDeleteWarningDialog = show) }
    }

    fun showMirrorCopyDialog(show: Boolean) {
        updateUiState { it.copy(showMirrorCopyDialog = show) }
    }

    fun createMirroredCopy() {
        viewModelScope.launch {
            val original = uiState.subGesture ?: return@launch
            val newId = java.util.UUID.randomUUID().toString()
            val mirrored = original.copy(
                id = newId,
                name = AppContext.get().getString(
                    R.string.mirror_sub_gesture_name,
                    original.name.ifEmpty { AppContext.get().getString(R.string.sub_gesture) }
                ),
                leftActionId = original.rightActionId,
                rightActionId = original.leftActionId,
                upRightActionId = original.upLeftActionId,
                upLeftActionId = original.upRightActionId,
                downRightActionId = original.downLeftActionId,
                downLeftActionId = original.downRightActionId,
                angle = original.angle.copy(
                    boundaries = original.angle.boundaries.let { b ->
                        listOf(3, 2, 1, 0, 7, 6, 5, 4).map { i -> ((0.5f - b[i]) + 1f) % 1f }
                    }
                ),
            )
            SettingsProvider.updateSubGestureSettings { settings ->
                settings.copy(subGestures = settings.subGestures + mirrored)
            }
        }
    }

    fun deleteSubGesture() {
        viewModelScope.launch {
            SettingsProvider.updateSubGestureSettings { settings ->
                settings.copy(
                    subGestures = settings.subGestures.filter { it.id != subGestureEditor.subGestureId }
                )
            }
            cleanReferences(subGestureEditor.subGestureId)
        }.invokeOnCompletion {
            finish()
        }
    }

    private suspend fun cleanReferences(deletedId: String) {
        val sideButtons = SettingsProvider.getSideGestureButtons()
        val bottomButtons = SettingsProvider.getBottomGestureButtons()

        fun cleanIfSubGesture(action: Action?): Action? {
            if (action == null) return null
            if (action.value == GlobalActions.SUB_GESTURE) {
                val data = try {
                    kotlinx.serialization.json.Json.decodeFromString<SubGestureActionData>(action.data)
                } catch (_: Exception) { null }
                if (data?.id == deletedId) return null
            }
            val cleanedLongPress = cleanIfSubGesture(action.longPressAction)
            return if (cleanedLongPress != action.longPressAction) {
                action.copy(longPressAction = cleanedLongPress)
            } else {
                action
            }
        }

        fun cleanActions(buttons: List<hunoia.luno.gesture.GestureButton>): List<hunoia.luno.gesture.GestureButton> {
            return buttons.map { button ->
                button.copy(
                    slideActions = button.slideActions.copy(
                        center = button.slideActions.center.mapNotNull { cleanIfSubGesture(it) },
                        up = button.slideActions.up.mapNotNull { cleanIfSubGesture(it) },
                        down = button.slideActions.down.mapNotNull { cleanIfSubGesture(it) },
                        center2 = button.slideActions.center2.mapNotNull { cleanIfSubGesture(it) },
                        up2 = button.slideActions.up2.mapNotNull { cleanIfSubGesture(it) },
                        down2 = button.slideActions.down2.mapNotNull { cleanIfSubGesture(it) },
                    ),
                    longSlideActions = button.longSlideActions.copy(
                        center = button.longSlideActions.center.mapNotNull { cleanIfSubGesture(it) },
                        up = button.longSlideActions.up.mapNotNull { cleanIfSubGesture(it) },
                        down = button.longSlideActions.down.mapNotNull { cleanIfSubGesture(it) },
                        center2 = button.longSlideActions.center2.mapNotNull { cleanIfSubGesture(it) },
                        up2 = button.longSlideActions.up2.mapNotNull { cleanIfSubGesture(it) },
                        down2 = button.longSlideActions.down2.mapNotNull { cleanIfSubGesture(it) },
                    ),
                    tapActions = button.tapActions.copy(
                        center = button.tapActions.center.mapNotNull { cleanIfSubGesture(it) },
                        up = button.tapActions.up.mapNotNull { cleanIfSubGesture(it) },
                        down = button.tapActions.down.mapNotNull { cleanIfSubGesture(it) },
                        center2 = button.tapActions.center2.mapNotNull { cleanIfSubGesture(it) },
                        up2 = button.tapActions.up2.mapNotNull { cleanIfSubGesture(it) },
                        down2 = button.tapActions.down2.mapNotNull { cleanIfSubGesture(it) },
                    )
                )
            }
        }

        SettingsProvider.updateSideGestureButtons { cleanActions(it) }
        SettingsProvider.updateBottomGestureButtons { cleanActions(it) }
        SettingsProvider.updateSubGestureSettings { settings ->
            val cleanedSubGestures = settings.subGestures.map { gesture ->
                fun clean(id: String?) = if (id == deletedId || id == GlobalActions.SUB_GESTURE) null else id
                gesture.copy(
                    upActionId = clean(gesture.upActionId),
                    downActionId = clean(gesture.downActionId),
                    leftActionId = clean(gesture.leftActionId),
                    rightActionId = clean(gesture.rightActionId),
                    upRightActionId = clean(gesture.upRightActionId),
                    downRightActionId = clean(gesture.downRightActionId),
                    downLeftActionId = clean(gesture.downLeftActionId),
                    upLeftActionId = clean(gesture.upLeftActionId),
                )
            }
            settings.copy(subGestures = cleanedSubGestures)
        }
    }

    fun updateAngle(angle: SubGestureAngle) {
        viewModelScope.launch {
            SettingsProvider.updateSubGestureSettings { settings ->
                settings.copy(
                    subGestures = settings.subGestures.map { gesture ->
                        if (gesture.id == subGestureEditor.subGestureId) gesture.copy(angle = angle)
                        else gesture
                    }
                )
            }
        }
    }

    fun updateColor(color: Int) {
        viewModelScope.launch {
            SettingsProvider.updateSubGestureSettings { settings ->
                settings.copy(
                    subGestures = settings.subGestures.map { gesture ->
                        if (gesture.id == subGestureEditor.subGestureId) gesture.copy(color = color)
                        else gesture
                    }
                )
            }
        }
    }

    private fun updateSubGesture(fieldUpdate: SubGesture.() -> SubGesture) {
        viewModelScope.launch {
            SettingsProvider.updateSubGestureSettings { settings ->
                settings.copy(
                    subGestures = settings.subGestures.map { gesture ->
                        if (gesture.id == subGestureEditor.subGestureId) gesture.fieldUpdate()
                        else gesture
                    }
                )
            }
        }
    }

    fun onSubVibrateChange(value: Boolean) = updateSubGesture { copy(vibrate = value) }
    fun onSubVibrateImmediatelyChange(value: Boolean) = updateSubGesture { copy(vibrateImmediately = value) }
    fun onSubVibrationEffectChange(value: VibrationEffects) = updateSubGesture { copy(vibrationEffect = value) }
    fun onSubCustomVibrationMsChange(value: Float) = updateSubGesture { copy(customVibrationMs = value.toLong()) }
    fun onSubTriggerDistanceChange(value: Float) = updateSubGesture { copy(triggerDistance = value.toInt()) }

    private fun loadData() {
        viewModelScope.launch {
            SettingsProvider.subGestureSettings.collectLatest { settings ->
                val gesture = settings.subGestures.find { it.id == subGestureEditor.subGestureId }
                updateUiState {
                    it.copy(
                        subGesture = gesture,
                        allSubGestures = settings.subGestures,
                    )
                }
            }
        }
    }

    data class UiState(
        val subGesture: SubGesture? = null,
        val allSubGestures: List<SubGesture>? = null,
        val showDeleteWarningDialog: Boolean = false,
        val showMirrorCopyDialog: Boolean = false,
    )

    sealed interface UiEvent

    private object App {
        lateinit var context: android.content.Context
        fun init(ctx: android.content.Context) { context = ctx }
    }
}
