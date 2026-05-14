package hunoia.sideleap.action.handlers

import hunoia.sideleap.R
import hunoia.sideleap.action.ActionExecutionResult
import hunoia.sideleap.action.ActionHandler
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.freeze.FreezeAction

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
