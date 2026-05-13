package hunoia.sideleap.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

data class OneKeyFreezeResult(
    val oneKeyCount: Int,
    val protectedCount: Int,
    val targetCount: Int,
    val candidateCount: Int,
    val successCount: Int
)

object FrozenAppActionUtils {

    suspend fun oneKeyFreeze(context: Context): OneKeyFreezeResult {
        val settings = DataStoreHolder.frozenAppSettings.data.first()
        val showSystemApps = settings.showSystemAppsInManagePage
        val oneKeySet = settings.oneKeyPackageNames
        val protectedSet = settings.protectedPackageNames

        val rawTargets = oneKeySet - protectedSet

        val pm = context.packageManager
        val installedTargets = mutableListOf<String>()

        rawTargets.forEach { pkg ->
            val ai = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getApplicationInfo(pkg, 0)
                }
            }.getOrNull()
            if (ai == null) return@forEach
            if (!showSystemApps && isSystemApp(ai)) return@forEach
            installedTargets.add(pkg)
        }

        val frozenState = AppInfoUtils.queryFrozenStateByPackage(context, installedTargets)
        val candidates = installedTargets.filter { frozenState[it] != true }

        if (candidates.isNotEmpty()) {
            ShizukuUtils.executeFrozenBatch(context, candidates, disable = true)
        }

        delay(100)
        val latestState = AppInfoUtils.queryFrozenStateByPackage(context, candidates)
        val successCount = candidates.count { latestState[it] == true }

        if (candidates.isNotEmpty() && successCount == 0) {
            Log.e("OneKeyFreeze", "candidates=$candidates but all still not frozen after batch")
        }

        return OneKeyFreezeResult(
            oneKeyCount = oneKeySet.size,
            protectedCount = protectedSet.size,
            targetCount = installedTargets.size,
            candidateCount = candidates.size,
            successCount = successCount
        )
    }

    private fun isSystemApp(ai: ApplicationInfo): Boolean {
        val flags = ai.flags
        return (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }
}