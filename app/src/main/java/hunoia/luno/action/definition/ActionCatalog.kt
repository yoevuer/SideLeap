package hunoia.luno.action.definition

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Settings
import hunoia.luno.action.definition.PlayPause
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Window
import androidx.compose.ui.graphics.vector.ImageVector
import hunoia.luno.R
import hunoia.luno.action.Action
import hunoia.luno.action.GlobalActions

object ActionCatalog {

    val definitions: List<ActionDefinition> = listOf(
        ActionDefinition(GlobalActions.NONE, ActionCategory.NONE, ActionConfigKind.NONE,
            R.string.action_none, Icons.Default.Android),
        ActionDefinition(GlobalActions.BACK, ActionCategory.NAVIGATION, ActionConfigKind.NONE,
            R.string.action_back, Icons.AutoMirrored.Filled.ArrowBack),
        ActionDefinition(GlobalActions.HOME, ActionCategory.NAVIGATION, ActionConfigKind.NONE,
            R.string.action_home, Icons.Default.Home),
        ActionDefinition(GlobalActions.RECENT, ActionCategory.NAVIGATION, ActionConfigKind.NONE,
            R.string.action_recent, Icons.Default.ViewCarousel),
        ActionDefinition(GlobalActions.VOLUME_UP, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_volume_up, Icons.AutoMirrored.Filled.VolumeUp),
        ActionDefinition(GlobalActions.VOLUME_DOWN, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_volume_down, Icons.AutoMirrored.Filled.VolumeDown),
        ActionDefinition(GlobalActions.MUTE, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_mute, Icons.AutoMirrored.Filled.VolumeMute),
        ActionDefinition(GlobalActions.PLAY_PAUSE_SONG, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_play_pause_song, Icons.Default.PlayPause),
        ActionDefinition(GlobalActions.LAST_SONG, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_last_song, Icons.Default.SkipPrevious),
        ActionDefinition(GlobalActions.NEXT_SONG, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_next_song, Icons.Default.SkipNext),
        ActionDefinition(GlobalActions.PREVIOUS_APP, ActionCategory.NAVIGATION, ActionConfigKind.PREVIOUS_APP,
            R.string.action_previous_app, Icons.Default.SwapHoriz),
        ActionDefinition(GlobalActions.OPEN_NOTIFICATION_PANEL, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_open_notification_panel, Icons.Default.Notifications),
        ActionDefinition(GlobalActions.OPEN_QUICK_PANEL, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_open_quick_panel, Icons.Default.Dashboard),
        ActionDefinition(GlobalActions.LOCK_SCREEN, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_lock_screen, Icons.Default.ScreenLockPortrait),
        ActionDefinition(GlobalActions.FLASHLIGHT, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_flashlight, Icons.Default.FlashlightOn),
        ActionDefinition(GlobalActions.ASSIST_APP, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_assist_app, Icons.Default.Assistant),
        ActionDefinition(GlobalActions.SCREENSHOT, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_screenshot, Icons.Default.Screenshot),
        ActionDefinition(GlobalActions.SPLIT_SCREEN, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_split_screen, Icons.Default.Splitscreen),
        ActionDefinition(GlobalActions.POWER_BUTTON, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_power_button, Icons.Default.PowerSettingsNew),
        ActionDefinition(GlobalActions.KEEP_SCREEN_ON, ActionCategory.SYSTEM, ActionConfigKind.NONE,
            R.string.action_keep_screen_on, Icons.Default.BrightnessHigh),
        ActionDefinition(GlobalActions.POPUP_SCREEN, ActionCategory.NAVIGATION, ActionConfigKind.NONE,
            R.string.action_popup_screen, Icons.Default.Window),
        ActionDefinition(GlobalActions.MOVE_SCREEN, ActionCategory.NAVIGATION, ActionConfigKind.MOVE_SCREEN,
            R.string.action_move_screen, Icons.Default.OpenWith),
        ActionDefinition(GlobalActions.BACK_TO_TOP, ActionCategory.NAVIGATION, ActionConfigKind.NONE,
            R.string.action_back_to_top, Icons.Default.VerticalAlignTop),
        ActionDefinition(GlobalActions.GOTO_BOTTOM, ActionCategory.NAVIGATION, ActionConfigKind.GOTO_BOTTOM,
            R.string.action_goto_bottom, Icons.Default.VerticalAlignBottom),
        ActionDefinition(GlobalActions.CLICK_CURRENT_POSITION, ActionCategory.NAVIGATION, ActionConfigKind.NONE,
            R.string.action_click_current_position, Icons.Default.TouchApp),
        ActionDefinition(GlobalActions.VIRTUAL_MOUSE, ActionCategory.TOOL, ActionConfigKind.VIRTUAL_MOUSE,
            R.string.action_virtual_mouse, Icons.Default.Mouse),
        ActionDefinition(GlobalActions.OPEN_APP_ACTIVITY, ActionCategory.TOOL, ActionConfigKind.OPEN_APP_OR_URL,
            R.string.action_open_activity, Icons.Default.Settings),
        ActionDefinition(GlobalActions.OPEN_URL, ActionCategory.TOOL, ActionConfigKind.OPEN_APP_OR_URL,
            R.string.action_open_url, Icons.AutoMirrored.Filled.OpenInNew),
        ActionDefinition(GlobalActions.QUICK_APP_LAUNCHER, ActionCategory.TOOL, ActionConfigKind.NONE,
            R.string.action_quick_app_panel, Icons.Default.Apps),
        ActionDefinition(GlobalActions.RANDOM_NAME, ActionCategory.TOOL, ActionConfigKind.NONE,
            R.string.action_random_name, Icons.Default.AutoAwesome),
        ActionDefinition(GlobalActions.ONE_KEY_FREEZE_APPS, ActionCategory.TOOL, ActionConfigKind.NONE,
            R.string.action_one_key_freeze_apps, Icons.Default.AcUnit),
        ActionDefinition(GlobalActions.GENERATE_PASSWORD_COPY, ActionCategory.TOOL, ActionConfigKind.NONE,
            R.string.action_generate_password_copy, Icons.Default.ContentCopy),
        ActionDefinition(GlobalActions.OPEN_PASSWORD_GENERATOR, ActionCategory.TOOL, ActionConfigKind.NONE,
            R.string.action_open_password_generator, Icons.Default.Password),
        ActionDefinition(GlobalActions.HIDE_GESTURE_BUTTON, ActionCategory.SYSTEM, ActionConfigKind.HIDE_GESTURE_BUTTON,
            R.string.action_hide_gesture_button, Icons.Default.Gesture),
        ActionDefinition(GlobalActions.VOLUME_SCRUB, ActionCategory.SYSTEM, ActionConfigKind.VOLUME_SCRUB,
            R.string.action_volume_scrub, Icons.AutoMirrored.Filled.VolumeUp),
        ActionDefinition(GlobalActions.EXECUTE_SHELL_COMMAND, ActionCategory.TOOL, ActionConfigKind.SHELL_COMMAND,
            R.string.action_shell_command, Icons.Default.Terminal),
        ActionDefinition(GlobalActions.SUB_GESTURE, ActionCategory.SUB_GESTURE, ActionConfigKind.NONE,
            R.string.action_sub_gesture, Icons.Default.Gesture, isDisplayed = false),
    )

    private val byIdMap: Map<String, ActionDefinition> = definitions.associateBy { it.actionId }

    fun byId(actionId: String): ActionDefinition? = byIdMap[actionId]
    fun byAction(action: Action): ActionDefinition? = byIdMap[action.value]
    fun hasConfig(actionId: String): Boolean = byIdMap[actionId]?.configKind != ActionConfigKind.NONE
}
