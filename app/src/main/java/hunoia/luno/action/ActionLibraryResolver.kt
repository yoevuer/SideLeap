package hunoia.luno.action

import hunoia.luno.config.ConfigProvider
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.ActionLibraryRefData
import hunoia.luno.config.model.ActionLibraryType
import hunoia.luno.core.JsonSerializer

internal object ActionLibraryResolver {
    suspend fun resolve(action: Action): Action? {
        if (action.value != GlobalActions.EXECUTE_SHELL_COMMAND &&
            action.value != GlobalActions.OPEN_URL &&
            action.value != GlobalActions.OPEN_APP_ACTIVITY
        ) {
            return action
        }
        val ref = runCatching {
            JsonSerializer.decodeFromString<ActionLibraryRefData>(action.data)
        }.getOrNull() ?: return action
        if (ref.entryId.isBlank()) return action
        val entry = ConfigProvider.getActionLibrarySettings().entries.firstOrNull { it.id == ref.entryId }
            ?: return null
        val value = when (entry.type) {
            ActionLibraryType.Shell -> GlobalActions.EXECUTE_SHELL_COMMAND
            ActionLibraryType.Url -> GlobalActions.OPEN_URL
            ActionLibraryType.Activity -> GlobalActions.OPEN_APP_ACTIVITY
        }
        val data = when (entry.type) {
            ActionLibraryType.Shell -> JsonSerializer.encodeToString(entry.shellCommand)
            ActionLibraryType.Url,
            ActionLibraryType.Activity -> JsonSerializer.encodeToString(entry.openAppOrUrl)
        }
        return action.copy(value = value, data = data)
    }
}
