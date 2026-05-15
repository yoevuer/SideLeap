package hunoia.sideleap.ui.screen.actionselect

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Window
import hunoia.sideleap.action.display.PlayPause
import hunoia.sideleap.R
import hunoia.sideleap.action.definition.ActionCategory
import hunoia.sideleap.action.definition.ActionConfigKind

val actionTitleResMap: Map<String, Int> = mapOf(
    "none" to R.string.action_none,
    "back" to R.string.action_back,
    "home" to R.string.action_home,
    "recent" to R.string.action_recent,
    "volume_up" to R.string.action_volume_up,
    "volume_down" to R.string.action_volume_down,
    "mute" to R.string.action_mute,
    "play_pause" to R.string.action_play_pause_song,
    "last_song" to R.string.action_last_song,
    "next_song" to R.string.action_next_song,
    "previous_app" to R.string.action_previous_app,
    "open_notification" to R.string.action_open_notification_panel,
    "open_quick_settings" to R.string.action_open_quick_panel,
    "lock_screen" to R.string.action_lock_screen,
    "flashlight" to R.string.action_flashlight,
    "assist_app" to R.string.action_assist_app,
    "screenshot" to R.string.action_screenshot,
    "power_button" to R.string.action_power_button,
    "keep_screen_on" to R.string.action_keep_screen_on,
    "popup_screen" to R.string.action_popup_screen,
    "move_screen" to R.string.action_move_screen,
    "back_to_top" to R.string.action_back_to_top,
    "goto_bottom" to R.string.action_goto_bottom,
    "open_app_or_url" to R.string.action_open_app_or_url,
    "quick_app_launcher" to R.string.action_quick_app_panel,
    "random_name" to R.string.action_random_name,
    "one_key_freeze" to R.string.action_one_key_freeze_apps,
)

val actionIconMap: Map<String, Any> = mapOf(
    "none" to Icons.Default.Android,
    "back" to Icons.Default.ArrowBack,
    "home" to Icons.Default.Home,
    "recent" to Icons.Default.ViewCarousel,
    "volume_up" to Icons.Default.VolumeUp,
    "volume_down" to Icons.Default.VolumeDown,
    "mute" to Icons.Default.VolumeMute,
    "play_pause" to Icons.Default.PlayPause,
    "last_song" to Icons.Default.SkipPrevious,
    "next_song" to Icons.Default.SkipNext,
    "previous_app" to Icons.Default.SwapHoriz,
    "open_notification" to Icons.Default.Notifications,
    "open_quick_settings" to Icons.Default.Dashboard,
    "lock_screen" to Icons.Default.ScreenLockPortrait,
    "flashlight" to Icons.Default.FlashlightOn,
    "assist_app" to Icons.Default.Assistant,
    "screenshot" to Icons.Default.Screenshot,
    "power_button" to Icons.Default.PowerSettingsNew,
    "keep_screen_on" to Icons.Default.BrightnessHigh,
    "popup_screen" to Icons.Default.Window,
    "move_screen" to Icons.Default.OpenWith,
    "back_to_top" to Icons.Default.VerticalAlignTop,
    "goto_bottom" to Icons.Default.VerticalAlignBottom,
    "open_app_or_url" to Icons.Default.OpenInNew,
    "quick_app_launcher" to Icons.Default.Apps,
    "random_name" to Icons.Default.Assistant,
    "one_key_freeze" to Icons.Default.AcUnit,
)

val actionDescResMap: Map<String, Int> = mapOf(
    "back" to R.string.action_desc_back,
    "home" to R.string.action_desc_home,
    "recent" to R.string.action_desc_recent,
    "open_notification" to R.string.action_desc_notification,
    "lock_screen" to R.string.action_desc_lock_screen,
    "flashlight" to R.string.action_desc_flashlight,
    "screenshot" to R.string.action_desc_screenshot,
    "open_app_or_url" to R.string.action_desc_open_app,
    "quick_app_launcher" to R.string.action_desc_quick_launcher,
)

val actionSettingHintResMap: Map<ActionConfigKind, Int> = mapOf(
    ActionConfigKind.PREVIOUS_APP to R.string.action_setting_hint_previous_app,
    ActionConfigKind.MOVE_SCREEN to R.string.action_setting_hint_move_screen,
    ActionConfigKind.GOTO_BOTTOM to R.string.action_setting_hint_goto_bottom,
    ActionConfigKind.OPEN_APP_OR_URL to R.string.action_setting_hint_open_app_or_url,
)

val actionPermissionHintResMap: Map<String, Int> = mapOf(
    "flashlight" to R.string.action_permission_hint_flashlight,
    "screenshot" to R.string.action_permission_hint_screenshot,
    "power_button" to R.string.action_permission_hint_power_button,
)

val ActionCategory.displayName: String get() = when (this) {
    ActionCategory.NONE -> ""
    ActionCategory.NAVIGATION -> "导航"
    ActionCategory.MEDIA -> "媒体"
    ActionCategory.SYSTEM -> "系统"
    ActionCategory.WINDOW -> "窗口"
    ActionCategory.LAUNCHER -> "启动"
    ActionCategory.TOOL -> "工具"
}
