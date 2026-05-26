package hunoia.luno.settings.model

import androidx.annotation.Keep
import hunoia.luno.settings.defaults.InitialSettingsDefaults.GestureEnabled
import hunoia.luno.settings.defaults.InitialSettingsDefaults.Unlocked
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class InitialSettings(
    val gestureEnabled: Boolean = GestureEnabled,
    val unlocked: Boolean = Unlocked
)
