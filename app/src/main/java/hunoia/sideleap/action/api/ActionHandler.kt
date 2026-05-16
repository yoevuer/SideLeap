package hunoia.sideleap.action.api

import hunoia.sideleap.action.Action

interface ActionHandler {
    val supportedActions: Set<String>
    suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult
}
