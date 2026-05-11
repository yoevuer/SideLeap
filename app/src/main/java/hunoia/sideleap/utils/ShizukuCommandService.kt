package hunoia.sideleap.utils

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
        return try {
            val process = ProcessBuilder("pm", "enable", packageName)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            val elapsed = System.currentTimeMillis() - startTime
            "service: elapsed=${elapsed}ms\ncommand=pm enable $packageName\nexitCode=$exitCode\noutput=$output"
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            "service: elapsed=${elapsed}ms\nerror: ${e::class.simpleName} ${e.message}"
        }
    }

    override fun destroy() {
        Thread {
            Thread.sleep(100)
            kotlin.system.exitProcess(0)
        }.start()
    }
}