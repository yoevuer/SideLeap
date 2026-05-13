package hunoia.sideleap.action.handlers

import android.view.KeyEvent
import hunoia.sideleap.action.ActionHandler
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.entity.Action
import hunoia.sideleap.ktx.dispatchMediaKeyEvent
import hunoia.sideleap.ktx.toggleMute
import hunoia.sideleap.ktx.volumeDown
import hunoia.sideleap.ktx.volumeUp

object MediaActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.VOLUME_UP,
        GlobalActions.VOLUME_DOWN,
        GlobalActions.MUTE,
        GlobalActions.PLAY_PAUSE_SONG,
        GlobalActions.LAST_SONG,
        GlobalActions.NEXT_SONG,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): Boolean {
        when (action.value) {
            GlobalActions.VOLUME_UP -> context.appContext.volumeUp()
            GlobalActions.VOLUME_DOWN -> context.appContext.volumeDown()
            GlobalActions.MUTE -> context.appContext.toggleMute()
            GlobalActions.PLAY_PAUSE_SONG -> context.appContext.dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            GlobalActions.LAST_SONG -> context.appContext.dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            GlobalActions.NEXT_SONG -> context.appContext.dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
            else -> return false
        }
        return true
    }
}
