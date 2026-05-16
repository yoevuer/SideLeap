package hunoia.sideleap.freeze

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import hunoia.sideleap.freeze.ShizukuBridgeService
import hunoia.sideleap.system.api.ShizukuCommand
import hunoia.sideleap.system.api.ShizukuRuntime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

data class FreezeResult(
    val success: Boolean,
    val packageName: String,
    val wasFrozen: Boolean,
    val nowFrozen: Boolean
)

data class OneKeyFreezeResult(
    val oneKeyCount: Int,
    val protectedCount: Int,
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

    fun isShizukuReady(): Boolean {
        return ShizukuRuntime.isAvailable() &&
            !ShizukuRuntime.isPreV11OrUnsupported() &&
            ShizukuRuntime.checkPermission()
    }

    suspend fun checkAndFreeze(context: Context, packageName: String): FreezeResult {
        val wasFrozen = FreezeState.isFrozen(context, packageName)
        if (wasFrozen) {
            return FreezeResult(true, packageName, wasFrozen = true, nowFrozen = true)
        }
        val result = withContext(Dispatchers.IO) {
            ShizukuCommand.disablePackage(context, packageName)
        }
        delay(100)
        val nowFrozen = FreezeState.isFrozen(context, packageName)
        return FreezeResult(result.success, packageName, wasFrozen = false, nowFrozen = nowFrozen)
    }

    suspend fun checkAndUnfreeze(context: Context, packageName: String): FreezeResult {
        val wasFrozen = FreezeState.isFrozen(context, packageName)
        if (!wasFrozen) {
            return FreezeResult(true, packageName, wasFrozen = false, nowFrozen = false)
        }
        val result = withContext(Dispatchers.IO) {
            ShizukuCommand.enablePackage(context, packageName)
        }
        delay(100)
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
            ShizukuCommand.executeBatch(context, candidates, disable = true)
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
            ShizukuCommand.executeBatch(context, candidates, disable = false)
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
        val settings = hunoia.sideleap.settings.api.SettingsProvider.getFrozenAppSettings()
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
            if (!showSystemApps && FreezeState.isSystemApp(ai)) return@forEach
            installedTargets.add(pkg)
        }

        Log.i("OneKeyFreeze", "oneKeySet=${oneKeySet.size} protectedSet=${protectedSet.size} " +
            "rawTargets=${rawTargets.size} installedTargets=${installedTargets.size}")

        val frozenState = FreezeState.queryFrozenStateByPackage(context, installedTargets)
        val candidates = installedTargets.filter { frozenState[it] != true }

        Log.i("OneKeyFreeze", "frozenCandidates=${candidates.size}")

        if (candidates.isNotEmpty()) {
            ShizukuCommand.executeBatch(context, candidates, disable = true)
        }

        delay(100)
        val latestState = FreezeState.queryFrozenStateByPackage(context, candidates)
        val successCount = candidates.count { latestState[it] == true }

        if (candidates.isNotEmpty() && successCount == 0) {
            Log.e("OneKeyFreeze", "candidates=$candidates but all still not frozen after batch")
        }

        Log.i("OneKeyFreeze", "successCount=$successCount")

        OneKeyFreezeResult(
            oneKeyCount = oneKeySet.size,
            protectedCount = protectedSet.size,
            targetCount = installedTargets.size,
            candidateCount = candidates.size,
            successCount = successCount
        )
    }

    suspend fun oneKeyFreezeForService(context: Context): OneKeyFreezeResult = withContext(Dispatchers.IO) {
        Log.i("OneKeyFreeze", "using bridge service for action context")
        val intent = Intent(context, ShizukuBridgeService::class.java)
        val latch = CountDownLatch(1)
        val result = AtomicInteger(-1)

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                if (binder == null) { latch.countDown(); return }
                try {
                    val messenger = Messenger(binder)
                    val replyHandler = object : Handler(Looper.getMainLooper()) {
                        override fun handleMessage(msg: Message) {
                            if (msg.what == ShizukuBridgeService.MSG_FREEZE_BATCH_RESULT) {
                                result.set(msg.data.getInt(ShizukuBridgeService.EXTRA_SUCCESS_COUNT, -1))
                                latch.countDown()
                            }
                        }
                    }
                    val replyMessenger = Messenger(replyHandler)
                    val msg = Message.obtain(null, ShizukuBridgeService.MSG_FREEZE_BATCH)
                    msg.replyTo = replyMessenger
                    messenger.send(msg)
                } catch (e: Exception) {
                    latch.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }

        try {
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
            if (!latch.await(2, TimeUnit.SECONDS)) {
                result.set(-2)
            }
        } catch (e: Exception) {
            result.set(-3)
        } finally {
            try { context.unbindService(conn) } catch (_: Exception) {}
        }

        val successCount = result.get().coerceAtLeast(0)
        Log.i("OneKeyFreeze", "bridge result successCount=$successCount")
        if (successCount > 0) {
            FreezeState.invalidateFrozenCache()
        }
        OneKeyFreezeResult(
            oneKeyCount = successCount,
            protectedCount = 0,
            targetCount = successCount,
            candidateCount = successCount,
            successCount = successCount
        )
    }

    suspend fun oneKeyUnfreeze(context: Context, targets: List<String>): OneKeyFreezeResult = withContext(Dispatchers.IO) {
        val frozenState = FreezeState.queryFrozenStateByPackage(context, targets)
        val candidates = targets.filter { frozenState[it] == true }

        if (candidates.isNotEmpty()) {
            ShizukuCommand.executeBatch(context, candidates, disable = false)
        }

        delay(100)
        val latestState = FreezeState.queryFrozenStateByPackage(context, candidates)
        val successCount = candidates.count { latestState[it] != true }

        if (successCount > 0) {
            FreezeState.invalidateFrozenCache()
        }

        OneKeyFreezeResult(
            oneKeyCount = targets.size,
            protectedCount = 0,
            targetCount = targets.size,
            candidateCount = candidates.size,
            successCount = successCount
        )
    }

    fun computeOneKeyTargetsInRange(
        apps: List<hunoia.sideleap.launcher.model.AppInfo>,
        oneKeyPackageNames: Set<String>,
        protectedPackageNames: Set<String>
    ): List<String> {
        return apps
            .asSequence()
            .map { it.packageName }
            .filter { it !in protectedPackageNames }
            .distinct()
            .filter { it in oneKeyPackageNames }
            .toList()
    }
}
