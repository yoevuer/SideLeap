package hunoia.luno.shizuku

data class ShellCommandResult(
    val success: Boolean,
    val exitCode: Int,
    val output: String,
    val elapsedMs: Long,
    val error: String? = null,
)

internal fun parseShellCommandResult(raw: String): ShellCommandResult {
    val lines = raw.lines()
    val error = lines.firstOrNull { it.startsWith("error:") }
        ?.removePrefix("error:")
        ?.trim()
    if (error != null) {
        return ShellCommandResult(false, -1, "", 0, error = error)
    }
    val elapsed = lines.firstOrNull { it.startsWith("elapsed=") }
        ?.removePrefix("elapsed=")
        ?.removeSuffix("ms")
        ?.toLongOrNull() ?: 0L
    val exitCode = lines.firstOrNull { it.startsWith("exitCode=") }
        ?.removePrefix("exitCode=")
        ?.toIntOrNull() ?: -1
    val stdout = raw.substringAfter("\nstdout=", missingDelimiterValue = "")
    return ShellCommandResult(
        success = exitCode == 0,
        exitCode = exitCode,
        output = stdout,
        elapsedMs = elapsed,
    )
}
