package hunoia.sideleap.action.handlers

import hunoia.sideleap.R
import hunoia.sideleap.action.api.ActionExecutionResult
import hunoia.sideleap.action.api.ActionHandler
import hunoia.sideleap.action.api.ActionHandlerContext
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.freeze.api.FreezeAction

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
