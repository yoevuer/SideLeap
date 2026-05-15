package hunoia.sideleap.action.handlers

import android.os.Build
import com.blankj.utilcode.util.ScreenUtils
import hunoia.sideleap.R
import hunoia.sideleap.action.ActionExecutionResult
import hunoia.sideleap.action.ActionHandler
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.settings.model.ActionSettings
import hunoia.sideleap.system.accessibility.Accessibility
import hunoia.sideleap.core.serialization.JsonHelper
import hunoia.sideleap.action.MoveScreenData

object MoveScreenActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.MOVE_SCREEN,
        GlobalActions.BACK_TO_TOP,
        GlobalActions.GOTO_BOTTOM,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.MOVE_SCREEN -> handleMoveScreen(action, context)
            GlobalActions.BACK_TO_TOP -> handleBackToTop(context)
            GlobalActions.GOTO_BOTTOM -> handleGotoBottom(context)
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }

    private fun handleMoveScreen(action: Action, context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            context.showVersionTooLowToast(R.string.action_move_screen)
            return
        }
        if (!context.gestureSettings.longSlideTriggerImmediately) {
            context.showToast(context.appContext.getString(R.string.move_screen_disabled_cause_long_slide_trigger_immediately))
            return
        }
        val data = JsonHelper.decodeFromString<MoveScreenData>(action.data)
        if (data.x in 0..ScreenUtils.getScreenWidth() &&
            data.y in 0..ScreenUtils.getScreenHeight()
        ) {
            when (data.action) {
                ActionSettings.MoveScreen.Action.LongPress -> {
                    Accessibility.longPress(context.accessibilityService, data.x, data.y)
                }
                ActionSettings.MoveScreen.Action.DoubleTap -> {
                    Accessibility.doubleTap(context.accessibilityService, data.x, data.y)
                }
                ActionSettings.MoveScreen.Action.Tap -> {
                    Accessibility.click(context.accessibilityService, data.x, data.y)
                }
                else -> Unit
            }
        }
    }

    private fun handleBackToTop(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Accessibility.fastVerticalScroll(context.accessibilityService, true)
        } else {
            context.showVersionTooLowToast(R.string.action_back_to_top)
        }
    }

    private fun handleGotoBottom(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val strength = context.actionSettings.gotoBottom.strength
            Accessibility.fastVerticalScroll(context.accessibilityService, false, strength)
        } else {
            context.showVersionTooLowToast(R.string.action_goto_bottom)
        }
    }
}
