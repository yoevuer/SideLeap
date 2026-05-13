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
import hunoia.sideleap.action.ActionHandler
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.entity.Action
import hunoia.sideleap.ktx.gotoAppDetailSettings
import hunoia.sideleap.ktx.launchAssist
import hunoia.sideleap.utils.showToast
import hunoia.sideleap.utils.showVersionTooLowToast
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
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): Boolean {
        when (action.value) {
            GlobalActions.POWER_BUTTON -> context.service.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            GlobalActions.LOCK_SCREEN -> handleLockScreen(context)
            GlobalActions.FLASHLIGHT -> handleFlashlight(context)
            GlobalActions.SPLIT_SCREEN -> handleSplitScreen(context)
            GlobalActions.ASSIST_APP -> context.appContext.launchAssist()
            GlobalActions.SCREENSHOT -> handleScreenshot(context)
            else -> return false
        }
        return true
    }

    private fun handleLockScreen(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            showVersionTooLowToast(context.appContext, R.string.action_lock_screen)
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
                showToast(R.string.grant_camera_permission)
                PermissionUtils
                    .permission(Manifest.permission.CAMERA)
                    .callback { isAllGranted, _, _, deniedForever ->
                        if (isAllGranted) {
                            block()
                        } else if (deniedForever.isNotEmpty()) {
                            showToast(R.string.goto_grant_camera_permission)
                            context.appContext.gotoAppDetailSettings()
                        }
                    }
                    .request()
            }
        } else {
            showToast(R.string.flashlight_failed)
        }
    }

    private fun handleSplitScreen(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.service.performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        } else {
            showVersionTooLowToast(context.appContext, R.string.action_split_screen)
        }
    }

    private fun handleScreenshot(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.scope.launch {
                delay(500)
                context.service.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            }
        } else {
            showVersionTooLowToast(context.appContext, R.string.action_screenshot)
        }
    }
}
