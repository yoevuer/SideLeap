package hunoia.luno.freeze.api

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import hunoia.luno.config.ConfigProvider
import hunoia.luno.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class FreezeResult(
    val success: Boolean,
    val packageName: String,
    val wasFrozen: Boolean,
    val nowFrozen: Boolean
)

data class OneKeyFreezeResult(
    val oneKeyCount: Int,
    val targetCount: Int,
    val candidateCount: Int,
    val successCount: Int
)

data class BatchFreezeResult(
    val requestedCount: Int,
    val successCount: Int,
    val failedCount: Int,
    val attemptedCount: Int
)

object FreezeAction {

    fun isShizukuReady(): Boolean = ShizukuManager.currentStatus().isReady

    suspend fun checkAndFreeze(context: Context, packageName: String): FreezeResult {
        val wasFrozen = FreezeState.isFrozen(context, packageName)
        if (wasFrozen) {
            return FreezeResult(true, packageName, wasFrozen = true, nowFrozen = true)
        }
        val result = withContext(Dispatchers.IO) {
            ShizukuManager.disablePackage(packageName)
        }
        FreezeState.invalidateFrozenCache()
        val nowFrozen = FreezeState.isFrozen(context, packageName)
        return FreezeResult(result.success, packageName, wasFrozen = false, nowFrozen = nowFrozen)
    }

    suspend fun checkAndUnfreeze(context: Context, packageName: String): FreezeResult {
        val wasFrozen = FreezeState.isFrozen(context, packageName)
        if (!wasFrozen) {
            return FreezeResult(true, packageName, wasFrozen = false, nowFrozen = false)
        }
        val result = withContext(Dispatchers.IO) {
            ShizukuManager.enablePackage(packageName)
        }
        FreezeState.invalidateFrozenCache()
        val nowFrozen = FreezeState.isFrozen(context, packageName)
        return FreezeResult(result.success, packageName, wasFrozen = true, nowFrozen = nowFrozen)
    }

    suspend fun batchFreeze(context: Context, packageNames: List<String>): BatchFreezeResult {
        val frozenState = FreezeState.queryFrozenStateByPackage(context, packageNames)
        val candidates = packageNames.filter { frozenState[it] != true }
        if (candidates.isEmpty()) {
            return BatchFreezeResult(packageNames.size, 0, 0, 0)
        }
        val batchResult = withContext(Dispatchers.IO) {
            ShizukuManager.executeBatch(candidates, disable = true)
        }
        val successCount = if (batchResult.fallbackTriggered) {
            batchResult.fallbackSuccessCount
        } else {
            batchResult.successCount
        }
        return BatchFreezeResult(
            requestedCount = packageNames.size,
            successCount = successCount,
            failedCount = candidates.size - successCount,
            attemptedCount = candidates.size
        )
    }

    suspend fun batchUnfreeze(context: Context, packageNames: List<String>): BatchFreezeResult {
        val frozenState = FreezeState.queryFrozenStateByPackage(context, packageNames)
        val candidates = packageNames.filter { frozenState[it] == true }
        if (candidates.isEmpty()) {
            return BatchFreezeResult(packageNames.size, 0, 0, 0)
        }
        val batchResult = withContext(Dispatchers.IO) {
            ShizukuManager.executeBatch(candidates, disable = false)
        }
        val successCount = if (batchResult.fallbackTriggered) {
            batchResult.fallbackSuccessCount
        } else {
            batchResult.successCount
        }
        return BatchFreezeResult(
            requestedCount = packageNames.size,
            successCount = successCount,
            failedCount = candidates.size - successCount,
            attemptedCount = candidates.size
        )
    }

    suspend fun oneKeyFreeze(context: Context): OneKeyFreezeResult = withContext(Dispatchers.IO) {
        val settings = ConfigProvider.getFrozenAppSettings()
        val oneKeySet = settings.oneKeyPackageNames

        val rawTargets = oneKeySet

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
            installedTargets.add(pkg)
        }

        Log.i("OneKeyFreeze", "oneKeySet=${oneKeySet.size} " +
            "rawTargets=${rawTargets.size} installedTargets=${installedTargets.size}")

        val frozenState = FreezeState.queryFrozenStateByPackage(context, installedTargets)
        val candidates = installedTargets.filter { frozenState[it] != true }

        Log.i("OneKeyFreeze", "frozenCandidates=${candidates.size}")

        if (candidates.isNotEmpty()) {
            ShizukuManager.executeBatch(candidates, disable = true)
        }

        FreezeState.invalidateFrozenCache()
        val latestState = FreezeState.queryFrozenStateByPackage(context, candidates)
        val successCount = candidates.count { latestState[it] == true }

        if (candidates.isNotEmpty() && successCount == 0) {
            Log.e("OneKeyFreeze", "candidates=$candidates but all still not frozen after batch")
        }

        Log.i("OneKeyFreeze", "successCount=$successCount")

        OneKeyFreezeResult(
            oneKeyCount = oneKeySet.size,
            targetCount = installedTargets.size,
            candidateCount = candidates.size,
            successCount = successCount
        )
    }

    suspend fun oneKeyFreezeForService(context: Context): OneKeyFreezeResult {
        return oneKeyFreeze(context)
    }

    suspend fun oneKeyUnfreeze(context: Context, targets: List<String>): OneKeyFreezeResult = withContext(Dispatchers.IO) {
        val frozenState = FreezeState.queryFrozenStateByPackage(context, targets)
        val candidates = targets.filter { frozenState[it] == true }

        if (candidates.isNotEmpty()) {
            ShizukuManager.executeBatch(candidates, disable = false)
        }

        FreezeState.invalidateFrozenCache()
        val latestState = FreezeState.queryFrozenStateByPackage(context, candidates)
        val successCount = candidates.count { latestState[it] != true }

        if (successCount > 0) {
            FreezeState.invalidateFrozenCache()
        }

        OneKeyFreezeResult(
            oneKeyCount = targets.size,
            targetCount = targets.size,
            candidateCount = candidates.size,
            successCount = successCount
        )
    }

    fun computeOneKeyTargetsInRange(
        apps: List<hunoia.luno.quicklaunch.model.AppInfo>,
        oneKeyPackageNames: Set<String>
    ): List<String> {
        return apps
            .asSequence()
            .map { it.packageName }
            .distinct()
            .filter { it in oneKeyPackageNames }
            .toList()
    }
}
