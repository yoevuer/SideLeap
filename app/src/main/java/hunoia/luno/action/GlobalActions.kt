package hunoia.luno.action

import hunoia.luno.config.model.Action


object GlobalActions {

    /** 无 */
    const val NONE = "0"
    /** 返回键 */
    const val BACK = "1"
    /** 主页键 */
    const val HOME = "2"
    /** 最近键 */
    const val RECENT = "3"
    /** 音量增加键 */
    const val VOLUME_UP = "6"
    /** 音量减小键 */
    const val VOLUME_DOWN = "7"
    /** 静音开关 */
    const val MUTE = "8"
    /** 播放/暂停歌曲 */
    const val PLAY_PAUSE_SONG = "9"
    /** 上一首 */
    const val LAST_SONG = "10"
    /** 下一首 */
    const val NEXT_SONG = "11"
    /** 上一个应用程序 */
    const val PREVIOUS_APP = "12"
    /** 打开通知面板 */
    const val OPEN_NOTIFICATION_PANEL = "15"
    /** 打开快捷面板 */
    const val OPEN_QUICK_PANEL = "16"
    /** 锁屏 */
    const val LOCK_SCREEN = "17"
    /** 手电筒 */
    const val FLASHLIGHT = "19"
    /** 分屏视图 */
    const val SPLIT_SCREEN = "20"
    /** 应用小窗 */
    const val POPUP_SCREEN = "21"
    /** 语音助手 */
    const val ASSIST_APP = "22"
    /** 截屏 */
    const val SCREENSHOT = "24"
    /** 电源键菜单 */
    const val POWER_BUTTON = "29"
    /** 隐藏触钮 */
    const val HIDE_GESTURE_BUTTON = "40"
    /** 屏幕常亮 */
    const val KEEP_SCREEN_ON = "46"
    /** 快速回顶部 */
    const val BACK_TO_TOP = "47"
    /** 打开 Activity */
    const val OPEN_APP_ACTIVITY = "49"
    /** 打开链接 */
    const val OPEN_URL = "60"
    /** 快速应用启动器 */
    const val QUICK_APP_LAUNCHER = "50"
    /** 生成随机名称 */
    const val RANDOM_NAME = "51"
    /** 一键冻结应用 */
    const val ONE_KEY_FREEZE_APPS = "52"
    /** 生成密码并复制 */
    const val GENERATE_PASSWORD_COPY = "53"
    /** 模拟点击当前位置 */
    const val CLICK_CURRENT_POSITION = "55"
    /** 虚拟鼠标 */
    const val POINTER = "56"
    /** 滑动调节音量 */
    const val VOLUME_SCRUB = "57"
    /** 执行 Shell 命令 */
    const val EXECUTE_SHELL_COMMAND = "58"
    /** 子手势 */
    const val SUB_GESTURE = "59"

    /** 启动应用 */
    const val EXTRA_LAUNCH_APP = "101"
    /** 启动快捷方式 */
    const val EXTRA_LAUNCH_SHORTCUT = "102"
}
