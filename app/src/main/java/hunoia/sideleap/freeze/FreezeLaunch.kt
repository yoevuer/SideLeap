package hunoia.sideleap.freeze

import android.content.Context
import hunoia.sideleap.R
import hunoia.sideleap.launcher.launch.Launcher
import hunoia.sideleap.launcher.query.AppQuery
import hunoia.sideleap.system.api.showToast
import kotlinx.coroutines.delay

object FreezeLaunch {

    suspend fun launchWithAutoUnfreeze(
        context: Context,
        packageName: String,
        className: String,
        miniWindow: Boolean = false,
        unfreezePackage: suspend (context: Context, packageName: String) -> Boolean = { _, _ -> true }
    ): Boolean {
        if (FreezeState.isFrozen(context, packageName)) {
            val unfrozen = unfreezePackage(context, packageName)
            if (!unfrozen) {
                showToast(R.string.enable_frozen_app_failed)
                return false
            }
            FreezeState.invalidateFrozenCache()
            AppQuery.invalidateLauncherCache()
            delay(100)
        }
        return launchApp(context, packageName, className, miniWindow)
    }

    suspend fun launchActivityWithAutoUnfreeze(
        context: Context,
        packageName: String,
        className: String,
        unfreezePackage: suspend (context: Context, packageName: String) -> Boolean = { _, _ -> true }
    ): Boolean {
        if (FreezeState.isFrozen(context, packageName)) {
            val unfrozen = unfreezePackage(context, packageName)
            if (!unfrozen) {
                showToast(R.string.enable_frozen_app_failed)
                return false
            }
            FreezeState.invalidateFrozenCache()
            AppQuery.invalidateLauncherCache()
            delay(100)
        }
        return launchAppActivity(context, packageName, className)
    }

    private fun launchApp(context: Context, packageName: String, className: String, miniWindow: Boolean = false): Boolean {
        return Launcher.launchApp(context, packageName, className, miniWindow)
    }

    private fun launchAppActivity(context: Context, packageName: String, className: String): Boolean {
        return Launcher.launchAppActivity(context, packageName, className)
    }
}
