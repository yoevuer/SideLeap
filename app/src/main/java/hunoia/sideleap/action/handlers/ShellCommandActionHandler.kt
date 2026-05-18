package hunoia.sideleap.action.handlers

import hunoia.sideleap.R
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.ShellCommandData
import hunoia.sideleap.action.api.ActionExecutionResult
import hunoia.sideleap.action.api.ActionHandler
import hunoia.sideleap.action.api.ActionHandlerContext
import hunoia.sideleap.core.serialization.JsonHelper
import hunoia.sideleap.system.api.ShizukuBinderExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShellCommandActionHandler : ActionHandler {
    override val supportedActions = setOf(GlobalActions.EXECUTE_SHELL_COMMAND)

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        val data = runCatching {
            JsonHelper.decodeFromString<ShellCommandData>(action.data)
        }.getOrDefault(ShellCommandData())
        val command = data.command.trim()
        if (command.isBlank()) {
            context.showToast(context.appContext.getString(R.string.action_setting_hint_shell_command))
            return ActionExecutionResult.Ignored
        }

        val result = withContext(Dispatchers.IO) {
            ShizukuBinderExecutor.runShellCommand(context.appContext, command)
        }
        if (data.showToast) {
            val message = if (result.success) {
                result.output.ifBlank { context.appContext.getString(R.string.shell_command_no_output) }
            } else {
                result.error ?: result.output.ifBlank { "unknown error" }
            }.take(500)
            context.showToast(message)
        }
        return ActionExecutionResult.Success
    }
}
