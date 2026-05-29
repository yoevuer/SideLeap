package hunoia.luno.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import hunoia.luno.config.model.BubbleStyle
import hunoia.luno.config.model.Position
import hunoia.luno.ui.screen.settings.gesture.getIcon
import hunoia.luno.ui.screen.settings.gesture.getIconInitialRotation

@Composable
fun BubbleGestureAnimation(
    animationStyle: BubbleStyle,
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

        val diameter = animationStyle.diameter.toFloat().coerceAtLeast(1f)
        val radius = diameter / 2f
        val strokeWidth = animationStyle.strokeWidth.toFloat()
        val maxOffset = animationStyle.maxOffset.toFloat().coerceAtLeast(radius)
        val offset = progress.coerceAtMost(maxOffset)
        val centerShiftRatio = (progress / maxOffset.coerceAtLeast(1f)).coerceIn(0f, 1f) * 0.18f
        val centerX = when (button.position) {
            Position.Left -> -radius + offset
            Position.Right -> size.width + radius - offset
            Position.Bottom -> (originXAnimVal + (fingerXAnimVal - originXAnimVal) * centerShiftRatio)
                .coerceIn(radius, size.width - radius)
        }
        val centerY = when (button.position) {
            Position.Left, Position.Right -> (originYAnimVal + (fingerYAnimVal - originYAnimVal) * centerShiftRatio)
                .coerceIn(radius, size.height - radius)
            Position.Bottom -> size.height + radius - offset
        }
        val activeAlpha = if (sideGestureState.canDistanceTriggered(button, false)) 1f else 0.55f

        drawCircle(
            color = backgroundColor,
            radius = radius,
            center = Offset(centerX, centerY),
            alpha = activeAlpha
        )
        if (animationStyle.strokeWidth > 0) {
            drawCircle(
                color = strokeColor,
                radius = radius - strokeWidth / 2f,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidth)
            )
        }

        val degree = animationStyle.getIconInitialRotation(button.position) +
            triggerRotationOffset(sideGestureState.triggerDirection, button.position)
        val iconSize = diameter * animationStyle.iconScale
        val bubbleCenter = Offset(centerX, centerY)
        rotate(degree, pivot = bubbleCenter) {
            translate(left = centerX - iconSize / 2f, top = centerY - iconSize / 2f) {
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
