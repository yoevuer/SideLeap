package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class SavedFocusTarget(
    val packageName: String,
    val viewId: String,
    val className: String,
    val appName: String = "",
    val label: String = "",
    val disabled: Boolean = false,
    val timestamp: Long = 0L,
)
