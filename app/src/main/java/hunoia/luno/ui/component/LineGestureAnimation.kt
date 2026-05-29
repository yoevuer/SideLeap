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
import hunoia.luno.config.model.LineStyle
import hunoia.luno.config.model.Position
import hunoia.luno.ui.screen.settings.gesture.getIcon
import hunoia.luno.ui.screen.settings.gesture.getIconInitialRotation

@Composable
fun LineGestureAnimation(
    animationStyle: LineStyle,
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

        val lineWidth = animationStyle.width.toFloat().coerceAtLeast(1f)
        val maxLength = animationStyle.maxLength.toFloat().coerceAtLeast(1f)
        val length = progress.coerceAtMost(maxLength).coerceAtLeast(1f)
        val maxOffset = animationStyle.maxOffset.toFloat().coerceAtLeast(0f)
        val offset = progress.coerceAtMost(maxOffset)
        val strokeWidth = animationStyle.strokeWidth.toFloat()
        val corRadius = animationStyle.cornerRadius.toFloat()
        val totalWidth = lineWidth + strokeWidth * 2f
        val entryDistance = totalWidth.coerceAtLeast(1f)
        val entryProgress = (progress / entryDistance).coerceIn(0f, 1f)
        val activeAlpha = if (sideGestureState.canDistanceTriggered(button, false)) 1f else 0.55f

        val centerShiftRatio = (progress / maxLength.coerceAtLeast(1f)).coerceIn(0f, 1f) * 0.15f
        val centerX = when (button.position) {
            Position.Left, Position.Right -> (originYAnimVal + (fingerYAnimVal - originYAnimVal) * centerShiftRatio)
                .coerceIn(totalWidth / 2f, size.height - totalWidth / 2f)
            Position.Bottom -> 0f
        }
        val centerY = when (button.position) {
            Position.Left, Position.Right -> 0f
            Position.Bottom -> (originXAnimVal + (fingerXAnimVal - originXAnimVal) * centerShiftRatio)
                .coerceIn(totalWidth / 2f, size.width - totalWidth / 2f)
        }

        val fillSize = when (button.position) {
            Position.Left, Position.Right -> Size(lineWidth, length)
            Position.Bottom -> Size(length, lineWidth)
        }
        val strokeSize = when (button.position) {
            Position.Left, Position.Right -> Size(totalWidth, length + strokeWidth * 2f)
            Position.Bottom -> Size(length + strokeWidth * 2f, totalWidth)
        }
        val fillTopLeft = when (button.position) {
            Position.Left -> Offset(offset, centerX - length / 2f)
            Position.Right -> Offset(size.width - offset - lineWidth, centerX - length / 2f)
            Position.Bottom -> Offset(centerY - length / 2f, size.height - offset - lineWidth)
        }
        val strokeTopLeft = when (button.position) {
            Position.Left -> Offset(
                lerpFloat(-strokeSize.width, fillTopLeft.x - strokeWidth, entryProgress),
                fillTopLeft.y - strokeWidth
            )
            Position.Right -> Offset(
                lerpFloat(size.width + strokeSize.width, fillTopLeft.x - strokeWidth, entryProgress),
                fillTopLeft.y - strokeWidth
            )
            Position.Bottom -> Offset(
                fillTopLeft.x - strokeWidth,
                lerpFloat(size.height + strokeSize.height, fillTopLeft.y - strokeWidth, entryProgress)
            )
        }
        val fillRadius = corRadius.coerceIn(0f, minOf(fillSize.width, fillSize.height) / 2f)
        val strokeRadius = (corRadius + strokeWidth).coerceIn(0f, minOf(strokeSize.width, strokeSize.height) / 2f)

        if (length > 0f) {
            if (strokeWidth > 0f) {
                drawRoundRect(
                    color = strokeColor,
                    topLeft = strokeTopLeft,
                    size = strokeSize,
                    cornerRadius = CornerRadius(strokeRadius, strokeRadius),
                    alpha = activeAlpha
                )
            }
            drawRoundRect(
                color = backgroundColor,
                topLeft = fillTopLeft,
                size = fillSize,
                cornerRadius = CornerRadius(fillRadius, fillRadius),
                alpha = activeAlpha
            )
        }

        val rectCenter = Offset(
            fillTopLeft.x + fillSize.width / 2f,
            fillTopLeft.y + fillSize.height / 2f
        )
        val degree = animationStyle.getIconInitialRotation(button.position) +
            triggerRotationOffset(sideGestureState.triggerDirection, button.position)
        val iconSize = minOf(fillSize.width, fillSize.height) * animationStyle.iconScale.coerceIn(0f, 1f)
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
