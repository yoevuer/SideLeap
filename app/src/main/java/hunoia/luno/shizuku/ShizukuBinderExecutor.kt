package hunoia.luno.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import hunoia.luno.IShizukuCommandService
import kotlinx.coroutines.runBlocking

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

@Deprecated("Use ShizukuManager directly. Will be removed in a future release.")
object ShizukuBinderExecutor {

    fun createArgs(context: Context, suffix: String) = UserServiceArgs(
        ComponentName(context.packageName, ShizukuCommandService::class.java.name)
    ).processNameSuffix(suffix).tag("sideleap-$suffix")
        .version(2).debuggable(true).daemon(false)

    fun runWithBinder(
        context: Context, packageName: String, suffix: String,
        call: (IShizukuCommandService) -> String
    ): PackageCommandResult {
        val args = createArgs(context, suffix)
        val latch = java.util.concurrent.CountDownLatch(1)
        val timedOut = java.util.concurrent.atomic.AtomicBoolean(false)
        var rawResult = ""

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                try {
                    val service = IShizukuCommandService.Stub.asInterface(binder)
                    rawResult = call(service)
                    try { service.destroy() } catch (_: Exception) {}
                } catch (e: Exception) {
                    rawResult = "error: ${e::class.simpleName} ${e.message}"
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
            rawResult = "error: ${e::class.simpleName} ${e.message}"
        } finally {
            try { Shizuku.unbindUserService(args, conn, true) } catch (_: Exception) {}
        }

        if (timedOut.get()) return PackageCommandResult(false, packageName, error = "timeout")
        val error = if (rawResult.startsWith("error:")) rawResult.removePrefix("error:").trim() else null
        val success = error == null && rawResult.contains("success=true")
        return PackageCommandResult(success = success, packageName = packageName, error = error)
    }

    @Deprecated("No longer needed with typed AIDL")
    fun parseFrozenActionResult(packageName: String, result: String): PackageCommandResult {
        return PackageCommandResult(result.startsWith("success"), packageName)
    }

    @Deprecated("No longer needed with typed AIDL")
    fun parseLauncherResult(result: String, packageName: String): EnablePackageResult {
        return EnablePackageResult(result.startsWith("success"), packageName)
    }

    fun runShellCommand(context: Context, command: String): ShellCommandResult {
        if (command.isBlank()) {
            return ShellCommandResult(false, -1, "", 0, error = "command is empty")
        }
        val result = runBlocking { ShizukuManager.executeShell(command) }
        return ShellCommandResult(
            success = result.isSuccess,
            exitCode = result.exitCode,
            output = result.stdout,
            elapsedMs = 0,
            error = result.errorMessage.ifBlank { null }
        )
    }
}
