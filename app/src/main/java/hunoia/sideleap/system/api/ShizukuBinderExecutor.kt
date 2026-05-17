package hunoia.sideleap.system.api

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import hunoia.sideleap.IShizukuCommandService
import hunoia.sideleap.system.shizuku.ShizukuCommandService

data class PackageCommandResult(
    val success: Boolean,
    val packageName: String,
    val exitCode: Int = -1,
    val error: String? = null
)

data class EnablePackageResult(
    val success: Boolean,
    val packageName: String,
    val exitCode: Int = -1,
    val output: String = "",
    val error: String? = null
)

object ShizukuBinderExecutor {

    fun createArgs(context: Context, suffix: String) = UserServiceArgs(
        ComponentName(context.packageName, ShizukuCommandService::class.java.name)
    ).processNameSuffix(suffix).tag("sideleap-frozen-app-action")
        .version(1).debuggable(true).daemon(false)

    fun runWithBinder(
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

    fun parseFrozenActionResult(packageName: String, result: String): PackageCommandResult {
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

    fun parseLauncherResult(result: String, packageName: String): EnablePackageResult {
        val exitCode = result.lines()
            .firstOrNull { it.startsWith("exitCode=") }
            ?.removePrefix("exitCode=")
            ?.toIntOrNull() ?: -1
        val output = result.lines()
            .firstOrNull { it.startsWith("output=") }
            ?.removePrefix("output=") ?: ""
        return EnablePackageResult(
            success = exitCode == 0,
            packageName = packageName,
            exitCode = exitCode,
            output = output
        )
    }
}
