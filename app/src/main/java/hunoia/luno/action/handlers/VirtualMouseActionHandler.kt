package hunoia.luno.action.handlers

import hunoia.luno.action.Action
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.api.ActionExecutionResult
import hunoia.luno.action.api.ActionHandler
import hunoia.luno.action.api.ActionHandlerContext
import hunoia.luno.action.virtualMouseContinuousModeOverride

object VirtualMouseActionHandler : ActionHandler {
    override val supportedActions = setOf(GlobalActions.VIRTUAL_MOUSE)

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        return if (context.showVirtualMouse(action.virtualMouseContinuousModeOverride())) {
            ActionExecutionResult.Success
        } else {
            ActionExecutionResult.Ignored
        }
    }
}
