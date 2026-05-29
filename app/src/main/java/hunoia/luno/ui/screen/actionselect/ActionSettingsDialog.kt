package hunoia.luno.ui.screen.actionselect

import hunoia.luno.config.model.Action

class ActionSettingsDialog(
    private val onUpdateUiState: ((UiState) -> UiState) -> Unit
) {
    fun show(show: Boolean, action: Action = Action.NONE) {
        onUpdateUiState {
            it.copy(actionSettingsDialog = it.actionSettingsDialog.copy(show = show, action = action))
        }
    }
}
