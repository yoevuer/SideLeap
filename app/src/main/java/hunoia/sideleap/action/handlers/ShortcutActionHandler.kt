package hunoia.sideleap.action.handlers

import hunoia.sideleap.action.ActionHandler
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.ktx.launchShortcutInfo
import hunoia.sideleap.ktx.shortcutInfo

object ShortcutActionHandler : ActionHandler {

    override val supportedActions = setOf(GlobalActions.EXTRA_LAUNCH_SHORTCUT)

    override suspend fun handle(action: Action, context: ActionHandlerContext): Boolean {
        when (action.value) {
            GlobalActions.EXTRA_LAUNCH_SHORTCUT -> {
                val shortcutInfo = action.shortcutInfo
                if (shortcutInfo != null) {
                    context.appContext.launchShortcutInfo(shortcutInfo)
                }
            }
            else -> return false
        }
        return true
    }
}
