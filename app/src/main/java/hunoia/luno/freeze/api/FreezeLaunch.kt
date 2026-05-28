package hunoia.luno.freeze.api

import android.content.Context
import hunoia.luno.R
import hunoia.luno.launcher.launch.Launcher
import hunoia.luno.launcher.query.AppQuery
import hunoia.luno.system.feedback.showToast
import kotlinx.coroutines.delay

object FreezeLaunch {

    suspend fun launchWithAutoUnfreeze(
        context: Context,
        packageName: String,
        className: String,
        miniWindow: Boolean = false,
        miniWindowHorizontalBias: Float = 0f,
        miniWindowVerticalBias: Float = 0f,
        miniWindowVerticalOffsetFraction: Float = 0f,
        miniWindowWidthFraction: Float = 0.46f,
        miniWindowHeightFraction: Float = 0.74f,
        miniWindowOverrideBounds: Boolean = false,
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
        return launchApp(
            context, packageName, className, miniWindow,
            miniWindowHorizontalBias, miniWindowVerticalBias,
            miniWindowVerticalOffsetFraction,
            miniWindowWidthFraction, miniWindowHeightFraction,
            miniWindowOverrideBounds,
        )
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

    private fun launchApp(
        context: Context,
        packageName: String,
        className: String,
        miniWindow: Boolean = false,
        miniWindowHorizontalBias: Float = 0f,
        miniWindowVerticalBias: Float = 0f,
        miniWindowVerticalOffsetFraction: Float = 0f,
        miniWindowWidthFraction: Float = 0.46f,
        miniWindowHeightFraction: Float = 0.74f,
        miniWindowOverrideBounds: Boolean = false,
    ): Boolean {
        return Launcher.launchApp(
            context, packageName, className, miniWindow,
            miniWindowHorizontalBias, miniWindowVerticalBias,
            miniWindowVerticalOffsetFraction,
            miniWindowWidthFraction, miniWindowHeightFraction,
            overrideBounds = miniWindowOverrideBounds,
        )
    }

    private fun launchAppActivity(context: Context, packageName: String, className: String): Boolean {
        return Launcher.launchAppActivity(context, packageName, className)
    }
}
