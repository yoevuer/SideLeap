package hunoia.luno.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.toPx
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.WaveStyle
import hunoia.luno.ui.screen.settings.gesture.getIcon
import hunoia.luno.ui.screen.settings.gesture.getIconInitialRotation
import hunoia.luno.gesture.horizontalMirror
import hunoia.luno.gesture.isVertical
import hunoia.luno.gesture.whenVertical
import hunoia.luno.ui.theme.Spacing1
import hunoia.luno.ui.theme.Spacing40

@Composable
fun WaveGestureAnimation(
    animationStyle: WaveStyle,
    sideGestureState: SideGestureState,
    displayOriginX: Float,
    displayOriginY: Float,
    displayFingerX: Float,
    displayFingerY: Float,
    modifier: Modifier = Modifier
) {
    val button = sideGestureState.button ?: return
    val icon = animationStyle.getIcon()
    val bezierOffset = if (animationStyle.safeBounds) 70.dp.toPx() else 0f
    val bezierSpacing = if (animationStyle.safeBounds) Spacing40.toPx() else 0f
    val bezierMaxWidth = animationStyle.width.toFloat()
    val bezierLengthHalf = bezierMaxWidth * animationStyle.bezierLengthHalfRatio
    val bezierTransformOffsetCoerce = if (animationStyle.transformEnabled) bezierLengthHalf / 2f else 0f
    val factorDp = Spacing1.toPx()
    val backgroundColor = resolveColor(animationStyle.backgroundColorSource, animationStyle.backgroundColorThemeKey, animationStyle.backgroundColor)
    val strokeColor = resolveColor(animationStyle.strokeColorSource, animationStyle.strokeColorThemeKey, animationStyle.strokeColor)
    val iconColor = resolveColor(animationStyle.iconColorSource, animationStyle.iconColorThemeKey, animationStyle.iconColor)
    val iconColorFilter = ColorFilter.tint(iconColor)
    val pathCache = remember { Path() }

    Canvas(modifier = modifier) {
        val triggerDirection = sideGestureState.triggerDirection
        val originXAnimVal = displayOriginX
        val originYAnimVal = displayOriginY
        val fingerXAnimVal = displayFingerX
        val fingerYAnimVal = displayFingerY
        if (originXAnimVal.isNaN() ||
            originYAnimVal.isNaN() ||
            fingerXAnimVal.isNaN() ||
            fingerYAnimVal.isNaN()
        ) {
            return@Canvas
        }
        when (button.position) {
            Position.Left -> if (fingerXAnimVal < 0f) return@Canvas
            Position.Right -> if (fingerXAnimVal > 0f) return@Canvas
            Position.Bottom -> if (fingerYAnimVal > 0f) return@Canvas
        }

        val clipMargin = bezierLengthHalf + bezierOffset + bezierSpacing + bezierTransformOffsetCoerce
        val clipBounds = when (button.position) {
            Position.Left -> androidx.compose.ui.geometry.Rect(0f, 0f, (bezierMaxWidth + clipMargin).coerceAtMost(size.width), size.height)
            Position.Right -> androidx.compose.ui.geometry.Rect((size.width - bezierMaxWidth - clipMargin).coerceAtLeast(0f), 0f, size.width, size.height)
            Position.Bottom -> androidx.compose.ui.geometry.Rect(0f, (size.height - bezierMaxWidth - clipMargin).coerceAtLeast(0f), size.width, size.height)
        }
        drawContext.canvas.clipRect(clipBounds.left, clipBounds.top, clipBounds.right, clipBounds.bottom)

        val transformOffset = button.whenVertical(
            vertical = originYAnimVal - fingerYAnimVal,
            horizontal = originXAnimVal - fingerXAnimVal
        ).coerceIn(-bezierTransformOffsetCoerce, bezierTransformOffsetCoerce)
        val safeOrigin = button.whenVertical(
            vertical = originYAnimVal - bezierOffset,
            horizontal = originXAnimVal - bezierOffset
        ).coerceIn(
            minimumValue = if (animationStyle.safeBounds) bezierLengthHalf + bezierSpacing else 0f,
            maximumValue = button.whenVertical(
                vertical = if (animationStyle.safeBounds) size.height - bezierLengthHalf - bezierSpacing else size.height,
                horizontal = if (animationStyle.safeBounds) size.width - bezierLengthHalf - bezierSpacing else size.width
            )
        )
        val bezierPath = pathCache.apply {
            rewind()

            val moveToX = button.horizontalMirror(
                pos = 0f,
                neg = size.width
            ).let { if (button.isVertical) it else safeOrigin - bezierLengthHalf }
            val moveToY = if (button.isVertical) safeOrigin - bezierLengthHalf else size.height

            when (animationStyle.shapeType) {
                WaveStyle.SHAPE_LINE -> {
                    val centerX = when (button.position) {
                        Position.Left -> fingerXAnimVal.coerceAtMost(bezierMaxWidth)
                        Position.Right -> (size.width + fingerXAnimVal).coerceAtLeast(size.width - bezierMaxWidth)
                        Position.Bottom -> safeOrigin
                    }
                    val lineTop = safeOrigin - bezierLengthHalf - transformOffset
                    val lineBottom = safeOrigin + bezierLengthHalf - transformOffset
                    if (button.position == Position.Bottom) {
                        moveTo(safeOrigin - bezierLengthHalf - transformOffset, centerX)
                        lineTo(safeOrigin + bezierLengthHalf - transformOffset, centerX)
                    } else {
                        moveTo(centerX, lineTop)
                        lineTo(centerX, lineBottom)
                    }
                }
                else -> {
                    moveTo(moveToX, moveToY)
                    val factor = factorDp
                    var safeFingerX: Float
                    var safeFingerY: Float
                    when (button.position) {
                        Position.Left, Position.Right -> {
                            safeFingerX = when (button.position) {
                                Position.Left -> fingerXAnimVal.coerceAtMost(bezierMaxWidth)
                                else -> (size.width + fingerXAnimVal).coerceAtLeast(size.width - bezierMaxWidth)
                            }
                            safeFingerY = safeOrigin - bezierLengthHalf / 2.5f - transformOffset
                            cubicTo(
                                x1 = when (button.position) { Position.Left -> -factor else -> size.width + factor },
                                y1 = safeFingerY,
                                x2 = safeFingerX, y2 = safeFingerY,
                                x3 = safeFingerX, y3 = safeOrigin - transformOffset
                            )
                            safeFingerY = safeOrigin + bezierLengthHalf / 2.5f - transformOffset
                            cubicTo(
                                x1 = safeFingerX, y1 = safeFingerY,
                                x2 = when (button.position) { Position.Left -> 0f else -> size.width },
                                y2 = safeFingerY,
                                x3 = when (button.position) { Position.Left -> -factor else -> size.width + factor },
                                y3 = safeOrigin + bezierLengthHalf
                            )
                        }
                        Position.Bottom -> {
                            safeFingerX = safeOrigin - bezierLengthHalf / 2.5f - transformOffset
                            safeFingerY = (size.height + fingerYAnimVal).coerceAtLeast(size.height - bezierMaxWidth)
                            cubicTo(safeFingerX, size.height + factor,
                                safeFingerX, safeFingerY,
                                safeOrigin - transformOffset, safeFingerY)
                            safeFingerX = safeOrigin + bezierLengthHalf / 2.5f - transformOffset
                            cubicTo(safeFingerX, safeFingerY,
                                safeFingerX, size.height,
                                safeOrigin + bezierLengthHalf, size.height + factor)
                        }
                    }
                }
            }

            if (animationStyle.strokeWidth > 0) {
                val offset2 = button.whenVertical(
                    vertical = Offset(button.horizontalMirror(-1f, 1f) * animationStyle.strokeWidth, 0f),
                    horizontal = Offset(0f, animationStyle.strokeWidth.toFloat())
                )
                translate(offset2)
            }
        }
        drawPath(path = bezierPath, color = backgroundColor)
        if (animationStyle.strokeWidth > 0) {
            drawPath(
                path = bezierPath,
                color = strokeColor,
                style = Stroke(animationStyle.strokeWidth.toFloat(), cap = StrokeCap.Round)
            )
        }

        val bezierBoundsRaw = bezierPath.getBounds()
        val bezierBounds = when (button.position) {
            Position.Left, Position.Right -> bezierBoundsRaw.translate(Offset(0f, -transformOffset))
            Position.Bottom -> bezierBoundsRaw.translate(Offset(-transformOffset, 0f))
        }
        icon.run {
            val initialDegree = animationStyle.getIconInitialRotation(button.position)
            val degree = initialDegree + triggerRotationOffset(triggerDirection, button.position)
            rotate(degree, pivot = bezierBounds.center) {
                val radius = when (button.position) {
                    Position.Left, Position.Right -> bezierBounds.width * animationStyle.iconScale
                    Position.Bottom -> bezierBounds.height * animationStyle.iconScale
                }
                val paddingHori = (bezierBounds.width - radius) / 2f
                val paddingVert = (bezierBounds.height - radius) / 2f
                val left = when (button.position) {
                    Position.Left -> paddingHori - animationStyle.strokeWidth
                    Position.Right -> size.width - bezierBounds.width + paddingHori + animationStyle.strokeWidth
                    Position.Bottom -> bezierBounds.left + bezierBounds.width / 2f - radius / 2f
                }
                val top = when (button.position) {
                    Position.Left, Position.Right -> bezierBounds.top + bezierBounds.height / 2f - radius / 2f
                    Position.Bottom -> size.height - bezierBounds.height + paddingVert + animationStyle.strokeWidth
                }
                translate(left = left, top = top) {
                    val canTriggered = sideGestureState.canDistanceTriggered(button, false)
                    draw(
                        size = Size(radius, radius),
                        colorFilter = iconColorFilter,
                        alpha = if (canTriggered) 1f else 0.25f
                    )
                }
            }
        }
    }
}
