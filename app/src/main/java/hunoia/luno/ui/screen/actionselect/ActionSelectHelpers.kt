package hunoia.luno.ui.screen.actionselect

import android.content.Context
import hunoia.luno.R
import hunoia.luno.action.api.ActionFacade
import hunoia.luno.action.payload.SubGestureActionData
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.SubGesture
import hunoia.luno.core.JsonHelper
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.ui.component.actionText

fun Context.selectedItemLabel(item: Any, subGestures: List<SubGesture>): String {
    return when (item) {
        is Action -> actionTextWithSubGesture(item, subGestures, emptyIfNone = false)
        is AppInfo -> item.label
        is LauncherInfo.ShortcutInfo -> item.label
        else -> ""
    }
}

fun Context.actionTextWithSubGesture(
    action: Action,
    subGestures: List<SubGesture>,
    emptyIfNone: Boolean
): String {
    if (action.value != ActionFacade.SUB_GESTURE) {
        return actionText(action, emptyIfNone)
    }
    val data = runCatching {
        JsonHelper.decodeFromString<SubGestureActionData>(action.data)
    }.getOrNull() ?: return getString(R.string.action_sub_gesture)
    return subGestures.firstOrNull { it.id == data.id }?.name ?: getString(R.string.action_sub_gesture)
}
