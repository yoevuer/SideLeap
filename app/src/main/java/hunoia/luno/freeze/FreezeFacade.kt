package hunoia.luno.freeze

import android.content.Context
import hunoia.luno.freeze.api.BatchFreezeResult
import hunoia.luno.freeze.api.FreezeAction
import hunoia.luno.freeze.api.FreezeResult
import hunoia.luno.freeze.api.FreezeState
import hunoia.luno.freeze.api.OneKeyFreezeResult
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.launcher.query.QuickAppLauncherAppList

object FreezeFacade {

    fun isShizukuReady(): Boolean = FreezeAction.isShizukuReady()

    fun isFrozen(context: Context, packageName: String): Boolean =
        FreezeState.isFrozen(context, packageName)

    suspend fun freeze(context: Context, packageName: String): FreezeResult =
        FreezeAction.checkAndFreeze(context, packageName)

    suspend fun unfreeze(context: Context, packageName: String): FreezeResult =
        FreezeAction.checkAndUnfreeze(context, packageName)

    suspend fun batchFreeze(context: Context, packageNames: List<String>): BatchFreezeResult =
        FreezeAction.batchFreeze(context, packageNames)

    suspend fun batchUnfreeze(context: Context, packageNames: List<String>): BatchFreezeResult =
        FreezeAction.batchUnfreeze(context, packageNames)

    suspend fun oneKeyFreeze(context: Context): OneKeyFreezeResult =
        FreezeAction.oneKeyFreeze(context)

    suspend fun oneKeyUnfreeze(context: Context, targets: List<String>): OneKeyFreezeResult =
        FreezeAction.oneKeyUnfreeze(context, targets)

    suspend fun oneKeyFreezeForService(context: Context): OneKeyFreezeResult =
        FreezeAction.oneKeyFreezeForService(context)

    fun queryFrozenApps(context: Context): List<AppInfo> =
        FreezeState.queryFrozenApplications(context)

    suspend fun queryFrozenAppsOnIo(context: Context): List<AppInfo> =
        FreezeState.queryFrozenApplicationsOnIo(context)

    fun queryFrozenStateByPackage(context: Context, packageNames: Collection<String>): Map<String, Boolean> =
        FreezeState.queryFrozenStateByPackage(context, packageNames)

    fun computeOneKeyTargets(apps: List<AppInfo>, oneKeyPackageNames: Set<String>): List<String> =
        FreezeAction.computeOneKeyTargetsInRange(apps, oneKeyPackageNames)

    fun invalidateFrozenCache() {
        FreezeState.invalidateFrozenCache()
    }

    fun queryQuickAppLauncherApps(context: Context): QuickAppLauncherAppList =
        FrozenQuickAppLauncherQuery.queryApps(context)
}
