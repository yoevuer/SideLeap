package hunoia.luno.action.handlers

import hunoia.luno.action.Action
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.api.ActionExecutionResult
import hunoia.luno.action.api.ActionHandler
import hunoia.luno.action.api.ActionHandlerContext
import hunoia.luno.action.pointerContinuousModeOverride

object PointerActionHandler : ActionHandler {
    override val supportedActions = setOf(GlobalActions.POINTER)

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        return if (context.showPointer(action.pointerContinuousModeOverride())) {
            ActionExecutionResult.Success
        } else {
            ActionExecutionResult.Ignored
        }
    }
}
