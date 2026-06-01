package hunoia.luno.config.model

import android.os.SystemClock
import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
enum class ActionLibraryType { Shell, Url, Activity }

@Serializable
@Keep
data class ActionLibraryRefData(
    val entryId: String
)

@Serializable
@Keep
data class ActionLibraryEntry(
    val id: String,
    val type: ActionLibraryType,
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val shellCommand: ShellCommandData = ShellCommandData(),
    val openAppOrUrl: OpenAppOrUrlData = OpenAppOrUrlData(),
) {
    companion object {
        fun create(type: ActionLibraryType, name: String = ""): ActionLibraryEntry {
            return ActionLibraryEntry(
                id = SystemClock.uptimeMillis().toString(),
                type = type,
                name = name,
                openAppOrUrl = when (type) {
                    ActionLibraryType.Activity -> OpenAppOrUrlData(type = OpenAppOrUrlData.TYPE_ACTIVITY)
                    ActionLibraryType.Url -> OpenAppOrUrlData(type = OpenAppOrUrlData.TYPE_URL)
                    ActionLibraryType.Shell -> OpenAppOrUrlData()
                }
            )
        }
    }
}

@Serializable
@Keep
data class ActionLibrarySettings(
    val entries: List<ActionLibraryEntry> = emptyList()
)
