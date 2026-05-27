package hunoia.luno.action.handlers

import hunoia.luno.core.DensityProvider
import hunoia.luno.action.api.ActionExecutionResult
import hunoia.luno.action.api.ActionHandler
import hunoia.luno.action.api.ActionHandlerContext
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.Action
import hunoia.luno.settings.model.ActionSettings
import hunoia.luno.system.accessibility.Accessibility
import hunoia.luno.core.serialization.JsonHelper
import hunoia.luno.action.MoveScreenData
import hunoia.luno.action.runtimeTouchPosition
import kotlinx.coroutines.delay

object MoveScreenActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.MOVE_SCREEN,
        GlobalActions.BACK_TO_TOP,
        GlobalActions.GOTO_BOTTOM,
        GlobalActions.CLICK_CURRENT_POSITION,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.MOVE_SCREEN -> handleMoveScreen(action, context)
            GlobalActions.BACK_TO_TOP -> handleBackToTop(context)
            GlobalActions.GOTO_BOTTOM -> handleGotoBottom(context)
            GlobalActions.CLICK_CURRENT_POSITION -> handleClickCurrentPosition(action, context)
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }

    private fun handleMoveScreen(action: Action, context: ActionHandlerContext) {
        val data = JsonHelper.decodeFromString<MoveScreenData>(action.data)
        if (data.x in 0..DensityProvider.screenWidthPx &&
            data.y in 0..DensityProvider.screenHeightPx
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
        Accessibility.fastVerticalScroll(context.accessibilityService, true)
    }

    private fun handleGotoBottom(context: ActionHandlerContext) {
        val strength = context.actionSettings.gotoBottom.strength
        Accessibility.fastVerticalScroll(context.accessibilityService, false, strength)
    }

    private suspend fun handleClickCurrentPosition(action: Action, context: ActionHandlerContext) {
        val (x, y) = action.runtimeTouchPosition() ?: return
        if (x in 0..DensityProvider.screenWidthPx && y in 0..DensityProvider.screenHeightPx) {
            context.hideGestureButton(250L)
            delay(80)
            Accessibility.click(context.accessibilityService, x, y)
        }
    }
}
