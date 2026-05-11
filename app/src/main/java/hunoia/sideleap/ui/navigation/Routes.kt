package hunoia.sideleap.ui.navigation

import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import hunoia.sideleap.entity.Position
import hunoia.sideleap.entity.TriggerDirection
import kotlinx.serialization.Serializable
import java.util.Collections
import java.util.LinkedHashMap

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/7
 */

@Keep
@Serializable
data object About

@Keep
@Serializable
data class ActionSelect(
    val gestureButtonId: String,
    val position: Position,
    val direction: TriggerDirection,
    val isLongSlide: Boolean,
    val isSideButton: Boolean
)

@Keep
@Serializable
data object AdvancedSettings

@Keep
@Serializable
data object AppBlacklist

@Keep
@Serializable
data object QuickAppLauncherHidden

@Keep
@Serializable
data object AdjustGestureAngles

@Serializable
@Keep
data class GestureButtonSettings(
    val buttonId: String,
    val position: Position
) {
    val isSideButton: Boolean
        get() = position == Position.Left || position == Position.Right
}

@Keep
@Serializable
data object GestureSettings

@Keep
@Serializable
data object Home

@Keep
@Serializable
data class IconResize(val ids: List<String>) {

    companion object {
        private const val MAX_ICON_CACHE_SIZE = 200

        val iconCache: MutableMap<String, Drawable> = Collections.synchronizedMap(
            object : LinkedHashMap<String, Drawable>(MAX_ICON_CACHE_SIZE, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Drawable>?): Boolean {
                    return size > MAX_ICON_CACHE_SIZE
                }
            }
        )

        val iconBgColorCache: MutableMap<String, Int> = Collections.synchronizedMap(
            object : LinkedHashMap<String, Int>(MAX_ICON_CACHE_SIZE, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Int>?): Boolean {
                    return size > MAX_ICON_CACHE_SIZE
                }
            }
        )
    }
}

@Keep
@Serializable
data object Unlock

@Keep
@Serializable
data object WaveAnimationStyle

@Keep
@Serializable
data object DiagnosticLogs
