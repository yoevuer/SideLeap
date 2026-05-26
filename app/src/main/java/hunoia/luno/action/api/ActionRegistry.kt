package hunoia.luno.action.api

import hunoia.luno.BuildConfig
import hunoia.luno.action.handlers.AppLaunchActionHandler
import hunoia.luno.action.handlers.FreezeAppsActionHandler
import hunoia.luno.action.handlers.MediaActionHandler
import hunoia.luno.action.handlers.MoveScreenActionHandler
import hunoia.luno.action.handlers.NavigationActionHandler
import hunoia.luno.action.handlers.PasswordGeneratorActionHandler
import hunoia.luno.action.handlers.RandomNameActionHandler
import hunoia.luno.action.handlers.ShortcutActionHandler
import hunoia.luno.action.handlers.ShellCommandActionHandler
import hunoia.luno.action.handlers.SystemActionHandler
import hunoia.luno.action.handlers.VirtualMouseActionHandler
import hunoia.luno.action.handlers.VolumeScrubActionHandler
import hunoia.luno.action.Action

object ActionRegistry {
    private val handlers: List<ActionHandler> = listOf(
        NavigationActionHandler,
        MediaActionHandler,
        SystemActionHandler,
        RandomNameActionHandler,
        PasswordGeneratorActionHandler,
        MoveScreenActionHandler,
        AppLaunchActionHandler,
        ShortcutActionHandler,
        FreezeAppsActionHandler,
        VirtualMouseActionHandler,
        VolumeScrubActionHandler,
        ShellCommandActionHandler,
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
