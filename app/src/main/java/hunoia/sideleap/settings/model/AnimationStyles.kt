package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import hunoia.sideleap.core.serialization.JsonHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Keep
data class AnimationStyles(
    val type: Int = AnimationStylesDefaults.Type,
    val json: String = "",
    val isAnimationEnabled: Boolean = AnimationStylesDefaults.IsAnimationEnabled
) {
    companion object {
        const val TYPE_WAVE = AnimationStylesDefaults.TYPE_WAVE
    }

    @Transient
    val value: AnimationStyle = run {
        val json = json
        if (json.isEmpty()) {
            return@run WaveStyle()
        }
        when (type) {
            TYPE_WAVE -> JsonHelper.decodeFromString<WaveStyle>(json)
            else -> error("Unknown AnimationStyle type: $type")
        }
    }
}

object AnimationStylesDefaults {

    const val TYPE_WAVE = 1

    const val Type = TYPE_WAVE
    const val IsAnimationEnabled = true
    const val WaveStyleBackgroundColor = android.graphics.Color.BLACK
    const val WaveStyleStrokeColor = android.graphics.Color.TRANSPARENT
    const val WaveStyleStrokeWidth = 0
    val WaveStyleWidth = com.blankj.utilcode.util.ConvertUtils.dp2px(40f)
    const val WaveStyleBezierLengthHalfRatio = 2.5f
    const val WaveStyleSafeBounds = true
    const val WaveStyleTransformEnabled = true
    val WaveStyleIconColor = android.graphics.Color.argb(200, 255, 255, 255)
    const val WaveStyleIconScale = 0.6f
    const val WaveStyleIconType = WaveStyle.ICON_TYPE_ARROW
}

sealed interface AnimationStyle

@Serializable
@Keep
data class WaveStyle(
    val backgroundColor: Int = AnimationStylesDefaults.WaveStyleBackgroundColor,
    val strokeColor: Int = AnimationStylesDefaults.WaveStyleStrokeColor,
    val strokeWidth: Int = AnimationStylesDefaults.WaveStyleStrokeWidth,
    val width: Int = AnimationStylesDefaults.WaveStyleWidth,
    val bezierLengthHalfRatio: Float = AnimationStylesDefaults.WaveStyleBezierLengthHalfRatio,
    val safeBounds: Boolean = AnimationStylesDefaults.WaveStyleSafeBounds,
    val transformEnabled: Boolean = AnimationStylesDefaults.WaveStyleTransformEnabled,
    val iconColor: Int = AnimationStylesDefaults.WaveStyleIconColor,
    val iconScale: Float = AnimationStylesDefaults.WaveStyleIconScale,
    val iconType: Int = AnimationStylesDefaults.WaveStyleIconType,
    val stickySlideEnabled: Boolean = false
) : AnimationStyle {

    companion object {
        const val ICON_TYPE_ARROW = 1
        const val ICON_TYPE_TRIANGLE = 2
        const val ICON_TYPE_ANGLE = 3
        const val ICON_TYPE_ARROW_NEW = 4
    }
}
