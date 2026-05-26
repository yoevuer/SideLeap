package hunoia.luno.freeze

import android.content.Context
import hunoia.luno.freeze.api.FreezeState
import hunoia.luno.launcher.query.AppQuery
import hunoia.luno.launcher.query.QuickAppLauncherAppList

object FrozenQuickAppLauncherQuery {
    fun queryApps(context: Context): QuickAppLauncherAppList {
        val frozenApps = FreezeState.queryFrozenApplications(context)
        val frozenPkgSet = frozenApps.map { it.packageName }.toSet()
        val launcherApps = AppQuery.queryLauncherActivities(
            context = context,
            allowRepeatPackage = false
        )
        val normalPkgNames = launcherApps.map { it.packageName }.toSet()
        return QuickAppLauncherAppList(
            apps = launcherApps + frozenApps.filter { it.packageName !in normalPkgNames },
            frozenPkgs = frozenPkgSet
        )
    }
}
