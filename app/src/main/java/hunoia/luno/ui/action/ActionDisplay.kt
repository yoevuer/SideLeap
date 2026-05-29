package hunoia.luno.ui.action

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource

import hunoia.luno.core.AppContext
import hunoia.luno.R
import hunoia.luno.action.Action
import hunoia.luno.action.appInfo
import hunoia.luno.action.shortcutInfo
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.GestureActions
import hunoia.luno.action.definition.ActionCatalog
import hunoia.luno.quicklaunch.model.icon

fun Context.actionText(action: Action, emptyIfNone: Boolean = true): String = when (action.value) {
    GlobalActions.EXTRA_LAUNCH_APP -> action.appInfo?.label ?: ""
    GlobalActions.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.label ?: ""
    else -> {
        val def = ActionCatalog.byId(action.value)
        if (def != null) getString(def.titleResId) else if (emptyIfNone) "" else getString(R.string.action_none)
    }
}

@Composable
fun actionText(action: Action, emptyIfNone: Boolean = true): String = when (action.value) {
    GlobalActions.EXTRA_LAUNCH_APP -> action.appInfo?.label ?: ""
    GlobalActions.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.label ?: ""
    else -> {
        val def = ActionCatalog.byId(action.value)
        if (def != null) stringResource(def.titleResId) else if (emptyIfNone) "" else stringResource(R.string.action_none)
    }
}

@Composable
fun actionIcon(action: Action): Any? = when (action.value) {
    GlobalActions.EXTRA_LAUNCH_APP -> action.appInfo?.icon ?: Icons.Default.Android
    GlobalActions.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.icon ?: Icons.Default.Android
    else -> ActionCatalog.byId(action.value)?.icon
}

@Composable
fun GestureActions.actionTextCompose(): String =
    listOfNotNull(center, up, down)
        .map { it.actionTextCompose(true) }
        .filter { it.isNotEmpty() }
        .joinToString(separator = ",")

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
                AppContext.get().actionText(it, emptyIfNone)
            }
    }
}
