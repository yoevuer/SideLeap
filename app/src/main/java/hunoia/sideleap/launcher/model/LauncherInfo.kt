package hunoia.sideleap.launcher.model

import android.graphics.Bitmap
import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Keep
data class LauncherInfo (
    val packageName: String,
    val className: String,
    val label: String,
    val shortcuts: List<ShortcutInfo> = emptyList(),
) {
    @Serializable
    @Keep
    data class ShortcutInfo(
        val packageName: String,
        val className: String,
        val intents: List<String>,
        val label: String,
        val iconRes: Int = 0,
        val iconPath: String? = null,
        val iconScale: Float = ScaleableDefaults.DEFAULT_SCALE,
        val iconBgColor: Int = 0,
        @Transient
        val iconBitmap: Bitmap? = null
    )
}
