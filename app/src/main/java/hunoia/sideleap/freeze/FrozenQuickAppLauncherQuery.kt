package hunoia.sideleap.freeze

import android.content.Context
import hunoia.sideleap.launcher.query.AppQuery
import hunoia.sideleap.launcher.query.QuickAppLauncherAppList

object FrozenQuickAppLauncherQuery {
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
