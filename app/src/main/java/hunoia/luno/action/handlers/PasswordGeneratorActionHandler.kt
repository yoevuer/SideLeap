package hunoia.luno.action.handlers

import hunoia.luno.R
import hunoia.luno.action.Action
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.api.ActionExecutionResult
import hunoia.luno.action.api.ActionHandler
import hunoia.luno.action.api.ActionHandlerContext
import hunoia.luno.action.api.PasswordGenerator
import hunoia.luno.system.copySensitiveText

object PasswordGeneratorActionHandler : ActionHandler {
    override val supportedActions = setOf(
        GlobalActions.GENERATE_PASSWORD_COPY,
        GlobalActions.OPEN_PASSWORD_GENERATOR,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.GENERATE_PASSWORD_COPY -> generateAndCopy(context)
            GlobalActions.OPEN_PASSWORD_GENERATOR -> context.openPasswordGenerator()
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }

    private fun generateAndCopy(context: ActionHandlerContext) {
        val password = runCatching {
            PasswordGenerator.generate(context.actionSettings.passwordGenerator)
        }.getOrNull()
        if (password == null) {
            context.showToast(context.appContext.getString(R.string.password_generate_failed))
            return
        }

        val copied = copySensitiveText(
            context = context.appContext,
            label = "Generated Password",
            text = password,
        )
        context.showToast(
            context.appContext.getString(
                if (copied) R.string.password_copied else R.string.password_copy_failed
            )
        )
    }
}
