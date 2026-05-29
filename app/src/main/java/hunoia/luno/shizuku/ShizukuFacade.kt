package hunoia.luno.shizuku

import android.content.Context

object ShizukuFacade {

    fun isReady(): Boolean = ShizukuRuntime.isAvailable() && ShizukuRuntime.checkPermission()

    fun runShellCommand(context: Context, command: String): ShellCommandResult =
        ShizukuBinderExecutor.runShellCommand(context, command)
}
