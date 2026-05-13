package hunoia.sideleap.utils

import android.os.IBinder
import hunoia.sideleap.BuildConfig
import hunoia.sideleap.IShizukuCommandService

class ShizukuCommandService : IShizukuCommandService.Stub() {

    private val startTime = System.currentTimeMillis()

    override fun listDisabledPackages(): String {
        return try {
            val process = ProcessBuilder("pm", "list", "packages", "-d")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            val elapsed = System.currentTimeMillis() - startTime
            "service: elapsed=${elapsed}ms\ncommand=pm list packages -d\nexitCode=$exitCode\nstdout=$output"
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            "service: elapsed=${elapsed}ms\nerror: ${e::class.simpleName} ${e.message}"
        }
    }

    override fun listDisabledPackageNames(): List<String> {
        return try {
            val process = ProcessBuilder("pm", "list", "packages", "-d")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode != 0) return emptyList()
            output.lines()
                .filter { it.startsWith("package:") }
                .map { it.removePrefix("package:") }
                .filter { it.isNotBlank() }
                .sorted()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun enablePackage(packageName: String): String {
        val startTime = System.currentTimeMillis()
        if (!Regex("^[A-Za-z0-9_.]+$").matches(packageName)) {
            val elapsed = System.currentTimeMillis() - startTime
            return "service: elapsed=${elapsed}ms\nerror: invalid packageName=$packageName"
        }

        val directResult = enablePackageDirect(packageName, startTime)
        if (directResult != null) return directResult

        return enablePackageShell(packageName, startTime)
    }

    override fun disablePackage(packageName: String): String {
        val startTime = System.currentTimeMillis()
        if (!Regex("^[A-Za-z0-9_.]+$").matches(packageName)) {
            val elapsed = System.currentTimeMillis() - startTime
            return "service: elapsed=${elapsed}ms\nerror: invalid packageName=$packageName"
        }
        return disablePackageShell(packageName, startTime)
    }

    private fun enablePackageDirect(packageName: String, overallStart: Long): String? {
        val apiStart = System.currentTimeMillis()
        try {
            if (BuildConfig.DEBUG) {
                android.util.Log.d("LauncherPerf", "enable_package: api_start pkg=$packageName")
            }

            val smClass = Class.forName("android.os.ServiceManager")
            val getService = smClass.getDeclaredMethod("getService", String::class.java)
            val binder = getService.invoke(null, "package") as IBinder

            val pmStubClass = Class.forName("android.content.pm.IPackageManager\$Stub")
            val asInterface = pmStubClass.getDeclaredMethod("asInterface", IBinder::class.java)
            val pm = asInterface.invoke(null, binder)
            val pmClass = Class.forName("android.content.pm.IPackageManager")

            val uhClass = Class.forName("android.os.UserHandle")
            val myUserId = uhClass.getDeclaredMethod("myUserId")
            val userId = myUserId.invoke(null) as Int

            val setEnabled = try {
                pmClass.getDeclaredMethod(
                    "setApplicationEnabledSetting",
                    String::class.java, Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
                    String::class.java
                )
            } catch (_: NoSuchMethodException) {
                pmClass.getDeclaredMethod(
                    "setApplicationEnabledSetting",
                    String::class.java, Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
                )
            }
            val callArgs: Array<Any> = if (setEnabled.parameterCount == 5) {
                arrayOf(packageName, 0, 0, userId, "com.android.shell")
            } else {
                arrayOf(packageName, 0, 0, userId)
            }
            setEnabled.invoke(pm, *callArgs)

            val getEnabled = pmClass.getDeclaredMethod(
                "getApplicationEnabledSetting",
                String::class.java, Int::class.javaPrimitiveType
            )
            val newState = getEnabled.invoke(pm, packageName, userId) as Int

            val apiElapsed = System.currentTimeMillis() - apiStart
            if (BuildConfig.DEBUG) {
                android.util.Log.d("LauncherPerf",
                    "enable_package: api_done pkg=$packageName newState=$newState elapsed=${apiElapsed}ms")
            }

            if (newState != 3) {
                return "service: elapsed=${apiElapsed}ms\ndirect_api=true\n" +
                       "command=android enable $packageName\n" +
                       "exitCode=0\n" +
                       "output=Package $packageName new state=$newState"
            }
        } catch (e: Exception) {
            val apiElapsed = System.currentTimeMillis() - apiStart
            if (BuildConfig.DEBUG) {
                android.util.Log.d("LauncherPerf",
                    "enable_package: api_fail pkg=$packageName " +
                    "error=${e::class.simpleName} ${e.message} elapsed=${apiElapsed}ms")
            }
        }
        return null
    }

    private fun enablePackageShell(packageName: String, overallStart: Long): String {
        val shellStart = System.currentTimeMillis()
        if (BuildConfig.DEBUG) {
            android.util.Log.d("LauncherPerf", "enable_package: shell_start pkg=$packageName")
        }
        try {
            val process = ProcessBuilder("pm", "enable", packageName)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            val shellElapsed = System.currentTimeMillis() - shellStart
            val totalElapsed = System.currentTimeMillis() - overallStart
            if (BuildConfig.DEBUG) {
                android.util.Log.d("LauncherPerf",
                    "enable_package: shell_done pkg=$packageName " +
                    "exitCode=$exitCode elapsed=${shellElapsed}ms total=${totalElapsed}ms")
            }
            return "service: elapsed=${totalElapsed}ms\ndirect_api=false\n" +
                   "command=pm enable $packageName\nexitCode=$exitCode\noutput=$output"
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - overallStart
            return "service: elapsed=${elapsed}ms\nerror: ${e::class.simpleName} ${e.message}"
        }
    }

    private fun disablePackageShell(packageName: String, overallStart: Long): String {
        try {
            val process = ProcessBuilder("pm", "disable-user", "--user", "0", packageName)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            val elapsed = System.currentTimeMillis() - overallStart
            return "service: elapsed=${elapsed}ms\n" +
                "command=pm disable-user --user 0 $packageName\nexitCode=$exitCode\noutput=$output"
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - overallStart
            return "service: elapsed=${elapsed}ms\nerror: ${e::class.simpleName} ${e.message}"
        }
    }

    override fun destroy() {
        Thread {
            Thread.sleep(100)
            kotlin.system.exitProcess(0)
        }.start()
    }
}
