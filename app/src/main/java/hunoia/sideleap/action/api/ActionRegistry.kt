package hunoia.sideleap.action.api

import hunoia.sideleap.BuildConfig
import hunoia.sideleap.action.handlers.AppLaunchActionHandler
import hunoia.sideleap.action.handlers.FreezeAppsActionHandler
import hunoia.sideleap.action.handlers.MediaActionHandler
import hunoia.sideleap.action.handlers.MoveScreenActionHandler
import hunoia.sideleap.action.handlers.NavigationActionHandler
import hunoia.sideleap.action.handlers.RandomNameActionHandler
import hunoia.sideleap.action.handlers.ShortcutActionHandler
import hunoia.sideleap.action.handlers.SystemActionHandler
import hunoia.sideleap.action.Action

object ActionRegistry {
    private val handlers: List<ActionHandler> = listOf(
        NavigationActionHandler,
        MediaActionHandler,
        SystemActionHandler,
        RandomNameActionHandler,
        MoveScreenActionHandler,
        AppLaunchActionHandler,
        ShortcutActionHandler,
        FreezeAppsActionHandler,
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

    suspend fun execute(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        val handler = handlerMap[action.value]
        if (handler == null) {
            return ActionExecutionResult.Ignored
        }
        return runCatching {
            handler.handle(action, context)
        }.getOrElse { e ->
            ActionExecutionResult.Failed(e.message)
        }
    }
}
