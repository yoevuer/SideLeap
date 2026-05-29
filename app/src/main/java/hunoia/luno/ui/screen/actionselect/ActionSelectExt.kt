package hunoia.luno.ui.screen.actionselect

import hunoia.luno.action.api.ActionFacade
import hunoia.luno.config.model.Action
import hunoia.luno.core.JsonHelper
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo

internal fun Any.toAction(): Action {
    return when (this) {
        is Action -> this.copy(extra = null)
        is AppInfo -> Action(
            value = ActionFacade.EXTRA_LAUNCH_APP,
            data = JsonHelper.encodeToString(this)
        )
        is LauncherInfo.ShortcutInfo -> Action(
            value = ActionFacade.EXTRA_LAUNCH_SHORTCUT,
            data = JsonHelper.encodeToString(this)
        )
        else -> error("Unsupported selected action type: ${this::class.java.name}")
    }
}

internal fun Action.sameAction(other: Action): Boolean {
    return value == other.value && data == other.data
}
