package hunoia.sideleap.action.handlers

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import hunoia.sideleap.R
import hunoia.sideleap.action.api.ActionExecutionResult
import hunoia.sideleap.action.api.ActionHandler
import hunoia.sideleap.action.api.ActionHandlerContext
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.launcher.model.OpenAppOrUrlData
import hunoia.sideleap.freeze.api.FreezeLaunch
import hunoia.sideleap.action.appInfo
import hunoia.sideleap.action.TriggerType
import hunoia.sideleap.launcher.launch.Launcher
import hunoia.sideleap.system.api.queryIntentActivitiesCompat
import hunoia.sideleap.action.runtimeTriggerType
import hunoia.sideleap.core.serialization.JsonHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AppLaunchActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.EXTRA_LAUNCH_APP,
        GlobalActions.OPEN_APP_OR_URL,
        GlobalActions.QUICK_APP_LAUNCHER,
        GlobalActions.POPUP_SCREEN,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.EXTRA_LAUNCH_APP -> handleExtraLaunchApp(action, context)
            GlobalActions.OPEN_APP_OR_URL -> handleOpenAppOrUrl(action, context)
            GlobalActions.QUICK_APP_LAUNCHER -> context.toggleQuickAppLauncher()
            GlobalActions.POPUP_SCREEN -> handlePopupScreen(context)
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }

    private fun handlePopupScreen(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val curPkgName = context.currentPackageName()
            if (context.nowInLauncher() || curPkgName.isNullOrEmpty()) {
                return
            }
            val intent = Intent().apply {
                setPackage(curPkgName)
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
                    curPkgName,
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
        val advancedSettings = context.advancedSettings
        val appInfo = action.appInfo
        if (appInfo != null) {
            val longPressLaunchPopup = advancedSettings.actionPanelAppLongPressLaunchPopup
            val triggerType = action.runtimeTriggerType()
            val miniWindow = triggerType?.let {
                when (it) {
                    TriggerType.Press -> !longPressLaunchPopup
                    TriggerType.LongPress -> longPressLaunchPopup
                }
            } ?: appInfo.miniWindow
            launchAppWithFrozenSupport(context, appInfo, miniWindow)
        }
    }

    private fun handleOpenAppOrUrl(action: Action, context: ActionHandlerContext) {
        val data = try {
            JsonHelper.decodeFromString<OpenAppOrUrlData>(action.data)
        } catch (e: Exception) {
            null
        }
        if (data != null) {
            when (data.type) {
                OpenAppOrUrlData.TYPE_ACTIVITY -> {
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
                else -> Launcher.launchOpenAppOrUrl(context.appContext, data)
            }
        }
    }

    private fun launchAppWithFrozenSupport(
        context: ActionHandlerContext,
        appInfo: hunoia.sideleap.launcher.model.AppInfo,
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
