package hunoia.sideleap.action

import hunoia.sideleap.BuildConfig
import hunoia.sideleap.action.handlers.AppLaunchActionHandler
import hunoia.sideleap.action.handlers.MediaActionHandler
import hunoia.sideleap.action.handlers.MoveScreenActionHandler
import hunoia.sideleap.action.handlers.NavigationActionHandler
import hunoia.sideleap.action.handlers.RandomNameActionHandler
import hunoia.sideleap.action.handlers.ShortcutActionHandler
import hunoia.sideleap.action.handlers.SystemActionHandler
import hunoia.sideleap.entity.Action

object ActionRegistry {
    private val handlers: List<ActionHandler> = listOf(
        NavigationActionHandler,
        MediaActionHandler,
        SystemActionHandler,
        RandomNameActionHandler,
        MoveScreenActionHandler,
        AppLaunchActionHandler,
        ShortcutActionHandler,
    )

    private val handlerMap: Map<String, ActionHandler> = handlers
        .flatMap { handler -> handler.supportedActions.map { it to handler } }
        .toMap()

    init {
        val seen = mutableSetOf<String>()
        for (handler in handlers) {
            for (id in handler.supportedActions) {
                check(seen.add(id)) {
                    "Duplicate actionId: $id registered by ${handler::class.simpleName}"
                }
            }
        }
    }

    fun isRegistered(actionId: String): Boolean = actionId in handlerMap

    suspend fun handle(action: Action, context: ActionHandlerContext): Boolean {
        val handler = handlerMap[action.value]
        if (handler == null) {
            if (BuildConfig.DEBUG) {
                check(false) { "Unregistered actionId: ${action.value}" }
            }
            return false
        }
        return runCatching {
            handler.handle(action, context)
        }.getOrElse { e ->
            e.printStackTrace()
            false
        }
    }
}
