package hunoia.luno.ui.screen.actionselect

import hunoia.luno.ui.navigation.IconResize

sealed interface UiEvent {
    data class GotoIconResize(val iconResize: IconResize) : UiEvent
}
