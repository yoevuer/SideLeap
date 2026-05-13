package hunoia.sideleap.action

import hunoia.sideleap.entity.Action

interface ActionHandler {
    val supportedActions: Set<String>
    suspend fun handle(action: Action, context: ActionHandlerContext): Boolean
}
