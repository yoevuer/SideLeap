package hunoia.luno.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import hunoia.luno.config.model.CapsuleStyle
import hunoia.luno.config.model.Position
import hunoia.luno.ui.screen.settings.gesture.getIcon
import hunoia.luno.ui.screen.settings.gesture.getIconInitialRotation

@Composable
fun CapsuleGestureAnimation(
    animationStyle: CapsuleStyle,
    sideGestureState: SideGestureState,
    displayOriginX: Float,
    displayOriginY: Float,
    displayFingerX: Float,
    displayFingerY: Float,
    modifier: Modifier = Modifier
) {
    val button = sideGestureState.button ?: return
    val icon = animationStyle.getIcon()
    val backgroundColor = resolveColor(animationStyle.backgroundColorSource, animationStyle.backgroundColorThemeKey, animationStyle.backgroundColor)
    val strokeColor = resolveColor(animationStyle.strokeColorSource, animationStyle.strokeColorThemeKey, animationStyle.strokeColor)
    val iconColor = resolveColor(animationStyle.iconColorSource, animationStyle.iconColorThemeKey, animationStyle.iconColor)
    val iconColorFilter = ColorFilter.tint(iconColor)

    Canvas(modifier = modifier) {
        val originXAnimVal = displayOriginX
        val originYAnimVal = displayOriginY
        val fingerXAnimVal = displayFingerX
        val fingerYAnimVal = displayFingerY
        if (originXAnimVal.isNaN() || originYAnimVal.isNaN() || fingerXAnimVal.isNaN() || fingerYAnimVal.isNaN()) {
            return@Canvas
        }

        val progress = when (button.position) {
            Position.Left -> fingerXAnimVal
            Position.Right -> -fingerXAnimVal
            Position.Bottom -> -fingerYAnimVal
        }.coerceAtLeast(0f)
        if (progress <= 1f) return@Canvas

        val thickness = animationStyle.thickness.toFloat().coerceAtLeast(1f)
        val strokeWidth = animationStyle.strokeWidth.toFloat()
        val maxLength = animationStyle.maxLength.toFloat().coerceAtLeast(thickness)
        val length = progress.coerceAtMost(maxLength).coerceAtLeast(thickness)
        val entryDistance = (thickness + strokeWidth * 2f).coerceAtLeast(1f)
        val entryProgress = (progress / entryDistance).coerceIn(0f, 1f)
        val centerShiftRatio = (progress / maxLength).coerceIn(0f, 1f) * 0.2f
        val centerX = when (button.position) {
            Position.Left, Position.Right -> 0f
            Position.Bottom -> (originXAnimVal + (fingerXAnimVal - originXAnimVal) * centerShiftRatio)
                .coerceIn(thickness / 2f, size.width - thickness / 2f)
        }
        val centerY = when (button.position) {
            Position.Left, Position.Right -> (originYAnimVal + (fingerYAnimVal - originYAnimVal) * centerShiftRatio)
                .coerceIn(thickness / 2f, size.height - thickness / 2f)
            Position.Bottom -> 0f
        }
        val topLeft = when (button.position) {
            Position.Left -> Offset(
                lerpFloat(-length - strokeWidth, 0f, entryProgress),
                centerY - thickness / 2f
            )
            Position.Right -> Offset(
                lerpFloat(size.width + strokeWidth, size.width - length, entryProgress),
                centerY - thickness / 2f
            )
            Position.Bottom -> Offset(
                centerX - thickness / 2f,
                lerpFloat(size.height + strokeWidth, size.height - length, entryProgress)
            )
        }
        val rectSize = when (button.position) {
            Position.Left, Position.Right -> Size(length, thickness)
            Position.Bottom -> Size(thickness, length)
        }
        val radiusCap = minOf(rectSize.width, rectSize.height) / 2f
        val cornerRadius = animationStyle.cornerRadius.toFloat().coerceIn(0f, radiusCap)
        val activeAlpha = if (sideGestureState.canDistanceTriggered(button, false)) 1f else 0.55f

        drawRoundRect(
            color = backgroundColor,
            topLeft = topLeft,
            size = rectSize,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            alpha = activeAlpha
        )
        if (animationStyle.strokeWidth > 0) {
            drawRoundRect(
                color = strokeColor,
                topLeft = topLeft,
                size = rectSize,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(strokeWidth)
            )
        }

        val degree = animationStyle.getIconInitialRotation(button.position) +
            triggerRotationOffset(sideGestureState.triggerDirection, button.position)
        val iconSize = minOf(rectSize.width, rectSize.height) * animationStyle.iconScale
        val rectCenter = Offset(
            x = topLeft.x + rectSize.width / 2f,
            y = topLeft.y + rectSize.height / 2f
        )
        rotate(degree, pivot = rectCenter) {
            translate(left = rectCenter.x - iconSize / 2f, top = rectCenter.y - iconSize / 2f) {
                icon.run {
                    draw(
                        size = Size(iconSize, iconSize),
                        colorFilter = iconColorFilter,
                        alpha = activeAlpha
                    )
                }
            }
        }
    }
}
