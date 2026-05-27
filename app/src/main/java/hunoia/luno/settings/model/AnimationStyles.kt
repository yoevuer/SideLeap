package hunoia.luno.settings.model

import androidx.annotation.Keep
import hunoia.luno.core.DensityProvider
import hunoia.luno.core.serialization.JsonHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Keep
data class AnimationStyles(
    val type: Int = AnimationStylesDefaults.Type,
    val json: String = "",
    val jsonMap: Map<Int, String> = emptyMap(),
    val isAnimationEnabled: Boolean = AnimationStylesDefaults.IsAnimationEnabled
) {
    companion object {
        const val TYPE_WAVE = AnimationStylesDefaults.TYPE_WAVE
        const val TYPE_CAPSULE = AnimationStylesDefaults.TYPE_CAPSULE
        const val TYPE_BUBBLE = AnimationStylesDefaults.TYPE_BUBBLE
        const val TYPE_LINE = AnimationStylesDefaults.TYPE_LINE
    }

    fun payloadOf(targetType: Int): String {
        val payload = jsonMap[targetType]
        return if (!payload.isNullOrEmpty()) payload else json
    }

    fun selectType(targetType: Int): AnimationStyles {
        val updatedMap = jsonMap + (type to json)
        return copy(type = targetType, json = payloadOf(targetType), jsonMap = updatedMap)
    }

    fun updateStyle(payloadType: Int, payload: String): AnimationStyles {
        return copy(json = payload, jsonMap = jsonMap + (payloadType to payload))
    }

    @Transient
    val value: AnimationStyle = run {
        val json = json
        if (json.isEmpty()) {
            return@run when (type) {
                TYPE_WAVE -> WaveStyle()
                TYPE_CAPSULE -> CapsuleStyle()
                TYPE_BUBBLE -> BubbleStyle()
                TYPE_LINE -> LineStyle()
                else -> WaveStyle()
            }
        }
        when (type) {
            TYPE_WAVE -> JsonHelper.decodeFromString<WaveStyle>(json)
            TYPE_CAPSULE -> JsonHelper.decodeFromString<CapsuleStyle>(json)
            TYPE_BUBBLE -> JsonHelper.decodeFromString<BubbleStyle>(json)
            TYPE_LINE -> JsonHelper.decodeFromString<LineStyle>(json)
            else -> error("Unknown AnimationStyle type: $type")
        }
    }
}

@Serializable
@Keep
data class LineStyle(
    val backgroundColor: Int = AnimationStylesDefaults.LineStyleBackgroundColor,
    val strokeColor: Int = AnimationStylesDefaults.LineStyleStrokeColor,
    val strokeWidth: Int = AnimationStylesDefaults.LineStyleStrokeWidth,
    val width: Int = AnimationStylesDefaults.LineStyleWidth,
    val maxLength: Int = AnimationStylesDefaults.LineStyleMaxLength,
    val maxOffset: Int = AnimationStylesDefaults.LineStyleMaxOffset,
    val cornerRadius: Int = AnimationStylesDefaults.LineStyleCornerRadius,
    val iconColor: Int = AnimationStylesDefaults.LineStyleIconColor,
    val iconScale: Float = AnimationStylesDefaults.LineStyleIconScale,
    val iconType: Int = AnimationStylesDefaults.LineStyleIconType,
    val backgroundColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val strokeColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val iconColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val backgroundColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultBackgroundThemeKey,
    val strokeColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultStrokeThemeKey,
    val iconColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultIconThemeKey,
) : AnimationStyle {
    companion object {
        const val ICON_TYPE_ARROW = 1
    }
}

enum class ColorSource { Custom, Theme }

enum class ThemeColorKey {
    Primary, PrimaryContainer,
    Secondary, SecondaryContainer,
    Tertiary, TertiaryContainer,
    Surface, SurfaceVariant,
    OnSurface, OnSurfaceVariant,
    Outline, OutlineVariant,
    SurfaceContainerLow, SurfaceContainer, SurfaceContainerHigh
}

object AnimationStylesDefaults {

    const val TYPE_WAVE = 1
    const val TYPE_CAPSULE = 2
    const val TYPE_BUBBLE = 3
    const val TYPE_LINE = 4

    const val Type = TYPE_WAVE
    const val IsAnimationEnabled = true

    // Shared color source / theme key defaults (identical across all 4 styles)
    val DefaultColorSource = ColorSource.Theme
    val DefaultBackgroundThemeKey = ThemeColorKey.SurfaceContainerHigh
    val DefaultStrokeThemeKey = ThemeColorKey.Outline
    val DefaultIconThemeKey = ThemeColorKey.OnSurface

    const val WaveStyleBackgroundColor = android.graphics.Color.BLACK
    const val WaveStyleStrokeColor = android.graphics.Color.TRANSPARENT
    const val WaveStyleStrokeWidth = 0
    val WaveStyleWidth = DensityProvider.dp2px(40f)
    const val WaveStyleBezierLengthHalfRatio = 2.5f
    const val WaveStyleSafeBounds = true
    const val WaveStyleTransformEnabled = true
    val WaveStyleIconColor = android.graphics.Color.argb(200, 255, 255, 255)
    const val WaveStyleIconScale = 0.6f
    const val WaveStyleIconType = WaveStyle.ICON_TYPE_ARROW
    const val WaveStyleShapeType = WaveStyle.SHAPE_WAVE

