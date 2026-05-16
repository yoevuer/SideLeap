package hunoia.sideleap.action.handlers

import android.Manifest
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_POWER_DIALOG
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN
import android.os.Build
import com.blankj.utilcode.util.FlashlightUtils
import com.blankj.utilcode.util.PermissionUtils
import hunoia.sideleap.R
import hunoia.sideleap.action.api.ActionExecutionResult
import hunoia.sideleap.action.api.ActionHandler
import hunoia.sideleap.action.api.ActionHandlerContext
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.system.api.launchAssist
import hunoia.sideleap.system.api.gotoAppDetailSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SystemActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.POWER_BUTTON,
        GlobalActions.LOCK_SCREEN,
        GlobalActions.FLASHLIGHT,
        GlobalActions.SPLIT_SCREEN,
        GlobalActions.ASSIST_APP,
        GlobalActions.SCREENSHOT,
        GlobalActions.KEEP_SCREEN_ON,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.POWER_BUTTON -> context.accessibilityService.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            GlobalActions.LOCK_SCREEN -> handleLockScreen(context)
            GlobalActions.FLASHLIGHT -> handleFlashlight(context)
            GlobalActions.SPLIT_SCREEN -> handleSplitScreen(context)
            GlobalActions.ASSIST_APP -> context.appContext.launchAssist()
            GlobalActions.SCREENSHOT -> handleScreenshot(context)
            GlobalActions.KEEP_SCREEN_ON -> context.toggleKeepScreenOn()
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }

    private fun handleLockScreen(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.accessibilityService.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            context.showVersionTooLowToast(R.string.action_lock_screen)
        }
    }

    private fun handleFlashlight(context: ActionHandlerContext) {
        if (FlashlightUtils.isFlashlightEnable()) {
            val block = {
                context.scope.launch(Dispatchers.Default) {
                    val turnOn = !FlashlightUtils.isFlashlightOn()
                    if (turnOn) {
                        FlashlightUtils.setFlashlightStatus(true)
                    } else {
                        FlashlightUtils.setFlashlightStatus(false)
                        FlashlightUtils.destroy()
                    }
                }
            }
            if (PermissionUtils.isGranted(Manifest.permission.CAMERA)) {
                block()
            } else {
                context.showToast(context.appContext.getString(R.string.grant_camera_permission))
                PermissionUtils
                    .permission(Manifest.permission.CAMERA)
                    .callback { isAllGranted, _, _, deniedForever ->
                        if (isAllGranted) {
                            block()
                        } else if (deniedForever.isNotEmpty()) {
                            context.showToast(context.appContext.getString(R.string.goto_grant_camera_permission))
                            context.appContext.gotoAppDetailSettings()
                        }
                    }
                    .request()
            }
        } else {
            context.showToast(context.appContext.getString(R.string.flashlight_failed))
        }
    }

    private fun handleSplitScreen(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.accessibilityService.performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        } else {
            context.showVersionTooLowToast(R.string.action_split_screen)
        }
    }

    private fun handleScreenshot(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.scope.launch {
                delay(500)
                context.accessibilityService.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            }
        } else {
            context.showVersionTooLowToast(R.string.action_screenshot)
        }
    }
}
