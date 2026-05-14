package hunoia.sideleap.utils

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import hunoia.sideleap.IShizukuCommandService
import hunoia.sideleap.core.diagnostics.LauncherDiagnostics
import java.util.concurrent.atomic.AtomicBoolean

object ShizukuUtils {

    private val probeRunning = AtomicBoolean(false)
    private val enableProbeRunning = AtomicBoolean(false)

    fun dumpState(context: Context, prefix: String) {
        val available = try { Shizuku.pingBinder() } catch (e: Exception) { false }
        val version = try { Shizuku.getVersion() } catch (e: Exception) { -1 }
        val preV11 = version in 1..10
        val permission = try { Shizuku.checkSelfPermission() } catch (e: Exception) { -2 }
        val permissionStr = when (permission) {
            android.content.pm.PackageManager.PERMISSION_GRANTED -> "granted"
            android.content.pm.PackageManager.PERMISSION_DENIED -> "denied"
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
        if (!try { Shizuku.pingBinder() } catch (e: Exception) { false }) {
            LauncherDiagnostics.d(context, "shizuku_disabled_probe: shizuku not available")
            return
        }
        val checkVersion = try { Shizuku.getVersion() } catch (e: Exception) { -1 }
        if (checkVersion < 11) {
            LauncherDiagnostics.d(context, "shizuku_disabled_probe: shizuku version too old, need v11+")
            return
        }
        val hasPermission = try { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }
        if (!hasPermission) {
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
        if (!try { Shizuku.pingBinder() } catch (e: Exception) { false }) {
            LauncherDiagnostics.d(context, "shizuku_disabled_names: unavailable")
            return emptySet()
        }
        val checkVersion = try { Shizuku.getVersion() } catch (e: Exception) { -1 }
        if (checkVersion < 11) {
            LauncherDiagnostics.d(context, "shizuku_disabled_names: unsupported")
            return emptySet()
        }
        val hasPermission = try { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }
        if (!hasPermission) {
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

    fun enablePackageForDiagnostic(context: Context, packageName: String) {
        if (!try { Shizuku.pingBinder() } catch (e: Exception) { false }) {
            LauncherDiagnostics.d(context, "shizuku_enable_probe: unavailable")
            return
        }
        val checkVersion = try { Shizuku.getVersion() } catch (e: Exception) { -1 }
        if (checkVersion < 11) {
            LauncherDiagnostics.d(context, "shizuku_enable_probe: unsupported")
            return
        }
        val hasPermission = try { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }
        if (!hasPermission) {
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
}