    val CapsuleStyleBackgroundColor = android.graphics.Color.argb(220, 18, 18, 18)
    const val CapsuleStyleStrokeColor = android.graphics.Color.TRANSPARENT
    const val CapsuleStyleStrokeWidth = 0
    val CapsuleStyleThickness = DensityProvider.dp2px(36f)
    val CapsuleStyleMaxLength = DensityProvider.dp2px(72f)
    val CapsuleStyleCornerRadius = DensityProvider.dp2px(18f)
    val CapsuleStyleIconColor = android.graphics.Color.argb(220, 255, 255, 255)
    const val CapsuleStyleIconScale = 0.52f
    const val CapsuleStyleIconType = WaveStyle.ICON_TYPE_ARROW

    val BubbleStyleBackgroundColor = android.graphics.Color.argb(220, 22, 22, 22)
    val BubbleStyleStrokeColor = android.graphics.Color.argb(36, 255, 255, 255)
    const val BubbleStyleStrokeWidth = 0
    val BubbleStyleDiameter = DensityProvider.dp2px(44f)
    val BubbleStyleMaxOffset = DensityProvider.dp2px(72f)
    val BubbleStyleIconColor = android.graphics.Color.argb(232, 255, 255, 255)
    const val BubbleStyleIconScale = 0.52f
    const val BubbleStyleIconType = WaveStyle.ICON_TYPE_ARROW

    val LineStyleBackgroundColor = android.graphics.Color.argb(220, 18, 18, 18)
    const val LineStyleStrokeColor = android.graphics.Color.TRANSPARENT
    const val LineStyleStrokeWidth = 0
    val LineStyleWidth = DensityProvider.dp2px(4f)
    val LineStyleMaxLength = DensityProvider.dp2px(72f)
    val LineStyleMaxOffset = DensityProvider.dp2px(36f)
    val LineStyleCornerRadius = DensityProvider.dp2px(2f)
    val LineStyleIconColor = android.graphics.Color.argb(220, 255, 255, 255)
    const val LineStyleIconScale = 0.5f
    const val LineStyleIconType = 1
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
    val shapeType: Int = AnimationStylesDefaults.WaveStyleShapeType,
    val stickySlideEnabled: Boolean = false,
    val backgroundColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val strokeColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val iconColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val backgroundColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultBackgroundThemeKey,
    val strokeColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultStrokeThemeKey,
    val iconColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultIconThemeKey,
) : AnimationStyle {

    companion object {
        const val ICON_TYPE_ARROW = 1
        const val ICON_TYPE_TRIANGLE = 2
        const val ICON_TYPE_ANGLE = 3
        const val ICON_TYPE_ARROW_NEW = 4

        const val SHAPE_WAVE = 0
        const val SHAPE_LINE = 3
    }
}

@Serializable
@Keep
data class CapsuleStyle(
    val backgroundColor: Int = AnimationStylesDefaults.CapsuleStyleBackgroundColor,
    val strokeColor: Int = AnimationStylesDefaults.CapsuleStyleStrokeColor,
    val strokeWidth: Int = AnimationStylesDefaults.CapsuleStyleStrokeWidth,
    val thickness: Int = AnimationStylesDefaults.CapsuleStyleThickness,
    val maxLength: Int = AnimationStylesDefaults.CapsuleStyleMaxLength,
    val cornerRadius: Int = AnimationStylesDefaults.CapsuleStyleCornerRadius,
    val iconColor: Int = AnimationStylesDefaults.CapsuleStyleIconColor,
    val iconScale: Float = AnimationStylesDefaults.CapsuleStyleIconScale,
    val iconType: Int = AnimationStylesDefaults.CapsuleStyleIconType,
    val backgroundColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val strokeColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val iconColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val backgroundColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultBackgroundThemeKey,
    val strokeColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultStrokeThemeKey,
    val iconColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultIconThemeKey,
) : AnimationStyle {
    companion object {
        const val ICON_TYPE_ARROW = 1
    }
}

@Serializable
@Keep
data class BubbleStyle(
    val backgroundColor: Int = AnimationStylesDefaults.BubbleStyleBackgroundColor,
    val strokeColor: Int = AnimationStylesDefaults.BubbleStyleStrokeColor,
    val strokeWidth: Int = AnimationStylesDefaults.BubbleStyleStrokeWidth,
    val diameter: Int = AnimationStylesDefaults.BubbleStyleDiameter,
    val maxOffset: Int = AnimationStylesDefaults.BubbleStyleMaxOffset,
    val iconColor: Int = AnimationStylesDefaults.BubbleStyleIconColor,
    val iconScale: Float = AnimationStylesDefaults.BubbleStyleIconScale,
    val iconType: Int = AnimationStylesDefaults.BubbleStyleIconType,
    val backgroundColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val strokeColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val iconColorSource: ColorSource = AnimationStylesDefaults.DefaultColorSource,
    val backgroundColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultBackgroundThemeKey,
    val strokeColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultStrokeThemeKey,
    val iconColorThemeKey: ThemeColorKey = AnimationStylesDefaults.DefaultIconThemeKey,
) : AnimationStyle {
    companion object {
        const val ICON_TYPE_ARROW = 1
    }
}
