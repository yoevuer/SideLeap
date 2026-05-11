package hunoia.sideleap.entity

import androidx.annotation.Keep
import hunoia.sideleap.constant.ScaleableDefaults
import kotlinx.serialization.Serializable

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/2
 */
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