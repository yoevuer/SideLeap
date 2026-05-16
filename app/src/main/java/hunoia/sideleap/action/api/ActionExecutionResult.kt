package hunoia.sideleap.action.api

sealed interface ActionExecutionResult {
    data object Success : ActionExecutionResult
    data object Ignored : ActionExecutionResult
    data class Failed(val reason: String? = null) : ActionExecutionResult
}
