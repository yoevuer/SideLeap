package hunoia.sideleap.action

import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.model.LauncherInfo
import hunoia.sideleap.core.serialization.JsonHelper

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