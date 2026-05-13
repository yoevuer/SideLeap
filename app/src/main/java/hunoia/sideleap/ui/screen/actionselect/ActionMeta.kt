package hunoia.sideleap.ui.screen.actionselect

import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.AcUnit
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
import hunoia.sideleap.ui.theme.icons.PlayPause
import hunoia.sideleap.R
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.entity.Action

enum class ActionCategory {
    NONE,
    NAVIGATION,
    MEDIA,
    SYSTEM,
    WINDOW,

    LAUNCHER,
    TOOL
}

enum class ActionTag(val displayName: String) {
    Configurable("可配置"),
    RequiresPermission("需权限"),
    Launch("启动"),
    System("系统"),

    Media("媒体")
}

data class ActionMeta(
    val action: Action,
    val labelRes: Int,
    val category: ActionCategory,
    val icon: Any? = null,
    val hasSettings: Boolean = false,
    val descRes: Int? = null,
    val tags: List<ActionTag> = emptyList(),
    val highlighted: Boolean = false,
    val permissionHintRes: Int? = null,
    val settingHintRes: Int? = null
)

val actionMetaList = listOf(
    ActionMeta(Action(GlobalActions.NONE), R.string.action_none, ActionCategory.NONE,
        icon = Icons.Default.Android),
    ActionMeta(Action(GlobalActions.BACK), R.string.action_back, ActionCategory.NAVIGATION,
        icon = Icons.Default.ArrowBack,
        descRes = R.string.action_desc_back, highlighted = true),
    ActionMeta(Action(GlobalActions.HOME), R.string.action_home, ActionCategory.NAVIGATION,
        icon = Icons.Default.Home,
        descRes = R.string.action_desc_home, highlighted = true),
    ActionMeta(Action(GlobalActions.RECENT), R.string.action_recent, ActionCategory.NAVIGATION,
        icon = Icons.Default.ViewCarousel,
        descRes = R.string.action_desc_recent, highlighted = true),
    ActionMeta(Action(GlobalActions.VOLUME_UP), R.string.action_volume_up, ActionCategory.MEDIA,
        icon = Icons.Default.VolumeUp,
        tags = listOf(ActionTag.Media)),
    ActionMeta(Action(GlobalActions.VOLUME_DOWN), R.string.action_volume_down, ActionCategory.MEDIA,
        icon = Icons.Default.VolumeDown,
        tags = listOf(ActionTag.Media)),
    ActionMeta(Action(GlobalActions.MUTE), R.string.action_mute, ActionCategory.MEDIA,
        icon = Icons.Default.VolumeMute,
        tags = listOf(ActionTag.Media)),
    ActionMeta(Action(GlobalActions.PLAY_PAUSE_SONG), R.string.action_play_pause_song, ActionCategory.MEDIA,
        icon = Icons.Default.PlayPause,
        tags = listOf(ActionTag.Media)),
    ActionMeta(Action(GlobalActions.LAST_SONG), R.string.action_last_song, ActionCategory.MEDIA,
        icon = Icons.Default.SkipPrevious,
        tags = listOf(ActionTag.Media)),
    ActionMeta(Action(GlobalActions.NEXT_SONG), R.string.action_next_song, ActionCategory.MEDIA,
        icon = Icons.Default.SkipNext,
        tags = listOf(ActionTag.Media)),
    ActionMeta(Action(GlobalActions.PREVIOUS_APP), R.string.action_previous_app, ActionCategory.NAVIGATION,
        icon = Icons.Default.SwapHoriz,
        hasSettings = true, tags = listOf(ActionTag.Configurable, ActionTag.System),
        settingHintRes = R.string.action_setting_hint_previous_app),
    ActionMeta(Action(GlobalActions.OPEN_NOTIFICATION_PANEL), R.string.action_open_notification_panel, ActionCategory.SYSTEM,
        icon = Icons.Default.Notifications,
        descRes = R.string.action_desc_notification, tags = listOf(ActionTag.System), highlighted = true),
    ActionMeta(Action(GlobalActions.OPEN_QUICK_PANEL), R.string.action_open_quick_panel, ActionCategory.SYSTEM,
        icon = Icons.Default.Dashboard,
        tags = listOf(ActionTag.System)),
    ActionMeta(Action(GlobalActions.LOCK_SCREEN), R.string.action_lock_screen, ActionCategory.SYSTEM,
        icon = Icons.Default.ScreenLockPortrait,
        descRes = R.string.action_desc_lock_screen, tags = listOf(ActionTag.System), highlighted = true),
    ActionMeta(Action(GlobalActions.FLASHLIGHT), R.string.action_flashlight, ActionCategory.SYSTEM,
        icon = Icons.Default.FlashlightOn,
        descRes = R.string.action_desc_flashlight, tags = listOf(ActionTag.System, ActionTag.RequiresPermission),
        permissionHintRes = R.string.action_permission_hint_flashlight),
    ActionMeta(Action(GlobalActions.ASSIST_APP), R.string.action_assist_app, ActionCategory.SYSTEM,
        icon = Icons.Default.Assistant,
        tags = listOf(ActionTag.System)),
    ActionMeta(Action(GlobalActions.SCREENSHOT), R.string.action_screenshot, ActionCategory.SYSTEM,
        icon = Icons.Default.Screenshot,
        descRes = R.string.action_desc_screenshot, tags = listOf(ActionTag.System, ActionTag.RequiresPermission), highlighted = true,
        permissionHintRes = R.string.action_permission_hint_screenshot),
    ActionMeta(Action(GlobalActions.POWER_BUTTON), R.string.action_power_button, ActionCategory.SYSTEM,
        icon = Icons.Default.PowerSettingsNew,
        tags = listOf(ActionTag.System, ActionTag.RequiresPermission),
        permissionHintRes = R.string.action_permission_hint_power_button),
    ActionMeta(Action(GlobalActions.KEEP_SCREEN_ON), R.string.action_keep_screen_on, ActionCategory.SYSTEM,
        icon = Icons.Default.BrightnessHigh,
        tags = listOf(ActionTag.System)),
    ActionMeta(Action(GlobalActions.POPUP_SCREEN), R.string.action_popup_screen, ActionCategory.WINDOW,
        icon = Icons.Default.Window),
    ActionMeta(Action(GlobalActions.MOVE_SCREEN), R.string.action_move_screen, ActionCategory.WINDOW,
        icon = Icons.Default.OpenWith,
        hasSettings = true, tags = listOf(ActionTag.Configurable),
        settingHintRes = R.string.action_setting_hint_move_screen),
    ActionMeta(Action(GlobalActions.BACK_TO_TOP), R.string.action_back_to_top, ActionCategory.WINDOW,
        icon = Icons.Default.VerticalAlignTop),
    ActionMeta(Action(GlobalActions.GOTO_BOTTOM), R.string.action_goto_bottom, ActionCategory.WINDOW,
        icon = Icons.Default.VerticalAlignBottom,
        hasSettings = true, tags = listOf(ActionTag.Configurable),
        settingHintRes = R.string.action_setting_hint_goto_bottom),
    ActionMeta(Action(GlobalActions.OPEN_APP_OR_URL), R.string.action_open_app_or_url, ActionCategory.LAUNCHER,
        icon = Icons.Default.OpenInNew,
        hasSettings = true, descRes = R.string.action_desc_open_app, tags = listOf(ActionTag.Configurable, ActionTag.Launch),
        settingHintRes = R.string.action_setting_hint_open_app_or_url),
    ActionMeta(Action(GlobalActions.QUICK_APP_LAUNCHER), R.string.action_quick_app_panel, ActionCategory.LAUNCHER,
        icon = Icons.Default.Apps,
        descRes = R.string.action_desc_quick_launcher, tags = listOf(ActionTag.Launch), highlighted = true),
    ActionMeta(Action(GlobalActions.RANDOM_NAME), R.string.action_random_name, ActionCategory.TOOL,
        icon = Icons.Default.Assistant),
    ActionMeta(Action(GlobalActions.ONE_KEY_FREEZE_APPS), R.string.action_one_key_freeze_apps, ActionCategory.TOOL,
        icon = Icons.Default.AcUnit,
        tags = listOf(ActionTag.System)),
)

val actionMetaByValue: Map<String, ActionMeta> = actionMetaList.associateBy { it.action.value }
