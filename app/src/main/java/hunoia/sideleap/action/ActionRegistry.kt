package hunoia.sideleap.action

import hunoia.sideleap.BuildConfig
import hunoia.sideleap.entity.Action

object ActionRegistry {
    private val handlers: List<ActionHandler> = emptyList()

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
