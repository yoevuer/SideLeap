package hunoia.sideleap.ui.screen.settings.gesture

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.payload.SubGestureActionData
import hunoia.sideleap.core.serialization.JsonHelper
import hunoia.sideleap.gesture.SubGestureDirection
import hunoia.sideleap.ui.navigation.SubGestureEditor
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.settings.model.SubGesture
import hunoia.sideleap.settings.model.SubGestureAngle
import hunoia.sideleap.settings.model.SubGestureSettings
import hunoia.sideleap.ui.screen.settings.gesture.SubGestureSettingsVM.UiEvent
import hunoia.sideleap.ui.screen.settings.gesture.SubGestureSettingsVM.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SubGestureSettingsVM(savedStateHandle: SavedStateHandle) : BaseComposeVM<UiState, UiEvent>() {

    private val subGestureEditor = savedStateHandle.toRoute<SubGestureEditor>()

    override val initialState: UiState = UiState()

    val colorPickerDialog = ColorPickerDialog()

    init {
        loadData()
    }

    fun updateName(name: String) {
        updateUiState { it.copy(editingName = name) }
    }

    fun confirmName() {
        viewModelScope.launch {
            val name = uiState.editingName
            if (name.isBlank()) return@launch
            SettingsProvider.updateSubGestureSettings { settings ->
                settings.copy(
                    subGestures = settings.subGestures.map { gesture ->
                        if (gesture.id == subGestureEditor.subGestureId) gesture.copy(name = name)
                        else gesture
                    }
                )
            }
        }
    }

    fun showDeleteWarningDialog(show: Boolean) {
        updateUiState { it.copy(showDeleteWarningDialog = show) }
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

        fun cleanActions(buttons: List<hunoia.sideleap.gesture.GestureButton>): List<hunoia.sideleap.gesture.GestureButton> {
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
                gesture.copy(
                    upAction = cleanIfSubGesture(gesture.upAction),
                    downAction = cleanIfSubGesture(gesture.downAction),
                    leftAction = cleanIfSubGesture(gesture.leftAction),
                    rightAction = cleanIfSubGesture(gesture.rightAction),
                    upRightAction = cleanIfSubGesture(gesture.upRightAction),
                    downRightAction = cleanIfSubGesture(gesture.downRightAction),
                    downLeftAction = cleanIfSubGesture(gesture.downLeftAction),
                    upLeftAction = cleanIfSubGesture(gesture.upLeftAction),
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

    inner class ColorPickerDialog {

        fun show(show: Boolean) {
            updateUiState {
                val color = it.subGesture?.let { g -> Color(g.color) } ?: it.colorPickerDialog.second
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
                it.copy(colorPickerDialog = it.colorPickerDialog.copy(first = false))
            }
            val picked = uiState.colorPickerDialog.second
            updateColor(picked.toArgb())
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            SettingsProvider.subGestureSettings.collectLatest { settings ->
                val gesture = settings.subGestures.find { it.id == subGestureEditor.subGestureId }
                updateUiState {
                    it.copy(
                        subGesture = gesture,
                        allSubGestures = settings.subGestures,
                        editingName = gesture?.name ?: ""
                    )
                }
            }
        }
    }

    data class UiState(
        val subGesture: SubGesture? = null,
        val allSubGestures: List<SubGesture>? = null,
        val editingName: String = "",
        val showDeleteWarningDialog: Boolean = false,
        val colorPickerDialog: Pair<Boolean, Color> = Pair(false, Color.Transparent),
    )

    sealed interface UiEvent

    private object App {
        lateinit var context: android.content.Context
        fun init(ctx: android.content.Context) { context = ctx }
    }
}
