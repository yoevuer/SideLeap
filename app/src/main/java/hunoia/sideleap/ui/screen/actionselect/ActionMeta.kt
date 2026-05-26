package hunoia.sideleap.ui.screen.actionselect

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
    "virtual_mouse" to R.string.action_virtual_mouse,
    "volume_scrub" to R.string.action_volume_scrub,
    "shell_command" to R.string.action_shell_command,
    "open_activity" to R.string.action_open_activity,
    "open_url" to R.string.action_open_url,
    "quick_app_launcher" to R.string.action_quick_app_panel,
    "random_name" to R.string.action_random_name,
    "one_key_freeze" to R.string.action_one_key_freeze_apps,
    "generate_password_copy" to R.string.action_generate_password_copy,
    "open_password_generator" to R.string.action_open_password_generator,
    "sub_gesture" to R.string.action_sub_gesture,
    "focus_input" to R.string.action_focus_input,
)

val actionDescResMap: Map<String, Int> = mapOf(
    "back" to R.string.action_desc_back,
    "home" to R.string.action_desc_home,
    "recent" to R.string.action_desc_recent,
    "open_notification" to R.string.action_desc_notification,
    "lock_screen" to R.string.action_desc_lock_screen,
    "flashlight" to R.string.action_desc_flashlight,
    "screenshot" to R.string.action_desc_screenshot,
    "open_activity" to R.string.action_desc_open_activity,
    "open_url" to R.string.action_desc_open_url,
    "quick_app_launcher" to R.string.action_desc_quick_launcher,
    "virtual_mouse" to R.string.action_desc_virtual_mouse,
    "volume_scrub" to R.string.action_desc_volume_scrub,
    "shell_command" to R.string.action_desc_shell_command,
    "sub_gesture" to R.string.action_desc_sub_gesture,
    "focus_input" to R.string.action_desc_focus_input,
)

val actionSettingHintResMap: Map<ActionConfigKind, Int> = mapOf(
    ActionConfigKind.PREVIOUS_APP to R.string.action_setting_hint_previous_app,
    ActionConfigKind.MOVE_SCREEN to R.string.action_setting_hint_move_screen,
    ActionConfigKind.GOTO_BOTTOM to R.string.action_setting_hint_goto_bottom,
    ActionConfigKind.OPEN_APP_OR_URL to R.string.action_setting_hint_open_activity,
    ActionConfigKind.SHELL_COMMAND to R.string.action_setting_hint_shell_command,
    ActionConfigKind.VIRTUAL_MOUSE to R.string.action_setting_hint_virtual_mouse,
    ActionConfigKind.VOLUME_SCRUB to R.string.action_setting_hint_volume_scrub,
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
    ActionCategory.SUB_GESTURE -> "子手势"
    ActionCategory.TOOL -> "工具"
}
