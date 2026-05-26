package hunoia.luno.action

import androidx.annotation.Keep
import hunoia.luno.core.serialization.JsonHelper
import hunoia.luno.settings.model.GestureSettings
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class VirtualMouseActionData(
    val mode: Mode = Mode.Default,
) {
    @Serializable
    enum class Mode {
        Default,
        Continuous,
        Single,
    }
}

fun Action.virtualMouseContinuousModeOverride(): Boolean? {
    val mode = runCatching {
        JsonHelper.decodeFromString<VirtualMouseActionData>(data).mode
    }.getOrDefault(VirtualMouseActionData.Mode.Default)
    return when (mode) {
        VirtualMouseActionData.Mode.Default -> null
        VirtualMouseActionData.Mode.Continuous -> true
        VirtualMouseActionData.Mode.Single -> false
    }
}

fun Action.virtualMouseSettings(globalSettings: GestureSettings.VirtualMouse): GestureSettings.VirtualMouse {
    val override = virtualMouseContinuousModeOverride() ?: return globalSettings
    return globalSettings.copy(continuousMode = override)
}
