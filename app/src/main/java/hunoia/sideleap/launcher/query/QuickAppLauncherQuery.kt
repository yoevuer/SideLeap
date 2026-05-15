package hunoia.sideleap.launcher.query

import android.content.Context
import hunoia.sideleap.launcher.model.AppInfo

data class QuickAppLauncherAppList(
    val apps: List<AppInfo>,
    val frozenPkgs: Set<String>
)

object QuickAppLauncherQuery {
    fun queryApps(context: Context, showSystemApps: Boolean): QuickAppLauncherAppList {
        val launcherApps = AppQuery.queryLauncherActivities(
            context = context,
            allowRepeatPackage = false,
            showSystemApps = showSystemApps
        )
        return QuickAppLauncherAppList(
            apps = launcherApps,
            frozenPkgs = emptySet()
        )
    }
}
