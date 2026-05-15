package hunoia.sideleap.launcher.query

import android.content.Context
import hunoia.sideleap.freeze.FreezeState
import hunoia.sideleap.launcher.model.AppInfo

data class QuickAppLauncherAppList(
    val apps: List<AppInfo>,
    val frozenPkgs: Set<String>
)

object QuickAppLauncherQuery {
    fun queryApps(context: Context, showSystemApps: Boolean): QuickAppLauncherAppList {
        val frozenApps = FreezeState.queryFrozenApplications(context, showSystemApps)
        val frozenPkgSet = frozenApps.map { it.packageName }.toSet()
        val launcherApps = AppQuery.queryLauncherActivities(
            context = context,
            allowRepeatPackage = false,
            showSystemApps = showSystemApps
        )
        val normalPkgNames = launcherApps.map { it.packageName }.toSet()
        return QuickAppLauncherAppList(
            apps = launcherApps + frozenApps.filter { it.packageName !in normalPkgNames },
            frozenPkgs = frozenPkgSet
        )
    }
}
