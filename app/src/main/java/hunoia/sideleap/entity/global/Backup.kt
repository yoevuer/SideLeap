package hunoia.sideleap.entity.global

import androidx.annotation.Keep
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.entity.QuickAppLauncherSettings
import kotlinx.serialization.Serializable

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/10
 */
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
