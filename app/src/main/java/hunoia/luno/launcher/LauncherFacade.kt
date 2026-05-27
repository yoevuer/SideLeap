package hunoia.luno.launcher

import android.content.Context
import android.graphics.drawable.Drawable
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.launcher.model.LauncherInfo
import hunoia.luno.launcher.query.AppQuery
import hunoia.luno.launcher.query.ShortcutQuery
import hunoia.luno.launcher.query.LauncherIconQuery
import hunoia.luno.launcher.launch.QuickAppLaunch
import hunoia.luno.launcher.icon.IconResizeCache
import hunoia.luno.launcher.query.ActivityOption
import hunoia.luno.launcher.query.LauncherAppOption
import hunoia.luno.launcher.query.OpenAppOrUrlQuery
import kotlinx.coroutines.CoroutineScope

object LauncherFacade {

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
}
