package hunoia.luno.pointer
import hunoia.luno.config.model.Action

import androidx.annotation.Keep
import hunoia.luno.core.JsonSerializer
import hunoia.luno.config.model.GestureSettings
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class PointerActionData(
    val mode: Mode = Mode.Default,
) {
    @Serializable
    enum class Mode {
        Default,
        Continuous,
        Single,
    }
}

fun Action.pointerContinuousModeOverride(): Boolean? {
    val mode = runCatching {
        JsonSerializer.decodeFromString<PointerActionData>(data).mode
    }.getOrDefault(PointerActionData.Mode.Default)
    return when (mode) {
        PointerActionData.Mode.Default -> null
        PointerActionData.Mode.Continuous -> true
        PointerActionData.Mode.Single -> false
    }
}

fun Action.pointerSettings(globalSettings: GestureSettings.Pointer): GestureSettings.Pointer {
    val override = pointerContinuousModeOverride() ?: return globalSettings
    return globalSettings.copy(continuousMode = override)
}
