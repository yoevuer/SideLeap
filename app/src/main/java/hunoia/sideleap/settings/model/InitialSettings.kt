package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import hunoia.sideleap.settings.api.InitialSettingsDefaults.GestureEnabled
import hunoia.sideleap.settings.api.InitialSettingsDefaults.Unlocked
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class InitialSettings(
    val gestureEnabled: Boolean = GestureEnabled,
    val unlocked: Boolean = Unlocked
)
