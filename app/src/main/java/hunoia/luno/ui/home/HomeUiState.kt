package hunoia.luno.ui.home

import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.config.model.SubGesture

sealed interface RenameTarget {
    val name: String
    data class GestureButton(val button: hunoia.luno.config.model.GestureButton) : RenameTarget {
        override val name: String get() = button.name
    }
    data class SubGesture(val gesture: hunoia.luno.config.model.SubGesture) : RenameTarget {
        override val name: String get() = gesture.name
    }
}

sealed interface UiEvent {
    data object ScrollToBottom : UiEvent
    data class ScrollToEvent(val offsetY: Int) : UiEvent
}

data class UiState(
    val sideGestureButtons: List<GestureButton> = emptyList(),
    val bottomGestureButtons: List<GestureButton> = emptyList(),
    val subGestures: List<SubGesture> = emptyList(),
    val isGestureEnabled: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isIgnoringBatteryOptimizations: Boolean = false,
    val isSubGestureListExpanded: Boolean = false,
    val isBottomGestureButtonListExpanded: Boolean = false,
    val isSideGestureButtonListExpanded: Boolean = false,
    val pointer: GestureSettings.Pointer = GestureSettings.Pointer(),
    val miniWindowHorizontalBias: Float = 0f,
    val miniWindowVerticalBias: Float = 0f,
    val miniWindowVerticalOffsetFraction: Float = 0f,
    val miniWindowWidthFraction: Float = 0.46f,
    val miniWindowHeightFraction: Float = 0.74f,
    val miniWindowOverrideBounds: Boolean = false,
    val excludedAppCount: Int = 0,
    val frozenAppCount: Int = 0,
    val selectedFrozenAppCount: Int = 0,
    val renameDialogTarget: RenameTarget? = null,
)
