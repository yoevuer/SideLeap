package hunoia.sideleap.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.ServiceConnection
import android.os.IBinder
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import hunoia.sideleap.BuildConfig
import hunoia.sideleap.IShizukuCommandService

import java.util.concurrent.atomic.AtomicBoolean

object ShizukuUtils {

    private val probeRunning = AtomicBoolean(false)

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun getVersion(): Int {
        return try {
            Shizuku.getVersion()
        } catch (e: Exception) {
            -1
        }
    }

    fun isPreV11OrUnsupported(): Boolean {
        val version = getVersion()
        return version < 11
    }

    fun checkPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermissionIfNeeded(requestCode: Int = 0): Boolean {
        return try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                if (Shizuku.shouldShowRequestPermissionRationale()) {
                    return false
                }
                Shizuku.requestPermission(requestCode)
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun dumpState(context: Context, prefix: String) {
        val available = isShizukuAvailable()
        val version = getVersion()
        val preV11 = version in 1..10
        val permission = try {
            Shizuku.checkSelfPermission()
        } catch (e: Exception) {
            -2
        }
        val permissionStr = when (permission) {
            PackageManager.PERMISSION_GRANTED -> "granted"
            PackageManager.PERMISSION_DENIED -> "denied"
            else -> "unknown($permission)"
        }
        val processName = try {
            java.io.File("/proc/self/cmdline").readText().trimEnd('\u0000')
        } catch (e: Exception) { "unknown" }

        LauncherDiagnostics.d(context,
            "shizuku_state($prefix): process=$processName " +
            "available=$available version=$version preV11=$preV11 " +
            "permission=$permissionStr"
        )
    }

    fun dumpDisabledPackagesViaUserService(context: Context) {
        if (!isShizukuAvailable()) {
            LauncherDiagnostics.d(context, "shizuku_disabled_probe: shizuku not available")
            return
        }
        if (isPreV11OrUnsupported()) {
            LauncherDiagnostics.d(context, "shizuku_disabled_probe: shizuku version too old, need v11+")
            return
        }
        if (!checkPermission()) {
            LauncherDiagnostics.d(context, "shizuku_disabled_probe: permission not granted")
            return
        }
        if (!probeRunning.compareAndSet(false, true)) {
            LauncherDiagnostics.d(context, "shizuku_disabled_probe: already running")
            return
        }

        LauncherDiagnostics.d(context, "shizuku_disabled_probe: probe started on background thread")
        java.util.concurrent.CompletableFuture.runAsync {
            probeInternal(context)
        }
    }

    private fun probeInternal(context: Context) {
        try {
            LauncherDiagnostics.d(context, "shizuku_disabled_probe: binding user service")

        val args = UserServiceArgs(
            ComponentName(context.packageName, ShizukuCommandService::class.java.name)
        ).processNameSuffix("disabled_probe").tag("sideleap-disabled-packages-probe")
            .version(1).debuggable(true).daemon(false)

        val latch = java.util.concurrent.CountDownLatch(1)
        var result = ""
        val timedOut = java.util.concurrent.atomic.AtomicBoolean(false)

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                LauncherDiagnostics.d(context, "shizuku_disabled_probe: service connected")
                try {
                    val service = IShizukuCommandService.Stub.asInterface(binder)
                    LauncherDiagnostics.d(context, "shizuku_disabled_probe: calling listDisabledPackages")
                    result = service.listDisabledPackages()
                    LauncherDiagnostics.d(context, "shizuku_disabled_probe: listDisabledPackages returned")
                    try {
                        LauncherDiagnostics.d(context, "shizuku_disabled_probe: destroying user service")
                        service.destroy()
                        LauncherDiagnostics.d(context, "shizuku_disabled_probe: destroyed user service")
                    } catch (e: Exception) {
                        LauncherDiagnostics.d(context, "shizuku_disabled_probe: destroy exception: ${e::class.simpleName} ${e.message}")
                    }
                } catch (e: Exception) {
                    result = "error: ${e::class.simpleName} ${e.message}"
                    LauncherDiagnostics.d(context, "shizuku_disabled_probe: exception: ${e::class.simpleName} ${e.message}")
                } finally {
                    latch.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                LauncherDiagnostics.d(context, "shizuku_disabled_probe: service disconnected")
            }

            override fun onBindingDied(name: ComponentName?) {
                LauncherDiagnostics.d(context, "shizuku_disabled_probe: binding died")
                latch.countDown()
            }

            override fun onNullBinding(name: ComponentName?) {
                LauncherDiagnostics.d(context, "shizuku_disabled_probe: null binding")
                latch.countDown()
            }
        }

        try {
            Shizuku.bindUserService(args, conn)
            if (!latch.await(8, java.util.concurrent.TimeUnit.SECONDS)) {
                timedOut.set(true)
                LauncherDiagnostics.d(context, "shizuku_disabled_probe: timeout")
            }
        } catch (e: Exception) {
            result = "error: ${e::class.simpleName} ${e.message}"
            LauncherDiagnostics.d(context, "shizuku_disabled_probe: bind exception: ${e::class.simpleName} ${e.message}")
        } finally {
            LauncherDiagnostics.d(context, "shizuku_disabled_probe: unbinding user service")
            try { Shizuku.unbindUserService(args, conn, true) } catch (e: Exception) {
                LauncherDiagnostics.d(context, "shizuku_disabled_probe: unbind exception: ${e::class.simpleName} ${e.message}")
            }
            LauncherDiagnostics.d(context, "shizuku_disabled_probe: unbound user service")
        }

        if (timedOut.get()) return

        val stdoutLines = result.lines().count { it.startsWith("stdout=") || it.isNotBlank() && !it.startsWith("command=") && !it.startsWith("exitCode=") && !it.startsWith("service:") && !it.startsWith("error:") }
        LauncherDiagnostics.d(context, "shizuku_disabled_probe: stdoutLines=$stdoutLines")

        for (line in result.lines()) {
            if (line.isNotBlank()) {
                LauncherDiagnostics.d(context, "shizuku_disabled_probe: $line")
            }
        }
        } finally {
            probeRunning.set(false)
        }
    }

