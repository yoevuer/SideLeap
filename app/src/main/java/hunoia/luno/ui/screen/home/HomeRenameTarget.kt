package hunoia.luno.ui.screen.home

sealed interface RenameTarget {
    data class GestureButton(val button: hunoia.luno.config.model.GestureButton) : RenameTarget
    data class SubGesture(val gesture: hunoia.luno.config.model.SubGesture) : RenameTarget
}
