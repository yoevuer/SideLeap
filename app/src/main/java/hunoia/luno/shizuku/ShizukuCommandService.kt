package hunoia.luno.shizuku

import android.os.IBinder
import hunoia.luno.BuildConfig
import hunoia.luno.IShizukuCommandService
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

private val TAG = "ShizukuSvc"

private val smGetService by lazy {
    Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String::class.java)
}
private val pmStubAsInterface by lazy {
    Class.forName("android.content.pm.IPackageManager\$Stub").getDeclaredMethod("asInterface", IBinder::class.java)
}
private val pmClass by lazy { Class.forName("android.content.pm.IPackageManager") }
private val userHandleMyUserId by lazy {
    Class.forName("android.os.UserHandle").getDeclaredMethod("myUserId")
}
private val setEnabledSetting6 by lazy {
    runCatching {
        pmClass.getDeclaredMethod(
            "setApplicationEnabledSetting",
            String::class.java, Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
            String::class.java
        )
    }.getOrNull()
}
private val setEnabledSetting5 by lazy {
    runCatching {
        pmClass.getDeclaredMethod(
            "setApplicationEnabledSetting",
            String::class.java, Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
        )
    }.getOrNull()
}
private val getEnabledSetting by lazy {
    pmClass.getDeclaredMethod(
        "getApplicationEnabledSetting",
        String::class.java, Int::class.javaPrimitiveType
    )
}

class ShizukuCommandService : IShizukuCommandService.Stub() {

    override fun executeShellCommand(command: String): String {
        if (command.isBlank()) {
            return "error: Command is blank"
        }
        if (command.length > MAX_COMMAND_LENGTH) {
            return "error: Command too long (max $MAX_COMMAND_LENGTH)"
        }
        return try {
            val process = ProcessBuilder("sh", "-c", command).start()
            val stdoutCollector = StreamCollector(process.inputStream)
            val stderrCollector = StreamCollector(process.errorStream)
            val stdoutThread = stdoutCollector.start()
            val stderrThread = stderrCollector.start()
            val finished = waitFor(process, CMD_TIMEOUT_MS)
            if (!finished) {
                process.destroy()
            }
            stdoutThread.join(STREAM_TIMEOUT_MS)
            stderrThread.join(STREAM_TIMEOUT_MS)
            val stdout = stdoutCollector.content()
            val stderr = stderrCollector.content()
            if (!finished) {
                return "timedOut=true\nexitCode=-1\nstdout=$stdout\nstderr=$stderr"
            }
            "timedOut=false\nexitCode=${process.exitValue()}\nstdout=$stdout\nstderr=$stderr"
        } catch (t: Throwable) {
            "error: ${t.message ?: t.javaClass.simpleName}"
        }
    }

    override fun enablePackage(packageName: String): String {
        if (!isValidPackageName(packageName)) {
            return "error: invalid packageName=$packageName"
        }
        val direct = enablePackageDirect(packageName)
        if (direct != null) return "success=true"
        return enablePackageShell(packageName)
    }

    override fun disablePackage(packageName: String): String {
        if (!isValidPackageName(packageName)) {
            return "error: invalid packageName=$packageName"
        }
        return disablePackageDirect(packageName)
    }

    override fun enablePackageApi(packageName: String): String {
        if (!isValidPackageName(packageName)) {
            return "error: invalid packageName=$packageName"
        }
        val result = enablePackageDirect(packageName)
        return result ?: "error: enablePackageDirect failed"
    }

    override fun disablePackageApi(packageName: String): String {
        if (!isValidPackageName(packageName)) {
            return "error: invalid packageName=$packageName"
        }
        return disablePackageDirect(packageName)
    }

    override fun disablePackages(packageNames: MutableList<String>): MutableList<String> {
        return packageNames.map { pkg ->
            if (!isValidPackageName(pkg)) "error: invalid packageName=$pkg"
            else disablePackageDirect(pkg)
        }.toMutableList()
    }

