package hunoia.luno.action.handlers

import hunoia.luno.R
import hunoia.luno.action.api.ActionExecutionResult
import hunoia.luno.action.api.ActionHandler
import hunoia.luno.action.api.ActionHandlerContext
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.Action
import hunoia.luno.freeze.api.FreezeAction

object FreezeAppsActionHandler : ActionHandler {

    override val supportedActions = setOf(GlobalActions.ONE_KEY_FREEZE_APPS)

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.ONE_KEY_FREEZE_APPS -> {
                val result = FreezeAction.oneKeyFreezeForService(context.appContext)
                val msg = context.appContext.getString(R.string.bulk_frozen_count, result.successCount)
                context.showToast(msg)
            }
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }
}
