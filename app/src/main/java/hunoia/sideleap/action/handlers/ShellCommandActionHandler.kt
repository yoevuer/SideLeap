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
        val command = runCatching {
            JsonHelper.decodeFromString<ShellCommandData>(action.data).command.trim()
        }.getOrDefault("")
        if (command.isBlank()) {
            context.showToast(context.appContext.getString(R.string.action_setting_hint_shell_command))
            return ActionExecutionResult.Ignored
        }

        val result = withContext(Dispatchers.IO) {
            ShizukuBinderExecutor.runShellCommand(context.appContext, command)
        }
        if (result.success) {
            val summary = result.output.lineSequence().firstOrNull { it.isNotBlank() }?.take(120).orEmpty()
            val msg = context.appContext.getString(R.string.shell_command_executed, result.exitCode)
            context.showToast(if (summary.isBlank()) msg else "$msg: $summary")
        } else {
            val error = result.error ?: result.output.take(120).ifBlank { "unknown error" }
            context.showToast(context.appContext.getString(R.string.shell_command_failed, error))
        }
        return ActionExecutionResult.Success
    }
}
