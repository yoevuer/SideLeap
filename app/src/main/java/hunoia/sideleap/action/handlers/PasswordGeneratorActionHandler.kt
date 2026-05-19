package hunoia.sideleap.action.handlers

import hunoia.sideleap.R
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.api.ActionExecutionResult
import hunoia.sideleap.action.api.ActionHandler
import hunoia.sideleap.action.api.ActionHandlerContext
import hunoia.sideleap.action.api.PasswordGenerator
import hunoia.sideleap.system.copySensitiveText

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
