package hunoia.sideleap.system.api

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import hunoia.sideleap.BuildConfig
import hunoia.sideleap.IShizukuCommandService
import hunoia.sideleap.system.shizuku.ShizukuCommandService
import java.util.concurrent.atomic.AtomicBoolean

data class PackageCommandResult(
    val success: Boolean,
    val packageName: String,
    val exitCode: Int = -1,
    val error: String? = null
)

data class BatchFrozenResult(
    val requestedCount: Int,
    val attemptedCount: Int,
    val successCount: Int,
    val failedCount: Int,
    val fallbackTriggered: Boolean,
    val fallbackAttemptedCount: Int = 0,
    val fallbackSuccessCount: Int = 0,
    val fallbackFailedCount: Int = 0,
    val errorSummary: String? = null
)

data class EnablePackageResult(
    val success: Boolean,
    val packageName: String,
    val exitCode: Int = -1,
    val output: String = "",
    val error: String? = null
)

object ShizukuCommand {

    private fun createArgs(context: Context, suffix: String) = UserServiceArgs(
        ComponentName(context.packageName, ShizukuCommandService::class.java.name)
    ).processNameSuffix(suffix).tag("sideleap-frozen-app-action")
        .version(1).debuggable(true).daemon(false)

    private fun runWithBinder(
        context: Context, packageName: String, suffix: String,
        call: (IShizukuCommandService) -> String
    ): PackageCommandResult {
        val args = createArgs(context, suffix)
        val latch = java.util.concurrent.CountDownLatch(1)
        val timedOut = java.util.concurrent.atomic.AtomicBoolean(false)
        var result = ""

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                try {
                    val service = IShizukuCommandService.Stub.asInterface(binder)
                    result = call(service)
                    try { service.destroy() } catch (_: Exception) {}
                } catch (e: Exception) {
                    result = "error: ${e::class.simpleName} ${e.message}"
                } finally { latch.countDown() }
            }
            override fun onServiceDisconnected(name: ComponentName?) {}
            override fun onBindingDied(name: ComponentName?) { latch.countDown() }
            override fun onNullBinding(name: ComponentName?) { latch.countDown() }
        }

        try {
            Shizuku.bindUserService(args, conn)
            if (!latch.await(8, java.util.concurrent.TimeUnit.SECONDS)) timedOut.set(true)
        } catch (e: Exception) {
            result = "error: ${e::class.simpleName} ${e.message}"
        } finally {
            try { Shizuku.unbindUserService(args, conn, true) } catch (_: Exception) {}
        }

        if (timedOut.get()) return PackageCommandResult(false, packageName, error = "timeout")
        return parseFrozenActionResult(packageName, result)
    }

    fun disablePackage(context: Context, packageName: String): PackageCommandResult {
        return executePackageCommand(context, packageName, disable = true)
    }

    fun enablePackage(context: Context, packageName: String): PackageCommandResult {
        return executePackageCommand(context, packageName, disable = false)
    }

    fun executeBatch(context: Context, packageNames: List<String>, disable: Boolean): BatchFrozenResult {
        val requestedCount = packageNames.size
        if (packageNames.isEmpty()) return BatchFrozenResult(0, 0, 0, 0, false)
        val dt = "FrozenBatch"

        if (!ShizukuRuntime.awaitBinderReady()) {
            Log.e(dt, "shizuku binder not received after timeout")
            return BatchFrozenResult(
                requestedCount = requestedCount, attemptedCount = 0,
                successCount = 0, failedCount = requestedCount,
                fallbackTriggered = false, errorSummary = "shizuku binder not received"
            )
        }

        val args = createArgs(context, "frozen_app_action")
        val latch = java.util.concurrent.CountDownLatch(1)
        val timedOut = java.util.concurrent.atomic.AtomicBoolean(false)
        val results = mutableListOf<PackageCommandResult>()

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                if (binder == null) {
                    Log.e(dt, "binder is null")
                    packageNames.forEach { pkg ->
                        results += PackageCommandResult(false, pkg, error = "null binder")
                    }
                    latch.countDown()
                    return
                }
                try {
                    val service = IShizukuCommandService.Stub.asInterface(binder)
                    packageNames.forEach { packageName ->
                        try {
                            val apiResult = if (disable) {
                                service.disablePackageApi(packageName)
                            } else {
                                service.enablePackageApi(packageName)
                            }
                            val parsed = parseFrozenActionResult(packageName, apiResult)
                            results += parsed
                            if (!parsed.success) {
                                Log.e(dt, "FAIL pkg=$packageName exitCode=${parsed.exitCode} error=${parsed.error}")
                            }
                        } catch (e: Exception) {
                            Log.e(dt, "EXCEPTION pkg=$packageName ${e::class.simpleName} ${e.message}")
                            results += PackageCommandResult(false, packageName, error = "${e::class.simpleName} ${e.message}")
                        }
                    }
                    try {
                        service.destroy()
                    } catch (e: Exception) {
                        Log.w(dt, "destroy exception: ${e::class.simpleName} ${e.message}")
                    }
                } catch (e: Exception) {
                    Log.e(dt, "onServiceConnected fatal: ${e::class.simpleName} ${e.message}")
                    packageNames.forEach { pkg ->
                        results += PackageCommandResult(false, pkg, error = "${e::class.simpleName} ${e.message}")
                    }
                } finally {
                    latch.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.w(dt, "onServiceDisconnected")
            }
            override fun onBindingDied(name: ComponentName?) {
                Log.e(dt, "onBindingDied")
                latch.countDown()
            }
            override fun onNullBinding(name: ComponentName?) {
                Log.e(dt, "onNullBinding")
                latch.countDown()
            }
        }

        var bindException: String? = null
        try {
            Shizuku.bindUserService(args, conn)
            val completed = latch.await(8, java.util.concurrent.TimeUnit.SECONDS)
            if (!completed) {
                timedOut.set(true)
                Log.e(dt, "latch timed out after 8s")
            }
        } catch (e: Exception) {
            bindException = "${e::class.simpleName} ${e.message}"
            Log.e(dt, "bindUserService exception: $bindException")
            packageNames.forEach { pkg ->
                results += PackageCommandResult(false, pkg, error = bindException)
            }
        } finally {
            try {
                Shizuku.unbindUserService(args, conn, true)
            } catch (e: Exception) {
                Log.w(dt, "unbind exception: ${e::class.simpleName} ${e.message}")
            }
        }

        if (timedOut.get()) {
            Log.e(dt, "batch timed out, falling back")
            return executeSingleFallback(context, packageNames, disable, "timeout")
        }
        if (bindException != null && results.isEmpty()) {
            Log.e(dt, "bind failed with no results, falling back")
            return executeSingleFallback(context, packageNames, disable, bindException)
        }
        if (results.isEmpty()) {
            Log.e(dt, "empty results despite successful bind, falling back")
            return executeSingleFallback(context, packageNames, disable, "empty results")
        }

        val batchSuccessCount = results.count { it.success }
        val batchFailedCount = results.count { !it.success }
        val needsFallback = batchFailedCount > 0

        if (needsFallback) {
            Log.w(dt, "batch partial/full failure: success=$batchSuccessCount failed=$batchFailedCount, falling back")
            return executeSingleFallback(context, packageNames, disable,
                "batch failed: ${batchFailedCount}/${requestedCount}")
        }

        return BatchFrozenResult(
            requestedCount = requestedCount,
            attemptedCount = results.size,
            successCount = batchSuccessCount,
            failedCount = batchFailedCount,
            fallbackTriggered = false
        )
    }

    private fun executeSingleFallback(
        context: Context, packageNames: List<String>, disable: Boolean, reason: String
    ): BatchFrozenResult {
        val dt = "FrozenBatch"
        Log.w(dt, "executeSingleFallback triggered: $reason")
        var fallbackAttempted = 0
        var fallbackSuccess = 0
        var fallbackFailed = 0
        for (pkg in packageNames) {
            val singleResult = executePackageCommandDirect(context, pkg, disable)
            fallbackAttempted++
            if (singleResult.success) {
                fallbackSuccess++
            } else {
                fallbackFailed++
                Log.e(dt, "fallback FAIL pkg=$pkg exitCode=${singleResult.exitCode} error=${singleResult.error}")
            }
        }
        return BatchFrozenResult(
            requestedCount = packageNames.size,
            attemptedCount = 0,
            successCount = 0,
            failedCount = packageNames.size,
            fallbackTriggered = true,
            fallbackAttemptedCount = fallbackAttempted,
            fallbackSuccessCount = fallbackSuccess,
            fallbackFailedCount = fallbackFailed,
            errorSummary = reason
        )
    }

    private fun executePackageCommand(context: Context, packageName: String, disable: Boolean): PackageCommandResult {
        if (!ShizukuRuntime.isAvailable() || ShizukuRuntime.isPreV11OrUnsupported() || !ShizukuRuntime.checkPermission()) {
            return PackageCommandResult(false, packageName, error = "shizuku unavailable")
        }
        return runWithBinder(context, packageName, "frozen_app_action") { service ->
            if (disable) service.disablePackageApi(packageName) else service.enablePackageApi(packageName)
        }
    }

    private fun executePackageCommandDirect(context: Context, packageName: String, disable: Boolean): PackageCommandResult {
        if (!ShizukuRuntime.awaitBinderReady()) {
            return PackageCommandResult(false, packageName, error = "shizuku binder not received")
        }
        return runWithBinder(context, packageName, "frozen_app_action") { service ->
            if (disable) service.disablePackageApi(packageName) else service.enablePackageApi(packageName)
        }
    }

    private fun parseFrozenActionResult(packageName: String, result: String): PackageCommandResult {
        if (result.startsWith("error:")) {
            return PackageCommandResult(false, packageName, error = result.removePrefix("error:").trim())
        }
        val exitCode = result.lines()
            .firstOrNull { it.startsWith("exitCode=") }
            ?.removePrefix("exitCode=")
            ?.toIntOrNull() ?: -1
        return PackageCommandResult(
            success = exitCode == 0,
            packageName = packageName,
            exitCode = exitCode
        )
    }

    private val enableLock = Any()
    private var enableService: IShizukuCommandService? = null
    private var enableBinder: IBinder? = null
    private var enableArgs: UserServiceArgs? = null

    private val enableDeathRecipient = IBinder.DeathRecipient {
        synchronized(enableLock) {
            if (BuildConfig.DEBUG) {
                android.util.Log.d("LauncherPerf", "enable_package: bind_user_service dead")
            }
            enableService = null
            enableBinder = null
            enableArgs = null
        }
    }

    fun enablePackageForLauncher(context: Context, packageName: String): EnablePackageResult {
        if (!ShizukuRuntime.isAvailable()) {
            if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "shizuku_enable_launcher: unavailable")
            return EnablePackageResult(false, packageName, error = "shizuku unavailable")
        }
        if (ShizukuRuntime.isPreV11OrUnsupported()) {
            if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "shizuku_enable_launcher: unsupported")
            return EnablePackageResult(false, packageName, error = "shizuku unsupported")
        }
        if (!ShizukuRuntime.checkPermission()) {
            if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "shizuku_enable_launcher: permission denied")
            return EnablePackageResult(false, packageName, error = "permission denied")
        }

        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "shizuku_enable_launcher: target=$packageName")

        var result = enableWithCachedService(context, packageName)
        if (result != null) {
            if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "shizuku_enable_launcher: result=${result.success}")
            return result
        }

        synchronized(enableLock) { clearEnableCache() }
        result = enableWithCachedService(context, packageName)
        if (result != null) {
            if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "shizuku_enable_launcher: result=${result.success} retry")
            return result
        }

        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "shizuku_enable_launcher: failed after retry")
        return EnablePackageResult(false, packageName, error = "enable failed after retry")
    }

    fun clearEnableServiceCache() {
        synchronized(enableLock) {
            enableBinder?.unlinkToDeath(enableDeathRecipient, 0)
            enableService = null
            enableBinder = null
            enableArgs = null
        }
        if (BuildConfig.DEBUG) {
            android.util.Log.d("LauncherPerf", "enable_package: cache cleared")
        }
    }

    private fun clearEnableCache() {
        enableBinder?.unlinkToDeath(enableDeathRecipient, 0)
        enableService = null
        enableBinder = null
        enableArgs = null
    }

    private fun enableWithCachedService(context: Context, packageName: String): EnablePackageResult? {
        val cached = synchronized(enableLock) { enableService }
        if (cached != null) {
            if (BuildConfig.DEBUG) {
                android.util.Log.d("LauncherPerf", "enable_package: bind_user_service reused pkg=$packageName")
            }
            return try {
                val resultStr = cached.enablePackage(packageName)
                parseLauncherResult(context, resultStr, packageName)
            } catch (e: android.os.DeadObjectException) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.d("LauncherPerf", "enable_package: bind_user_service dead pkg=$packageName ${e::class.simpleName}")
                }
                null
            } catch (e: android.os.RemoteException) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.d("LauncherPerf", "enable_package: bind_user_service remote_exception pkg=$packageName ${e::class.simpleName}")
                }
                null
            }
        }
        return enableBindAndCache(context, packageName)
    }

    private fun enableBindAndCache(context: Context, packageName: String): EnablePackageResult? {
        val tBind = System.currentTimeMillis()
        if (BuildConfig.DEBUG) {
            android.util.Log.d("LauncherPerf", "enable_package: bind_user_service start pkg=$packageName")
        }

        val args = UserServiceArgs(
            ComponentName(context.packageName, ShizukuCommandService::class.java.name)
        ).processNameSuffix("enable_launcher").tag("sideleap-enable-package-launcher")
            .version(1).debuggable(true).daemon(false)

        val latch = java.util.concurrent.CountDownLatch(1)
        var result = ""
        val timedOut = java.util.concurrent.atomic.AtomicBoolean(false)
        var serviceRef: IShizukuCommandService? = null
        var binderRef: IBinder? = null

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.d("LauncherPerf",
                        "enable_package: bind_user_service connected pkg=$packageName elapsed=${System.currentTimeMillis() - tBind}ms")
                }
                try {
                    val svc = IShizukuCommandService.Stub.asInterface(binder)
                    serviceRef = svc
                    binderRef = binder
                    result = svc.enablePackage(packageName)
                } catch (e: Exception) {
                    result = "error: ${e::class.simpleName} ${e.message}"
                } finally {
                    latch.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
            override fun onBindingDied(name: ComponentName?) { latch.countDown() }
            override fun onNullBinding(name: ComponentName?) { latch.countDown() }
        }

        try {
            Shizuku.bindUserService(args, conn)
            if (!latch.await(8, java.util.concurrent.TimeUnit.SECONDS)) {
                timedOut.set(true)
            }
        } catch (e: Exception) {
            result = "error: ${e::class.simpleName} ${e.message}"
        }

        if (timedOut.get()) {
            if (BuildConfig.DEBUG) {
                android.util.Log.d("LauncherPerf",
                    "enable_package: bind_user_service timeout pkg=$packageName")
            }
            try { Shizuku.unbindUserService(args, conn, true) } catch (_: Exception) {}
            return EnablePackageResult(false, packageName, error = "timeout")
        }

        if (result.startsWith("error:")) {
            try { Shizuku.unbindUserService(args, conn, true) } catch (_: Exception) {}
            return EnablePackageResult(false, packageName, error = result.removePrefix("error:").trim())
        }

        val binderToCache = binderRef
        val serviceToCache = serviceRef
        synchronized(enableLock) {
            if (enableService == null && serviceToCache != null && binderToCache != null) {
                enableService = serviceToCache
                enableBinder = binderToCache
                enableArgs = args
                try {
                    binderToCache.linkToDeath(enableDeathRecipient, 0)
                } catch (_: Exception) {}
            }
        }

        return parseLauncherResult(context, result, packageName)
    }

    private fun parseLauncherResult(context: Context, result: String, packageName: String): EnablePackageResult {
        val exitCode = result.lines()
            .firstOrNull { it.startsWith("exitCode=") }
            ?.removePrefix("exitCode=")
            ?.toIntOrNull() ?: -1
        val output = result.lines()
            .firstOrNull { it.startsWith("output=") }
            ?.removePrefix("output=") ?: ""
        val success = exitCode == 0
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "shizuku_enable_launcher: exitCode=$exitCode success=$success")
        for (line in result.lines()) {
            if (line.isNotBlank()) {
                if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "shizuku_enable_launcher: $line")
            }
        }
        return EnablePackageResult(success, packageName, exitCode, output)
    }
}