    override fun enablePackages(packageNames: MutableList<String>): MutableList<String> {
        return packageNames.map { pkg ->
            if (!isValidPackageName(pkg)) "error: invalid packageName=$pkg"
            else enablePackageDirect(pkg) ?: enablePackageShell(pkg)
        }.toMutableList()
    }

    override fun listDisabledPackageNames(): MutableList<String> {
        return try {
            val process = ProcessBuilder("pm", "list", "packages", "-d")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode != 0) return mutableListOf()
            output.lines()
                .filter { it.startsWith("package:") }
                .map { it.removePrefix("package:") }
                .filter { it.isNotBlank() }
                .sorted()
                .toMutableList()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    override fun destroy() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun enablePackageDirect(packageName: String): String? {
        return try {
            val binder = smGetService.invoke(null, "package") as IBinder
            val pm = pmStubAsInterface.invoke(null, binder)
            val userId = userHandleMyUserId.invoke(null) as Int

            val setEnabled = setEnabledSetting6 ?: setEnabledSetting5 ?: return null
            val callArgs: Array<Any> = if (setEnabled.parameterCount == 5) {
                arrayOf(packageName, 0, 0, userId, "com.android.shell")
            } else {
                arrayOf(packageName, 0, 0, userId)
            }
            setEnabled.invoke(pm, *callArgs)

            val newState = getEnabledSetting.invoke(pm, packageName, userId) as Int
            if (newState != 3) "success=true" else null
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                android.util.Log.d(TAG, "enablePackageDirect failed: ${e.message}")
            }
            null
        }
    }

    private fun enablePackageShell(packageName: String): String {
        return try {
            val process = ProcessBuilder("pm", "enable", packageName)
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()
            "success=${exitCode == 0}"
        } catch (e: Exception) {
            "error: ${e.message ?: e.javaClass.simpleName}"
        }
    }

    private fun disablePackageDirect(packageName: String): String {
        return try {
            val binder = smGetService.invoke(null, "package") as IBinder
            val pm = pmStubAsInterface.invoke(null, binder)
            val userId = userHandleMyUserId.invoke(null) as Int

            val setEnabled = setEnabledSetting6 ?: setEnabledSetting5 ?: return "error: setApplicationEnabledSetting not found"
            val callArgs: Array<Any> = if (setEnabled.parameterCount == 5) {
                arrayOf(packageName, 3, 0, userId, "com.android.shell")
            } else {
                arrayOf(packageName, 3, 0, userId)
            }
            setEnabled.invoke(pm, *callArgs)
            "success=true"
        } catch (e: Exception) {
            "error: ${e.message ?: e.javaClass.simpleName}"
        }
    }

    private fun isValidPackageName(name: String): Boolean {
        return Regex("^[A-Za-z0-9_.]+$").matches(name)
    }

    private fun waitFor(process: Process, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            try {
                process.exitValue()
                return true
            } catch (_: IllegalThreadStateException) {
                Thread.sleep(50L)
            }
        }
        return false
    }

    private class StreamCollector(inputStream: InputStream) {
        private val reader = BufferedReader(InputStreamReader(inputStream))
        private val output = StringBuilder()

        fun start(): Thread {
            return Thread {
                reader.useLines { lines ->
                    lines.forEach { line ->
                        if (output.isNotEmpty()) {
                            output.append("\n")
                        }
                        append(line)
                    }
                }
            }.apply { start() }
        }

        fun content(): String = output.toString()

        private fun append(value: String) {
            if (output.length >= MAX_OUTPUT_LENGTH) return
            val remaining = MAX_OUTPUT_LENGTH - output.length
            output.append(value.take(remaining))
        }
    }

    companion object {
        private const val CMD_TIMEOUT_MS = 10_000L
        private const val STREAM_TIMEOUT_MS = 300L
        private const val MAX_OUTPUT_LENGTH = 4096
        private const val MAX_COMMAND_LENGTH = 2000
    }
}
