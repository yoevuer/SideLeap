package hunoia.luno.ui.component

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource

import hunoia.luno.core.AppContext
import hunoia.luno.R
import hunoia.luno.config.model.Action
import hunoia.luno.action.api.appInfo
import hunoia.luno.action.api.shortcutInfo
import hunoia.luno.action.api.ActionFacade
import hunoia.luno.config.model.GestureActions
import hunoia.luno.quicklaunch.model.icon

fun Context.actionText(action: Action, emptyIfNone: Boolean = true): String = when (action.value) {
    ActionFacade.EXTRA_LAUNCH_APP -> action.appInfo?.label ?: ""
    ActionFacade.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.label ?: ""
    else -> {
        val def = ActionFacade.byId(action.value)
        if (def != null) getString(def.titleResId) else if (emptyIfNone) "" else getString(R.string.action_none)
    }
}

@Composable
fun actionText(action: Action, emptyIfNone: Boolean = true): String = when (action.value) {
    ActionFacade.EXTRA_LAUNCH_APP -> action.appInfo?.label ?: ""
    ActionFacade.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.label ?: ""
    else -> {
        val def = ActionFacade.byId(action.value)
        if (def != null) stringResource(def.titleResId) else if (emptyIfNone) "" else stringResource(R.string.action_none)
    }
}

@Composable
fun actionIcon(action: Action): Any? = when (action.value) {
    ActionFacade.EXTRA_LAUNCH_APP -> action.appInfo?.icon ?: Icons.Default.Android
    ActionFacade.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.icon ?: Icons.Default.Android
    else -> ActionFacade.byId(action.value)?.icon
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
                it.value.isNotEmpty() && it.value != ActionFacade.NONE
            }
            .joinToString(separator = ",") {
                AppContext.get().actionText(it, emptyIfNone)
            }
    }
}
