package hunoia.luno.shizuku

import android.content.Context
import kotlinx.coroutines.runBlocking

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

@Deprecated("Use ShizukuManager directly")
object ShizukuCommand {

    fun disablePackage(context: Context, packageName: String): PackageCommandResult {
        val result = runBlocking { ShizukuManager.disablePackage(packageName) }
        return PackageCommandResult(
            success = result.success,
            packageName = packageName,
            error = result.errorMessage.ifBlank { null }
        )
    }

    fun enablePackage(context: Context, packageName: String): PackageCommandResult {
        val result = runBlocking { ShizukuManager.enablePackage(packageName) }
        return PackageCommandResult(
            success = result.success,
            packageName = packageName,
            error = result.errorMessage.ifBlank { null }
        )
    }

    fun executeBatch(context: Context, packageNames: List<String>, disable: Boolean): BatchFrozenResult {
        return runBlocking { ShizukuManager.executeBatch(packageNames, disable) }
    }

    fun enablePackageForLauncher(context: Context, packageName: String): EnablePackageResult {
        val result = runBlocking { ShizukuManager.enablePackage(packageName) }
        return EnablePackageResult(
            success = result.success,
            packageName = packageName,
            error = result.errorMessage.ifBlank { null }
        )
    }

    fun clearEnableServiceCache() {}
}
