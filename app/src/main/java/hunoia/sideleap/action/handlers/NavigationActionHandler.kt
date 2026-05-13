package hunoia.sideleap.action.handlers

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS
import hunoia.sideleap.action.ActionHandler
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.entity.Action

object NavigationActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.BACK,
        GlobalActions.HOME,
        GlobalActions.RECENT,
        GlobalActions.OPEN_NOTIFICATION_PANEL,
        GlobalActions.OPEN_QUICK_PANEL,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): Boolean {
        when (action.value) {
            GlobalActions.BACK -> context.service.performGlobalAction(GLOBAL_ACTION_BACK)
            GlobalActions.HOME -> context.service.performGlobalAction(GLOBAL_ACTION_HOME)
            GlobalActions.RECENT -> context.service.performGlobalAction(GLOBAL_ACTION_RECENTS)
            GlobalActions.OPEN_NOTIFICATION_PANEL -> context.service.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            GlobalActions.OPEN_QUICK_PANEL -> context.service.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        }
        return true
    }
}
