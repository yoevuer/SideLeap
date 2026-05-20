package hunoia.sideleap.action.definition

import hunoia.sideleap.action.Action
import hunoia.sideleap.action.GlobalActions

object ActionCatalog {

    val definitions: List<ActionDefinition> = listOf(
        ActionDefinition(GlobalActions.NONE, ActionCategory.NONE, ActionConfigKind.NONE, "none", "none"),
        ActionDefinition(GlobalActions.BACK, ActionCategory.NAVIGATION, ActionConfigKind.NONE, "back", "back"),
        ActionDefinition(GlobalActions.HOME, ActionCategory.NAVIGATION, ActionConfigKind.NONE, "home", "home"),
        ActionDefinition(GlobalActions.RECENT, ActionCategory.NAVIGATION, ActionConfigKind.NONE, "recent", "recent"),
        ActionDefinition(GlobalActions.VOLUME_UP, ActionCategory.SYSTEM, ActionConfigKind.NONE, "volume_up", "volume_up"),
        ActionDefinition(GlobalActions.VOLUME_DOWN, ActionCategory.SYSTEM, ActionConfigKind.NONE, "volume_down", "volume_down"),
        ActionDefinition(GlobalActions.MUTE, ActionCategory.SYSTEM, ActionConfigKind.NONE, "mute", "mute"),
        ActionDefinition(GlobalActions.PLAY_PAUSE_SONG, ActionCategory.SYSTEM, ActionConfigKind.NONE, "play_pause", "play_pause"),
        ActionDefinition(GlobalActions.LAST_SONG, ActionCategory.SYSTEM, ActionConfigKind.NONE, "last_song", "last_song"),
        ActionDefinition(GlobalActions.NEXT_SONG, ActionCategory.SYSTEM, ActionConfigKind.NONE, "next_song", "next_song"),
        ActionDefinition(GlobalActions.PREVIOUS_APP, ActionCategory.NAVIGATION, ActionConfigKind.PREVIOUS_APP, "previous_app", "previous_app"),
        ActionDefinition(GlobalActions.OPEN_NOTIFICATION_PANEL, ActionCategory.SYSTEM, ActionConfigKind.NONE, "open_notification", "open_notification"),
        ActionDefinition(GlobalActions.OPEN_QUICK_PANEL, ActionCategory.SYSTEM, ActionConfigKind.NONE, "open_quick_settings", "open_quick_settings"),
        ActionDefinition(GlobalActions.LOCK_SCREEN, ActionCategory.SYSTEM, ActionConfigKind.NONE, "lock_screen", "lock_screen"),
        ActionDefinition(GlobalActions.FLASHLIGHT, ActionCategory.SYSTEM, ActionConfigKind.NONE, "flashlight", "flashlight"),
        ActionDefinition(GlobalActions.ASSIST_APP, ActionCategory.SYSTEM, ActionConfigKind.NONE, "assist_app", "assist_app"),
        ActionDefinition(GlobalActions.SCREENSHOT, ActionCategory.SYSTEM, ActionConfigKind.NONE, "screenshot", "screenshot"),
        ActionDefinition(GlobalActions.POWER_BUTTON, ActionCategory.SYSTEM, ActionConfigKind.NONE, "power_button", "power_button"),
        ActionDefinition(GlobalActions.KEEP_SCREEN_ON, ActionCategory.SYSTEM, ActionConfigKind.NONE, "keep_screen_on", "keep_screen_on"),
        ActionDefinition(GlobalActions.POPUP_SCREEN, ActionCategory.NAVIGATION, ActionConfigKind.NONE, "popup_screen", "popup_screen"),
        ActionDefinition(GlobalActions.MOVE_SCREEN, ActionCategory.NAVIGATION, ActionConfigKind.MOVE_SCREEN, "move_screen", "move_screen"),
        ActionDefinition(GlobalActions.BACK_TO_TOP, ActionCategory.NAVIGATION, ActionConfigKind.NONE, "back_to_top", "back_to_top"),
        ActionDefinition(GlobalActions.GOTO_BOTTOM, ActionCategory.NAVIGATION, ActionConfigKind.GOTO_BOTTOM, "goto_bottom", "goto_bottom"),
        ActionDefinition(GlobalActions.CLICK_CURRENT_POSITION, ActionCategory.NAVIGATION, ActionConfigKind.NONE, "click_current_position", "click_current_position"),
        ActionDefinition(GlobalActions.VIRTUAL_MOUSE, ActionCategory.TOOL, ActionConfigKind.VIRTUAL_MOUSE, "virtual_mouse", "virtual_mouse"),
        ActionDefinition(GlobalActions.OPEN_APP_OR_URL, ActionCategory.TOOL, ActionConfigKind.OPEN_APP_OR_URL, "open_app_or_url", "open_app_or_url"),
        ActionDefinition(GlobalActions.QUICK_APP_LAUNCHER, ActionCategory.TOOL, ActionConfigKind.NONE, "quick_app_launcher", "quick_app_launcher"),
        ActionDefinition(GlobalActions.RANDOM_NAME, ActionCategory.TOOL, ActionConfigKind.NONE, "random_name", "random_name"),
        ActionDefinition(GlobalActions.ONE_KEY_FREEZE_APPS, ActionCategory.TOOL, ActionConfigKind.NONE, "one_key_freeze", "one_key_freeze"),
        ActionDefinition(GlobalActions.GENERATE_PASSWORD_COPY, ActionCategory.TOOL, ActionConfigKind.NONE, "generate_password_copy", "generate_password_copy"),
        ActionDefinition(GlobalActions.OPEN_PASSWORD_GENERATOR, ActionCategory.TOOL, ActionConfigKind.NONE, "open_password_generator", "open_password_generator"),
        ActionDefinition(GlobalActions.HIDE_GESTURE_BUTTON, ActionCategory.SYSTEM, ActionConfigKind.HIDE_GESTURE_BUTTON, "hide_gesture_button", "hide_gesture_button"),
        ActionDefinition(GlobalActions.VOLUME_SCRUB, ActionCategory.SYSTEM, ActionConfigKind.NONE, "volume_scrub", "volume_scrub"),
        ActionDefinition(GlobalActions.EXECUTE_SHELL_COMMAND, ActionCategory.TOOL, ActionConfigKind.SHELL_COMMAND, "shell_command", "shell_command"),
        ActionDefinition(GlobalActions.SUB_GESTURE, ActionCategory.SUB_GESTURE, ActionConfigKind.NONE, "sub_gesture", "sub_gesture", isDisplayed = false),
    )

    private val byIdMap: Map<String, ActionDefinition> = definitions.associateBy { it.actionId }

    fun byId(actionId: String): ActionDefinition? = byIdMap[actionId]
    fun byAction(action: Action): ActionDefinition? = byIdMap[action.value]
    fun hasConfig(actionId: String): Boolean = byIdMap[actionId]?.configKind != ActionConfigKind.NONE
}
