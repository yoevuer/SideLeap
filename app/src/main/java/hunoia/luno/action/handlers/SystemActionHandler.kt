package hunoia.luno.action.handlers

import android.Manifest
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_POWER_DIALOG
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import hunoia.luno.R
import hunoia.luno.action.api.ActionExecutionResult
import hunoia.luno.action.api.ActionHandler
import hunoia.luno.action.api.ActionHandlerContext
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.Action
import hunoia.luno.system.intent.launchAssist
import hunoia.luno.system.intent.gotoAppDetailSettings
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
        GlobalActions.HIDE_GESTURE_BUTTON,
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
            GlobalActions.HIDE_GESTURE_BUTTON -> context.hideGestureButton(context.actionSettings.hideGestureButton.delayMs)
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

    private var flashlightOn = false

    private fun handleFlashlight(context: ActionHandlerContext) {
        try {
            val cameraManager = context.appContext.getSystemService(android.content.Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                val block = {
                    context.scope.launch(Dispatchers.Default) {
                        flashlightOn = !flashlightOn
                        cameraManager.setTorchMode(cameraId, flashlightOn)
                    }
                }
                if (ContextCompat.checkSelfPermission(
                        context.appContext, Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    block()
                } else {
                    context.showToast(context.appContext.getString(R.string.grant_camera_permission))
                    context.showToast(context.appContext.getString(R.string.goto_grant_camera_permission))
                    context.appContext.gotoAppDetailSettings()
                }
            } else {
                context.showToast(context.appContext.getString(R.string.flashlight_failed))
            }
        } catch (e: Exception) {
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
                delay(200)
                context.accessibilityService.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            }
        } else {
            context.showVersionTooLowToast(R.string.action_screenshot)
        }
    }

}
