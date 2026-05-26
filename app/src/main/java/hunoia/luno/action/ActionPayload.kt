package hunoia.luno.action

import hunoia.luno.action.GlobalActions
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.launcher.model.LauncherInfo
import hunoia.luno.core.serialization.JsonHelper

val Action.appInfo: AppInfo? get() {
    if (value == GlobalActions.EXTRA_LAUNCH_APP) {
        return JsonHelper.decodeFromString<AppInfo>(data)
    }
    return null
}

val Action.shortcutInfo: LauncherInfo.ShortcutInfo? get() {
    if (value == GlobalActions.EXTRA_LAUNCH_SHORTCUT) {
        return JsonHelper.decodeFromString<LauncherInfo.ShortcutInfo>(data)
    }
    return null
}