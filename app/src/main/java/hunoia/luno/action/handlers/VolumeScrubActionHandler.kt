package hunoia.luno.action.handlers

import hunoia.luno.action.Action
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.api.ActionExecutionResult
import hunoia.luno.action.api.ActionHandler
import hunoia.luno.action.api.ActionHandlerContext

object VolumeScrubActionHandler : ActionHandler {
    override val supportedActions = setOf(GlobalActions.VOLUME_SCRUB)

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        return if (context.showVolumeScrub()) {
            ActionExecutionResult.Success
        } else {
            ActionExecutionResult.Ignored
        }
    }
}
