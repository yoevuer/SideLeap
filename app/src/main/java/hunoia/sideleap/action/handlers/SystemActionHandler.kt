package hunoia.sideleap.action.handlers

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_POWER_DIALOG
import hunoia.sideleap.action.ActionHandler
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.entity.Action

object SystemActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.POWER_BUTTON,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): Boolean {
        when (action.value) {
            GlobalActions.POWER_BUTTON -> context.service.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            else -> return false
        }
        return true
    }
}
