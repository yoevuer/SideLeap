package hunoia.luno.action.handlers

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import hunoia.luno.R
import hunoia.luno.action.api.ActionExecutionResult
import hunoia.luno.action.api.ActionHandler
import hunoia.luno.action.api.ActionHandlerContext
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.Action
import hunoia.luno.launcher.model.OpenAppOrUrlData
import hunoia.luno.freeze.api.FreezeLaunch
import hunoia.luno.action.appInfo
import hunoia.luno.launcher.launch.Launcher
import hunoia.luno.system.packages.queryIntentActivitiesCompat
import hunoia.luno.core.serialization.JsonHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AppLaunchActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.EXTRA_LAUNCH_APP,
        GlobalActions.OPEN_APP_ACTIVITY,
        GlobalActions.OPEN_URL,
        GlobalActions.QUICK_APP_LAUNCHER,
        GlobalActions.POPUP_SCREEN,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.EXTRA_LAUNCH_APP -> handleExtraLaunchApp(action, context)
            GlobalActions.OPEN_APP_ACTIVITY -> handleOpenAppActivity(action, context)
            GlobalActions.OPEN_URL -> handleOpenUrl(action, context)
            GlobalActions.QUICK_APP_LAUNCHER -> context.toggleQuickAppLauncher()
            GlobalActions.POPUP_SCREEN -> handlePopupScreen(context)
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }

    private fun handlePopupScreen(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val pkgName = context.accessibilityService
                .rootInActiveWindow?.packageName?.toString()
                ?: context.currentPackageName()
            if (context.nowInLauncher() || pkgName.isNullOrEmpty()) {
                return
            }
            val intent = Intent().apply {
                setPackage(pkgName)
                setAction(Intent.ACTION_MAIN)
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfo = context.appContext.packageManager
                .queryIntentActivitiesCompat(intent, PackageManager.MATCH_ALL)
                .firstOrNull()
            val className = resolveInfo?.activityInfo?.name
            if (!className.isNullOrEmpty()) {
                Launcher.launchAppInPopup(
                    context.appContext,
                    pkgName,
                    className,
                    context.advancedSettings.miniWindowHorizontalBias,
                    context.advancedSettings.miniWindowVerticalBias,
                    context.advancedSettings.miniWindowVerticalEdgeMarginFraction,
                    context.advancedSettings.miniWindowVerticalOffsetFraction,
                )
            }
        } else {
            context.showVersionTooLowToast(R.string.action_popup_screen)
        }
    }

    private fun handleExtraLaunchApp(action: Action, context: ActionHandlerContext) {
        val appInfo = action.appInfo ?: return
        launchAppWithFrozenSupport(context, appInfo, appInfo.miniWindow)
    }

    private fun handleOpenAppActivity(action: Action, context: ActionHandlerContext) {
        val data = try {
            JsonHelper.decodeFromString<OpenAppOrUrlData>(action.data)
        } catch (e: Exception) {
            null
        }
        if (data != null && data.packageName.isNotBlank() && data.activityClassName.isNotBlank()) {
            context.scope.launch {
                FreezeLaunch.launchActivityWithAutoUnfreeze(
                    context = context.appContext,
                    packageName = data.packageName,
                    className = data.activityClassName
                ) { _, pkg ->
                    suspendEnablePackageViaBridge(context.requestEnableFrozenPackage, pkg)
                }
            }
        }
    }

    private fun handleOpenUrl(action: Action, context: ActionHandlerContext) {
        val data = try {
            JsonHelper.decodeFromString<OpenAppOrUrlData>(action.data)
        } catch (e: Exception) {
            null
        }
        if (data != null && data.url.isNotBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(data.url)).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.appContext.startActivity(intent)
            } catch (_: Exception) { }
        }
    }

    private fun launchAppWithFrozenSupport(
        context: ActionHandlerContext,
        appInfo: hunoia.luno.launcher.model.AppInfo,
        miniWindow: Boolean
    ) {
        context.scope.launch {
            FreezeLaunch.launchWithAutoUnfreeze(
                context = context.appContext,
                packageName = appInfo.packageName,
                className = appInfo.className,
                miniWindow = miniWindow,
                miniWindowHorizontalBias = context.advancedSettings.miniWindowHorizontalBias,
                miniWindowVerticalBias = context.advancedSettings.miniWindowVerticalBias,
                miniWindowVerticalEdgeMarginFraction = context.advancedSettings.miniWindowVerticalEdgeMarginFraction,
                miniWindowVerticalOffsetFraction = context.advancedSettings.miniWindowVerticalOffsetFraction,
            ) { _, pkg ->
                suspendEnablePackageViaBridge(context.requestEnableFrozenPackage, pkg)
            }
        }
    }

    private suspend fun suspendEnablePackageViaBridge(
        requestEnableFrozenPackage: (String, (Boolean) -> Unit) -> Unit,
        packageName: String
    ): Boolean = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        requestEnableFrozenPackage(packageName) { success ->
            cont.resume(success)
        }
    }
}
