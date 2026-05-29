package hunoia.luno.action.api

import hunoia.luno.config.model.Action
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.core.JsonHelper

val Action.appInfo: AppInfo? get() {
    if (value == ActionFacade.EXTRA_LAUNCH_APP) {
        return JsonHelper.decodeFromString<AppInfo>(data)
    }
    return null
}

val Action.shortcutInfo: LauncherInfo.ShortcutInfo? get() {
    if (value == ActionFacade.EXTRA_LAUNCH_SHORTCUT) {
        return JsonHelper.decodeFromString<LauncherInfo.ShortcutInfo>(data)
    }
    return null
}
