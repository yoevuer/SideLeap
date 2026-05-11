package hunoia.sideleap.entity

import androidx.annotation.Keep
import hunoia.sideleap.constant.AnimationStylesDefaults
import hunoia.sideleap.constant.AnimationStylesDefaults.IsAnimationEnabled
import hunoia.sideleap.constant.AnimationStylesDefaults.Type
import hunoia.sideleap.constant.AnimationStylesDefaults.WaveStyleBackgroundColor
import hunoia.sideleap.constant.AnimationStylesDefaults.WaveStyleBezierLengthHalfRatio
import hunoia.sideleap.constant.AnimationStylesDefaults.WaveStyleIconColor
import hunoia.sideleap.constant.AnimationStylesDefaults.WaveStyleIconScale
import hunoia.sideleap.constant.AnimationStylesDefaults.WaveStyleIconType
import hunoia.sideleap.constant.AnimationStylesDefaults.WaveStyleSafeBounds
import hunoia.sideleap.constant.AnimationStylesDefaults.WaveStyleStrokeColor
import hunoia.sideleap.constant.AnimationStylesDefaults.WaveStyleStrokeWidth
import hunoia.sideleap.constant.AnimationStylesDefaults.WaveStyleTransformEnabled
import hunoia.sideleap.constant.AnimationStylesDefaults.WaveStyleWidth
import hunoia.sideleap.utils.JsonHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/20
 */

@Serializable
@Keep
data class AnimationStyles(
    val type: Int = Type,
    val json: String = "",
    val isAnimationEnabled: Boolean = IsAnimationEnabled
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

sealed interface AnimationStyle

@Serializable
@Keep
data class WaveStyle(
    val backgroundColor: Int = WaveStyleBackgroundColor,
    val strokeColor: Int = WaveStyleStrokeColor,
    val strokeWidth: Int = WaveStyleStrokeWidth,
    val width: Int = WaveStyleWidth,
    val bezierLengthHalfRatio: Float = WaveStyleBezierLengthHalfRatio,
    val safeBounds: Boolean = WaveStyleSafeBounds,
    val transformEnabled: Boolean = WaveStyleTransformEnabled,
    val iconColor: Int = WaveStyleIconColor,
    val iconScale: Float = WaveStyleIconScale,
    val iconType: Int = WaveStyleIconType,
    val stickySlideEnabled: Boolean = false
) : AnimationStyle {

    companion object {
        const val ICON_TYPE_ARROW = 1
        const val ICON_TYPE_TRIANGLE = 2
        const val ICON_TYPE_ANGLE = 3
        const val ICON_TYPE_ARROW_NEW = 4
    }
}