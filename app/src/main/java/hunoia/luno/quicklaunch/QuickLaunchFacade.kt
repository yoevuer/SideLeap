package hunoia.luno.quicklaunch

import android.content.Context
import android.graphics.drawable.Drawable
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.quicklaunch.query.AppQuery
import hunoia.luno.quicklaunch.query.ShortcutQuery
import hunoia.luno.quicklaunch.query.LauncherIconQuery
import hunoia.luno.quicklaunch.launch.Launcher
import hunoia.luno.quicklaunch.launch.QuickAppLaunch
import hunoia.luno.quicklaunch.icon.IconResizeCache
import hunoia.luno.quicklaunch.query.ActivityOption
import hunoia.luno.quicklaunch.query.LauncherAppOption
import hunoia.luno.quicklaunch.query.OpenAppOrUrlQuery
import kotlinx.coroutines.CoroutineScope

object QuickLaunchFacade {

    var showOverlay: () -> Unit = {}

    fun queryApps(context: Context, allowRepeatPackage: Boolean = true): List<AppInfo> =
        AppQuery.queryLauncherActivities(context, allowRepeatPackage)

    fun queryShortcutActivities(context: Context, allowRepeatPackage: Boolean = true): List<LauncherInfo> =
        AppQuery.queryCreateShortcutActivities(context, allowRepeatPackage)

    suspend fun findApp(context: Context, packageName: String): AppInfo? =
        AppQuery.findLauncherActivity(context, packageName)

    fun queryShortcuts(context: Context): List<LauncherInfo> =
        ShortcutQuery.getAllAppsWithShortcut(context)

    fun loadIcon(context: Context, packageName: String): Drawable? =
        LauncherIconQuery.loadApplicationIcon(context, packageName)

    fun resolveShortcutIconResourceId(context: Context, iconResource: android.content.Intent.ShortcutIconResource): Int =
        LauncherIconQuery.resolveShortcutIconResourceId(context, iconResource)

    fun getCachedIcon(packageName: String): Drawable? =
        IconResizeCache.iconCache[packageName]

    fun cacheIcon(packageName: String, icon: Drawable) {
        IconResizeCache.iconCache[packageName] = icon
    }

    fun getCachedIconBgColor(packageName: String): Int =
        IconResizeCache.iconBgColorCache[packageName] ?: 0

    fun cacheIconBgColor(packageName: String, color: Int) {
        IconResizeCache.iconBgColorCache[packageName] = color
    }

    fun clearIconCache() {
        IconResizeCache.iconCache.clear()
        IconResizeCache.iconBgColorCache.clear()
    }

    fun getIconCacheSnapshot(): Map<String, Drawable> =
        IconResizeCache.iconCache.toMap()

    fun getIconBgColorCacheSnapshot(): Map<String, Int> =
        IconResizeCache.iconBgColorCache.toMap()

    fun queryLauncherAppOptions(context: Context): List<LauncherAppOption> =
        OpenAppOrUrlQuery.queryLauncherApps(context)

    fun queryActivityOptions(
        context: Context,
        packageName: String,
        selectedActivityClassName: String,
        launcherClassName: String?
    ): List<ActivityOption> =
        OpenAppOrUrlQuery.queryActivityOptions(context, packageName, selectedActivityClassName, launcherClassName)

    fun formatActivityOptionText(option: ActivityOption, packageName: String): String =
        OpenAppOrUrlQuery.formatActivityOptionText(option, packageName)

    fun launchApp(
        context: Context,
        coroutineScope: CoroutineScope,
        app: AppInfo,
        isFrozen: Boolean = false,
        miniWindow: Boolean = false,
        debugPrefix: String? = null,
        requestEnableFrozenPackage: ((String, (Boolean) -> Unit) -> Unit)? = null,
        log: ((String) -> Unit)? = null,
        onLaunch: ((AppInfo, Boolean) -> Boolean)? = null,
        onLaunched: (() -> Unit)? = null,
    ) {
        QuickAppLaunch.launch(
            context = context,
            coroutineScope = coroutineScope,
            app = app,
            isFrozen = isFrozen,
            miniWindow = miniWindow,
            debugPrefix = debugPrefix,
            requestEnableFrozenPackage = requestEnableFrozenPackage ?: { _, callback -> callback(true) },
            log = log ?: { message -> android.util.Log.d("LunoLauncher", message); Unit },
            onLaunch = onLaunch ?: { _, _ -> true },
            onLaunched = onLaunched ?: {}
        )
    }

    fun invalidateLauncherCache() {
        AppQuery.invalidateLauncherCache()
    }

    fun launchAppInPopup(
        context: Context,
        packageName: String,
        className: String,
        horizontalBias: Float = 0.5f,
        verticalBias: Float = 1.0f,
        verticalOffsetFraction: Float = 1.0f,
        widthFraction: Float = 0.5f,
        heightFraction: Float = 0.5f,
        overrideBounds: Boolean = false,
    ) {
        Launcher.launchAppInPopup(
            context, packageName, className,
            horizontalBias, verticalBias, verticalOffsetFraction,
            widthFraction, heightFraction, overrideBounds,
        )
    }

    fun launchShortcutInfo(context: Context, shortcutInfo: LauncherInfo.ShortcutInfo) {
        Launcher.launchShortcutInfo(context, shortcutInfo)
    }
}
