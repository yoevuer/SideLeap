package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import hunoia.sideleap.gesture.GestureButton
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class Backup(
    val initialSettings: InitialSettings? = null,
    val advancedSettings: AdvancedSettings? = null,
    val gestureSettings: GestureSettings? = null,
    val actionSettings: ActionSettings? = null,
    val gestureButtons: List<GestureButton>? = null,
    val bottomGestureButtons: List<GestureButton>? = null,
    val quickAppLauncherSettings: QuickAppLauncherSettings? = null,
    val frozenAppSettings: FrozenAppSettings? = null,
    val timestamp: Long? = null,
    val version: String? = null
)
