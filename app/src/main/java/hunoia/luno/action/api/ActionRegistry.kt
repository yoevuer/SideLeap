package hunoia.luno.action.api

import hunoia.luno.action.Action
import hunoia.luno.action.handlers.allHandlers

object ActionRegistry {
    private val handlers: List<ActionHandler> = allHandlers

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