    fun fetchDisabledPackageNames(context: Context): Set<String> {
        if (!isShizukuAvailable()) {
            LauncherDiagnostics.d(context, "shizuku_disabled_names: unavailable")
            return emptySet()
        }
        if (isPreV11OrUnsupported()) {
            LauncherDiagnostics.d(context, "shizuku_disabled_names: unsupported")
            return emptySet()
        }
        if (!checkPermission()) {
            LauncherDiagnostics.d(context, "shizuku_disabled_names: permission denied")
            return emptySet()
        }
        return namesInternal(context)
    }

    private fun namesInternal(context: Context): Set<String> {
        try {
            LauncherDiagnostics.d(context, "shizuku_disabled_names: binding user service")

        val args = UserServiceArgs(
            ComponentName(context.packageName, ShizukuCommandService::class.java.name)
        ).processNameSuffix("disabled_probe").tag("sideleap-disabled-packages-probe")
            .version(1).debuggable(true).daemon(false)

        val latch = java.util.concurrent.CountDownLatch(1)
        var names = emptyList<String>()
        val timedOut = java.util.concurrent.atomic.AtomicBoolean(false)

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                LauncherDiagnostics.d(context, "shizuku_disabled_names: service connected")
                try {
                    val service = IShizukuCommandService.Stub.asInterface(binder)
                    LauncherDiagnostics.d(context, "shizuku_disabled_names: calling listDisabledPackageNames")
                    names = service.listDisabledPackageNames()
                    LauncherDiagnostics.d(context, "shizuku_disabled_names: listDisabledPackageNames returned count=${names.size}")
                    try {
                        LauncherDiagnostics.d(context, "shizuku_disabled_names: destroying user service")
                        service.destroy()
                        LauncherDiagnostics.d(context, "shizuku_disabled_names: destroyed user service")
                    } catch (e: Exception) {
                        LauncherDiagnostics.d(context, "shizuku_disabled_names: destroy exception: ${e::class.simpleName} ${e.message}")
                    }
                } catch (e: Exception) {
                    LauncherDiagnostics.d(context, "shizuku_disabled_names: exception: ${e::class.simpleName} ${e.message}")
                } finally {
                    latch.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                LauncherDiagnostics.d(context, "shizuku_disabled_names: service disconnected")
            }

            override fun onBindingDied(name: ComponentName?) {
                LauncherDiagnostics.d(context, "shizuku_disabled_names: binding died")
                latch.countDown()
            }

            override fun onNullBinding(name: ComponentName?) {
                LauncherDiagnostics.d(context, "shizuku_disabled_names: null binding")
                latch.countDown()
            }
        }

        try {
            Shizuku.bindUserService(args, conn)
            if (!latch.await(8, java.util.concurrent.TimeUnit.SECONDS)) {
                timedOut.set(true)
                LauncherDiagnostics.d(context, "shizuku_disabled_names: timeout")
            }
        } catch (e: Exception) {
            LauncherDiagnostics.d(context, "shizuku_disabled_names: bind exception: ${e::class.simpleName} ${e.message}")
        } finally {
            LauncherDiagnostics.d(context, "shizuku_disabled_names: unbinding user service")
            try { Shizuku.unbindUserService(args, conn, true) } catch (e: Exception) {
                LauncherDiagnostics.d(context, "shizuku_disabled_names: unbind exception: ${e::class.simpleName} ${e.message}")
            }
            LauncherDiagnostics.d(context, "shizuku_disabled_names: unbound user service")
        }

        if (timedOut.get()) return emptySet()

        val resultSet = names.toSet()
        LauncherDiagnostics.d(context, "shizuku_disabled_names: count=${resultSet.size}")
        for (pkg in resultSet.sorted()) {
            LauncherDiagnostics.d(context, "shizuku_disabled_names: package=$pkg")
        }
        return resultSet
        } finally {
        }
    }

    data class EnablePackageResult(
        val success: Boolean,
        val packageName: String,
        val exitCode: Int = -1,
        val output: String = "",
        val error: String? = null
    )

    data class PackageCommandResult(
        val success: Boolean,
        val packageName: String,
        val exitCode: Int = -1,
        val error: String? = null
    )

    fun isUsableForFrozenAppActions(): Boolean {
        if (!isShizukuAvailable()) return false
        if (isPreV11OrUnsupported()) return false
        if (!checkPermission()) return false
        return true
    }

    fun disablePackageForFrozenApp(context: Context, packageName: String): PackageCommandResult {
        return executePackageCommand(context, packageName, disable = true)
    }

    fun enablePackageForFrozenApp(context: Context, packageName: String): PackageCommandResult {
        return executePackageCommand(context, packageName, disable = false)
    }

    fun executeFrozenBatch(context: Context, packageNames: List<String>, disable: Boolean): List<PackageCommandResult> {
        if (packageNames.isEmpty()) return emptyList()
        if (!isUsableForFrozenAppActions()) {
            return packageNames.map {
                PackageCommandResult(false, it, error = "shizuku unavailable")
            }
        }

        val args = UserServiceArgs(
            ComponentName(context.packageName, ShizukuCommandService::class.java.name)
        ).processNameSuffix("frozen_app_action").tag("sideleap-frozen-app-action")
            .version(1).debuggable(true).daemon(false)

        val latch = java.util.concurrent.CountDownLatch(1)
        val timedOut = java.util.concurrent.atomic.AtomicBoolean(false)
        val results = mutableListOf<PackageCommandResult>()

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                try {
                    val service = IShizukuCommandService.Stub.asInterface(binder)
                    packageNames.forEach { packageName ->
                        val result = if (disable) {
                            service.disablePackageApi(packageName)
                        } else {
                            service.enablePackageApi(packageName)
                        }
                        results += parseFrozenActionResult(packageName, result)
                    }
                    try {
                        service.destroy()
                    } catch (_: Exception) {
                    }
                } catch (e: Exception) {
                    packageNames.forEach { packageName ->
                        results += PackageCommandResult(false, packageName, error = "${e::class.simpleName} ${e.message}")
                    }
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
            packageNames.forEach { packageName ->
                results += PackageCommandResult(false, packageName, error = "${e::class.simpleName} ${e.message}")
            }
        } finally {
            try {
                Shizuku.unbindUserService(args, conn, true)
            } catch (_: Exception) {
            }
        }

        if (timedOut.get()) {
            return packageNames.map { packageName ->
                PackageCommandResult(false, packageName, error = "timeout")
            }
        }
        if (results.isEmpty()) {
            return packageNames.map { packageName ->
                PackageCommandResult(false, packageName, error = "empty result")
            }
        }
        return results
    }

    private fun executePackageCommand(context: Context, packageName: String, disable: Boolean): PackageCommandResult {
        if (!isUsableForFrozenAppActions()) {
            return PackageCommandResult(false, packageName, error = "shizuku unavailable")
        }

        val args = UserServiceArgs(
            ComponentName(context.packageName, ShizukuCommandService::class.java.name)
        ).processNameSuffix("frozen_app_action").tag("sideleap-frozen-app-action")
            .version(1).debuggable(true).daemon(false)

        val latch = java.util.concurrent.CountDownLatch(1)
        val timedOut = java.util.concurrent.atomic.AtomicBoolean(false)
        var result = ""

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                try {
                    val service = IShizukuCommandService.Stub.asInterface(binder)
                    result = if (disable) {
                        service.disablePackageApi(packageName)
                    } else {
                        service.enablePackageApi(packageName)
                    }
                    try {
                        service.destroy()
                    } catch (_: Exception) {
                    }
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
        } finally {
            try {
                Shizuku.unbindUserService(args, conn, true)
            } catch (_: Exception) {
            }
        }

        if (timedOut.get()) return PackageCommandResult(false, packageName, error = "timeout")
        return parseFrozenActionResult(packageName, result)
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

    private val enableProbeRunning = AtomicBoolean(false)

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

    fun enablePackageForDiagnostic(context: Context, packageName: String) {
        if (!isShizukuAvailable()) {
            LauncherDiagnostics.d(context, "shizuku_enable_probe: unavailable")
            return
        }
        if (isPreV11OrUnsupported()) {
            LauncherDiagnostics.d(context, "shizuku_enable_probe: unsupported")
            return
        }
        if (!checkPermission()) {
            LauncherDiagnostics.d(context, "shizuku_enable_probe: permission denied")
            return
        }
        if (!enableProbeRunning.compareAndSet(false, true)) {
            LauncherDiagnostics.d(context, "shizuku_enable_probe: already running")
            return
        }

        LauncherDiagnostics.d(context, "shizuku_enable_probe: target=$packageName")
        java.util.concurrent.CompletableFuture.runAsync {
            try {
                LauncherDiagnostics.d(context, "shizuku_enable_probe: binding user service")

                val args = UserServiceArgs(
                    ComponentName(context.packageName, ShizukuCommandService::class.java.name)
                ).processNameSuffix("enable_probe").tag("sideleap-enable-package-probe")
                    .version(1).debuggable(true).daemon(false)

                val latch = java.util.concurrent.CountDownLatch(1)
                var result = ""
                val timedOut = java.util.concurrent.atomic.AtomicBoolean(false)

                val conn = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                        LauncherDiagnostics.d(context, "shizuku_enable_probe: service connected")
                        try {
                            val service = IShizukuCommandService.Stub.asInterface(binder)
                            LauncherDiagnostics.d(context, "shizuku_enable_probe: calling enablePackage")
                            result = service.enablePackage(packageName)
                            LauncherDiagnostics.d(context, "shizuku_enable_probe: enablePackage returned")
                            try {
                                LauncherDiagnostics.d(context, "shizuku_enable_probe: destroying user service")
                                service.destroy()
                                LauncherDiagnostics.d(context, "shizuku_enable_probe: destroyed user service")
                            } catch (e: Exception) {
                                LauncherDiagnostics.d(context, "shizuku_enable_probe: destroy exception: ${e::class.simpleName} ${e.message}")
                            }
                        } catch (e: Exception) {
                            result = "error: ${e::class.simpleName} ${e.message}"
                            LauncherDiagnostics.d(context, "shizuku_enable_probe: exception: ${e::class.simpleName} ${e.message}")
                        } finally {
                            latch.countDown()
                        }
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        LauncherDiagnostics.d(context, "shizuku_enable_probe: service disconnected")
                    }

                    override fun onBindingDied(name: ComponentName?) {
                        LauncherDiagnostics.d(context, "shizuku_enable_probe: binding died")
                        latch.countDown()
                    }

                    override fun onNullBinding(name: ComponentName?) {
                        LauncherDiagnostics.d(context, "shizuku_enable_probe: null binding")
                        latch.countDown()
                    }
                }

                try {
                    Shizuku.bindUserService(args, conn)
                    if (!latch.await(8, java.util.concurrent.TimeUnit.SECONDS)) {
                        timedOut.set(true)
                        LauncherDiagnostics.d(context, "shizuku_enable_probe: timeout")
                    }
                } catch (e: Exception) {
                    result = "error: ${e::class.simpleName} ${e.message}"
                    LauncherDiagnostics.d(context, "shizuku_enable_probe: bind exception: ${e::class.simpleName} ${e.message}")
                } finally {
                    LauncherDiagnostics.d(context, "shizuku_enable_probe: unbinding user service")
                    try { Shizuku.unbindUserService(args, conn, true) } catch (e: Exception) {
                        LauncherDiagnostics.d(context, "shizuku_enable_probe: unbind exception: ${e::class.simpleName} ${e.message}")
                    }
                    LauncherDiagnostics.d(context, "shizuku_enable_probe: unbound user service")
                }

                if (timedOut.get()) return@runAsync

                for (line in result.lines()) {
                    if (line.isNotBlank()) {
                        LauncherDiagnostics.d(context, "shizuku_enable_probe: $line")
                    }
                }
            } finally {
                enableProbeRunning.set(false)
            }
        }
    }

    fun enablePackageForLauncher(context: Context, packageName: String): EnablePackageResult {
        if (!isShizukuAvailable()) {
            LauncherDiagnostics.d(context, "shizuku_enable_launcher: unavailable")
            return EnablePackageResult(false, packageName, error = "shizuku unavailable")
        }
        if (isPreV11OrUnsupported()) {
            LauncherDiagnostics.d(context, "shizuku_enable_launcher: unsupported")
            return EnablePackageResult(false, packageName, error = "shizuku unsupported")
        }
        if (!checkPermission()) {
            LauncherDiagnostics.d(context, "shizuku_enable_launcher: permission denied")
            return EnablePackageResult(false, packageName, error = "permission denied")
        }

        LauncherDiagnostics.d(context, "shizuku_enable_launcher: target=$packageName")

        var result = enableWithCachedService(context, packageName)
        if (result != null) {
            LauncherDiagnostics.d(context, "shizuku_enable_launcher: result=${result.success}")
            return result
        }

        // Cache miss or dead — clear and retry with fresh bind
        synchronized(enableLock) { clearEnableCache() }
        result = enableWithCachedService(context, packageName)
        if (result != null) {
            LauncherDiagnostics.d(context, "shizuku_enable_launcher: result=${result.success} retry")
            return result
        }

        LauncherDiagnostics.d(context, "shizuku_enable_launcher: failed after retry")
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

        // Cache: keep service connected, do NOT destroy or unbind
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
        LauncherDiagnostics.d(context, "shizuku_enable_launcher: exitCode=$exitCode success=$success")
        for (line in result.lines()) {
            if (line.isNotBlank()) {
                LauncherDiagnostics.d(context, "shizuku_enable_launcher: $line")
            }
        }
        return EnablePackageResult(success, packageName, exitCode, output)
    }
}
