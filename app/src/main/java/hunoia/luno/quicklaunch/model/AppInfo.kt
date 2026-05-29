package hunoia.luno.quicklaunch.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val iconScale: Float = ScaleableDefaults.DEFAULT_SCALE,
    val miniWindow: Boolean = false,
    val iconBgColor: Int = 0
)
