package hunoia.sideleap.action.definition

import hunoia.sideleap.action.Action

data class ActionDefinition(
    val actionId: String,
    val category: ActionCategory,
    val configKind: ActionConfigKind,
    val titleKey: String,
    val iconKey: String,
    val isDisplayed: Boolean = true
) {
    fun toAction(): Action = Action(actionId)
}
