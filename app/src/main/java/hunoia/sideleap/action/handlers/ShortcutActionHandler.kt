package hunoia.sideleap.action.handlers

import hunoia.sideleap.action.ActionExecutionResult
import hunoia.sideleap.action.ActionHandler
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.launcher.launch.Launcher
import hunoia.sideleap.action.shortcutInfo

object ShortcutActionHandler : ActionHandler {

    override val supportedActions = setOf(GlobalActions.EXTRA_LAUNCH_SHORTCUT)

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.EXTRA_LAUNCH_SHORTCUT -> {
                val shortcutInfo = action.shortcutInfo
                if (shortcutInfo != null) {
                    Launcher.launchShortcutInfo(context.appContext, shortcutInfo)
                }
            }
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }
}
