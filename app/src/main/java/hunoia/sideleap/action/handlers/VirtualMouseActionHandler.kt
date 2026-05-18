package hunoia.sideleap.action.handlers

import hunoia.sideleap.action.Action
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.api.ActionExecutionResult
import hunoia.sideleap.action.api.ActionHandler
import hunoia.sideleap.action.api.ActionHandlerContext

object VirtualMouseActionHandler : ActionHandler {
    override val supportedActions = setOf(GlobalActions.VIRTUAL_MOUSE)

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        return if (context.showVirtualMouse()) {
            ActionExecutionResult.Success
        } else {
            ActionExecutionResult.Ignored
        }
    }
}
