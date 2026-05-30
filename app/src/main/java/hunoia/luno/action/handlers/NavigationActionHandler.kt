package hunoia.luno.action.handlers

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS
import hunoia.luno.action.api.ActionExecutionResult
import hunoia.luno.action.api.ActionHandler
import hunoia.luno.action.api.ActionHandlerContext
import hunoia.luno.action.GlobalActions
import hunoia.luno.config.model.Action

object NavigationActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.BACK,
        GlobalActions.HOME,
        GlobalActions.RECENT,
        GlobalActions.OPEN_NOTIFICATION_PANEL,
        GlobalActions.OPEN_QUICK_PANEL,
        GlobalActions.PREVIOUS_APP,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.BACK -> context.accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK)
            GlobalActions.HOME -> context.accessibilityService.performGlobalAction(GLOBAL_ACTION_HOME)
            GlobalActions.RECENT -> context.accessibilityService.performGlobalAction(GLOBAL_ACTION_RECENTS)
            GlobalActions.OPEN_NOTIFICATION_PANEL -> context.accessibilityService.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            GlobalActions.OPEN_QUICK_PANEL -> context.accessibilityService.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            GlobalActions.PREVIOUS_APP -> {
                context.previousApp()
                return ActionExecutionResult.Success
            }
        }
        return ActionExecutionResult.Success
    }
}
