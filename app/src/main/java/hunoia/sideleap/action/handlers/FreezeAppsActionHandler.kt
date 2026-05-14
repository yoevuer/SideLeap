package hunoia.sideleap.action.handlers

import android.util.Log
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
                val result = FreezeAction.oneKeyFreeze(context.appContext)
                if (!result.shizukuAvailable) {
                    Log.e("FreezeActionHandler", "oneKeyFreeze failed: shizuku=${result.shizukuAvailable}")
                    @Suppress("HardCodedStringLiteral")
                    context.showToast("冻结功能暂不可用")
                } else {
                    val msg = context.appContext.getString(
                        R.string.bulk_frozen_count, result.successCount
                    )
                    context.showToast(msg)
                }
            }
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }
}
