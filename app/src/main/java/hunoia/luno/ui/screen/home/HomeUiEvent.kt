package hunoia.luno.ui.screen.home

sealed interface UiEvent {

    data object ScrollToBottom : UiEvent
    data class ScrollToEvent(val offsetY: Int) : UiEvent
}
