package hunoia.sideleap.action.display

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import hunoia.sideleap.App
import hunoia.sideleap.R
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.appInfo
import hunoia.sideleap.action.shortcutInfo
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.GestureActions
import hunoia.sideleap.launcher.ext.icon
import hunoia.sideleap.action.display.PlayPause

private val actionTextResMap: Map<String, Int> = mapOf(
    GlobalActions.BACK to R.string.action_back,
    GlobalActions.HOME to R.string.action_home,
    GlobalActions.RECENT to R.string.action_recent,
    GlobalActions.MENU to R.string.action_menu,
    GlobalActions.SEARCH_IN_APP to R.string.action_search_in_app,
    GlobalActions.VOLUME_UP to R.string.action_volume_up,
    GlobalActions.VOLUME_DOWN to R.string.action_volume_down,
    GlobalActions.MUTE to R.string.action_mute,
    GlobalActions.PLAY_PAUSE_SONG to R.string.action_play_pause_song,
    GlobalActions.LAST_SONG to R.string.action_last_song,
    GlobalActions.NEXT_SONG to R.string.action_next_song,
    GlobalActions.PREVIOUS_APP to R.string.action_previous_app,
    GlobalActions.APP_SCREEN to R.string.action_app_screen,
    GlobalActions.WEB_FORWARD to R.string.action_web_forward,
    GlobalActions.OPEN_NOTIFICATION_PANEL to R.string.action_open_notification_panel,
    GlobalActions.OPEN_QUICK_PANEL to R.string.action_open_quick_panel,
    GlobalActions.LOCK_SCREEN to R.string.action_lock_screen,
    GlobalActions.KILL_APP to R.string.action_kill_app,
    GlobalActions.FLASHLIGHT to R.string.action_flashlight,
    GlobalActions.SPLIT_SCREEN to R.string.action_split_screen,
    GlobalActions.POPUP_SCREEN to R.string.action_popup_screen,
    GlobalActions.ASSIST_APP to R.string.action_assist_app,
    GlobalActions.SEARCH to R.string.action_search,
    GlobalActions.SCREENSHOT to R.string.action_screenshot,
    GlobalActions.RECORD_SCREEN to R.string.action_record_screen,
    GlobalActions.SHOW_HIDE_NAV_BAR to R.string.action_show_hide_nav_bar,
    GlobalActions.PULL_SCREEN_DOWN to R.string.action_pull_screen_down,
    GlobalActions.EASY_ONE_HAND to R.string.action_easy_one_hand,
    GlobalActions.POWER_BUTTON to R.string.action_power_button,
    GlobalActions.AUTO_ROTATE to R.string.action_auto_rotate,
    GlobalActions.ROTATE_RIGHT to R.string.action_rotate_right,
    GlobalActions.ROTATE_LEFT to R.string.action_rotate_left,
    GlobalActions.INVERSE_COLOR to R.string.action_inverse_color,
    GlobalActions.QUICK_APP_PANEL to R.string.action_quick_app_panel,
    GlobalActions.LAUNCH_APP to R.string.action_launch_app,
    GlobalActions.LAUNCH_APP_IN_POPUP to R.string.action_launch_app_in_popup,
    GlobalActions.TASK_SWITCHER to R.string.action_task_switcher,
    GlobalActions.QUICK_TOOLS to R.string.action_quick_tools,
    GlobalActions.FLOAT_BALL to R.string.action_float_ball,
    GlobalActions.HIDE_GESTURE_BUTTON to R.string.action_hide_gesture_button,
    GlobalActions.MOVE_SCREEN to R.string.action_move_screen,
    GlobalActions.KEEP_SCREEN_ON to R.string.action_keep_screen_on,
    GlobalActions.BACK_TO_TOP to R.string.action_back_to_top,
    GlobalActions.GOTO_BOTTOM to R.string.action_goto_bottom,
    GlobalActions.CLICK_CURRENT_POSITION to R.string.action_click_current_position,
    GlobalActions.VIRTUAL_MOUSE to R.string.action_virtual_mouse,
    GlobalActions.OPEN_APP_OR_URL to R.string.action_open_app_or_url,
    GlobalActions.QUICK_APP_LAUNCHER to R.string.action_quick_app_panel,
    GlobalActions.RANDOM_NAME to R.string.action_random_name,
    GlobalActions.ONE_KEY_FREEZE_APPS to R.string.action_one_key_freeze_apps,
    GlobalActions.GENERATE_PASSWORD_COPY to R.string.action_generate_password_copy,
    GlobalActions.OPEN_PASSWORD_GENERATOR to R.string.action_open_password_generator
)

