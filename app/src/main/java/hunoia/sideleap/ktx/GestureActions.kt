package hunoia.sideleap.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import hunoia.sideleap.App
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.gesture.GestureActions
import hunoia.sideleap.launcher.model.LauncherInfo
import hunoia.sideleap.utils.JsonHelper

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/18
 */

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

@Composable
fun GestureActions.actionTextCompose(): String {
    var text = ""
    val centerText = center.actionTextCompose(true)
    if (centerText.isNotEmpty()) {
        text += centerText
    }
    val upText = up.actionTextCompose(true)
    if (upText.isNotEmpty()) {
        text += if (text.isEmpty()) {
            upText
        } else {
            ",$upText"
        }
    }
    val downText = down.actionTextCompose(true)
    if (downText.isNotEmpty()) {
        text += if (text.isEmpty()) {
            downText
        } else {
            ",$downText"
        }
    }
    return text
}

@Composable
fun List<Action>.actionTextCompose(emptyIfNone: Boolean = false): String {
    if (size <= 1) {
        val value = firstOrNull() ?: Action.NONE
        return actionText(value, emptyIfNone)
    }
    return remember(this, emptyIfNone) {
        this
            .filter {
                it.value.isNotEmpty() && it.value != GlobalActions.NONE
            }
            .joinToString(separator = ",") {
                App.getContext().actionText(it, emptyIfNone)
            }
    }
}

