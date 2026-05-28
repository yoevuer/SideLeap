package hunoia.luno.action

import androidx.annotation.Keep
import hunoia.luno.core.JsonHelper
import hunoia.luno.settings.model.GestureSettings
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
        JsonHelper.decodeFromString<PointerActionData>(data).mode
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
