package hunoia.luno.action.handlers

import android.view.KeyEvent
import hunoia.luno.action.api.ActionExecutionResult
import hunoia.luno.action.api.ActionHandler
import hunoia.luno.action.api.ActionHandlerContext
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.Action
import hunoia.luno.bridge.dispatchMediaKeyEvent
import hunoia.luno.bridge.toggleMute
import hunoia.luno.bridge.volumeDown
import hunoia.luno.bridge.volumeUp

object MediaActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.VOLUME_UP,
        GlobalActions.VOLUME_DOWN,
        GlobalActions.MUTE,
        GlobalActions.PLAY_PAUSE_SONG,
        GlobalActions.LAST_SONG,
        GlobalActions.NEXT_SONG,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.VOLUME_UP -> context.appContext.volumeUp()
            GlobalActions.VOLUME_DOWN -> context.appContext.volumeDown()
            GlobalActions.MUTE -> context.appContext.toggleMute()
            GlobalActions.PLAY_PAUSE_SONG -> context.appContext.dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            GlobalActions.LAST_SONG -> context.appContext.dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            GlobalActions.NEXT_SONG -> context.appContext.dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }
}
