package hunoia.sideleap.launcher.query

import android.content.Context
import hunoia.sideleap.launcher.model.AppInfo

data class QuickAppLauncherAppList(
    val apps: List<AppInfo>,
    val frozenPkgs: Set<String>
)

object QuickAppLauncherBaseQuery {
    fun queryApps(context: Context): QuickAppLauncherAppList {
        val launcherApps = AppQuery.queryLauncherActivities(
            context = context,
            allowRepeatPackage = false
        )
        return QuickAppLauncherAppList(
            apps = launcherApps,
            frozenPkgs = emptySet()
        )
    }
}