private val actionIconMap: Map<String, Any> = mapOf(
    GlobalActions.BACK to Icons.Default.ArrowBack,
    GlobalActions.HOME to Icons.Default.Home,
    GlobalActions.RECENT to Icons.Default.ViewCarousel,
    GlobalActions.MENU to Icons.Default.Menu,
    GlobalActions.SEARCH_IN_APP to Icons.Default.Search,
    GlobalActions.VOLUME_UP to Icons.Default.VolumeUp,
    GlobalActions.VOLUME_DOWN to Icons.Default.VolumeDown,
    GlobalActions.MUTE to Icons.Default.VolumeMute,
    GlobalActions.PLAY_PAUSE_SONG to Icons.Default.PlayPause,
    GlobalActions.LAST_SONG to Icons.Default.SkipPrevious,
    GlobalActions.NEXT_SONG to Icons.Default.SkipNext,
    GlobalActions.PREVIOUS_APP to Icons.Default.Android,
    GlobalActions.WEB_FORWARD to Icons.Default.Forward,
    GlobalActions.OPEN_NOTIFICATION_PANEL to Icons.Default.Notifications,
    GlobalActions.OPEN_QUICK_PANEL to Icons.Default.Settings,
    GlobalActions.LOCK_SCREEN to Icons.Default.ScreenLockPortrait,
    GlobalActions.KILL_APP to Icons.Default.Close,
    GlobalActions.FLASHLIGHT to Icons.Default.FlashlightOn,
    GlobalActions.SPLIT_SCREEN to Icons.Default.Splitscreen,
    GlobalActions.POPUP_SCREEN to Icons.Default.BrandingWatermark,
    GlobalActions.ASSIST_APP to Icons.Default.Assistant,
    GlobalActions.SEARCH to Icons.Default.Search,
    GlobalActions.SCREENSHOT to Icons.Default.Screenshot,
    GlobalActions.RECORD_SCREEN to Icons.Default.Screenshot,
    GlobalActions.SHOW_HIDE_NAV_BAR to Icons.Default.BarChart,
    GlobalActions.PULL_SCREEN_DOWN to Icons.Default.ArrowCircleDown,
    GlobalActions.EASY_ONE_HAND to Icons.Default.BackHand,
    GlobalActions.POWER_BUTTON to Icons.Default.PowerSettingsNew,
    GlobalActions.AUTO_ROTATE to Icons.Default.ScreenRotation,
    GlobalActions.ROTATE_RIGHT to Icons.Default.RotateRight,
    GlobalActions.ROTATE_LEFT to Icons.Default.RotateLeft,
    GlobalActions.INVERSE_COLOR to Icons.Default.InvertColors,
    GlobalActions.QUICK_APP_PANEL to Icons.Default.Apps,
    GlobalActions.LAUNCH_APP to Icons.Default.Android,
    GlobalActions.LAUNCH_APP_IN_POPUP to Icons.Default.Window,
    GlobalActions.TASK_SWITCHER to Icons.Default.ViewCarousel,
    GlobalActions.QUICK_TOOLS to Icons.Default.Handyman,
    GlobalActions.FLOAT_BALL to Icons.Default.SportsBaseball,
    GlobalActions.HIDE_GESTURE_BUTTON to Icons.Default.Gesture,
    GlobalActions.MOVE_SCREEN to Icons.Default.AddToHomeScreen,
    GlobalActions.KEEP_SCREEN_ON to Icons.Default.WbSunny,
    GlobalActions.BACK_TO_TOP to Icons.Default.VerticalAlignTop,
    GlobalActions.GOTO_BOTTOM to Icons.Default.VerticalAlignBottom,
    GlobalActions.CLICK_CURRENT_POSITION to Icons.Default.Gesture,
    GlobalActions.VIRTUAL_MOUSE to Icons.Default.Gesture,
    GlobalActions.OPEN_APP_OR_URL to Icons.Default.Forward,
    GlobalActions.QUICK_APP_LAUNCHER to Icons.Default.Apps,
    GlobalActions.RANDOM_NAME to Icons.Default.Assistant,
    GlobalActions.ONE_KEY_FREEZE_APPS to Icons.Default.FlashlightOn,
    GlobalActions.GENERATE_PASSWORD_COPY to Icons.Default.Password,
    GlobalActions.OPEN_PASSWORD_GENERATOR to Icons.Default.Password
)

fun Context.actionText(action: Action, emptyIfNone: Boolean = true): String = when (action.value) {
    GlobalActions.EXTRA_LAUNCH_APP -> action.appInfo?.label ?: ""
    GlobalActions.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.label ?: ""
    else -> {
        val resId = actionTextResMap[action.value]
        if (resId != null) getString(resId) else if (emptyIfNone) "" else getString(R.string.action_none)
    }
}

@Composable
fun actionText(action: Action, emptyIfNone: Boolean = true): String = when (action.value) {
    GlobalActions.EXTRA_LAUNCH_APP -> action.appInfo?.label ?: ""
    GlobalActions.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.label ?: ""
    else -> {
        val resId = actionTextResMap[action.value]
        if (resId != null) stringResource(resId) else if (emptyIfNone) "" else stringResource(R.string.action_none)
    }
}

@Composable
fun actionIcon(action: Action): Any? = when (action.value) {
    GlobalActions.EXTRA_LAUNCH_APP -> action.appInfo?.icon ?: Icons.Default.Android
    GlobalActions.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.icon ?: Icons.Default.Android
    else -> actionIconMap[action.value]
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
