package hunoia.sideleap.action.handlers

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import hunoia.sideleap.R
import hunoia.sideleap.action.ActionExecutionResult
import hunoia.sideleap.action.ActionHandler
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.OpenAppOrUrlData
import hunoia.sideleap.freeze.FreezeLaunch
import hunoia.sideleap.ktx.appInfo
import hunoia.sideleap.ktx.isMiniWindow
import hunoia.sideleap.ktx.launchAppInPopup
import hunoia.sideleap.ktx.launchOpenAppOrUrl
import hunoia.sideleap.system.packages.queryIntentActivitiesCompat
import hunoia.sideleap.ui.widget.ActionPanelState
import hunoia.sideleap.utils.JsonHelper
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
            GlobalActions.QUICK_APP_LAUNCHER -> context.service.quickAppLauncherOverlay.toggle()
            GlobalActions.POPUP_SCREEN -> handlePopupScreen(context)
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }

    private fun handlePopupScreen(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val curPkgName = context.currentPackageName()
            if (context.service.nowInLauncher() || curPkgName.isNullOrEmpty()) {
                return
            }
            val intent = Intent().apply {
                setPackage(curPkgName)
                setAction(Intent.ACTION_MAIN)
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfo = context.service.packageManager
                .queryIntentActivitiesCompat(intent, PackageManager.MATCH_ALL)
                .firstOrNull()
            val className = resolveInfo?.activityInfo?.name
            if (!className.isNullOrEmpty()) {
                context.appContext.launchAppInPopup(curPkgName, className)
            }
        } else {
            context.showVersionTooLowToast(R.string.action_popup_screen)
        }
    }

    private fun handleExtraLaunchApp(action: Action, context: ActionHandlerContext) {
        val advancedSettings = context.service.advancedSettings ?: return
        val appInfo = action.appInfo
        if (appInfo != null) {
            val longPressLaunchPopup = advancedSettings.actionPanelAppLongPressLaunchPopup
            val triggerType = action.extra as? ActionPanelState.TriggerType
            val miniWindow = triggerType?.isMiniWindow(longPressLaunchPopup) ?: appInfo.miniWindow
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
                            suspendEnablePackageViaBridge(context.service, pkg)
                        }
                    }
                }
                else -> context.appContext.launchOpenAppOrUrl(data)
            }
        }
    }

    private fun launchAppWithFrozenSupport(
        context: ActionHandlerContext,
        appInfo: hunoia.sideleap.entity.AppInfo,
        miniWindow: Boolean
    ) {
        context.scope.launch {
            FreezeLaunch.launchWithAutoUnfreeze(
                context = context.appContext,
                packageName = appInfo.packageName,
                className = appInfo.className,
                miniWindow = miniWindow
            ) { _, pkg ->
                suspendEnablePackageViaBridge(context.service, pkg)
            }
        }
    }

    private suspend fun suspendEnablePackageViaBridge(
        service: hunoia.sideleap.SideGestureService,
        packageName: String
    ): Boolean = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        service.requestEnableFrozenPackage(packageName) { success ->
            cont.resume(success)
        }
    }
}
