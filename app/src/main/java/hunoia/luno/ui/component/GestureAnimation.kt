package hunoia.luno.ui.component
import hunoia.luno.ui.theme.*

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.toPx
import androidx.compose.material3.MaterialTheme
import hunoia.luno.settings.model.AnimationStyle
import hunoia.luno.settings.model.BubbleStyle
import hunoia.luno.settings.model.CapsuleStyle
import hunoia.luno.settings.model.ColorSource
import hunoia.luno.settings.model.LineStyle
import hunoia.luno.settings.model.ThemeColorKey
import hunoia.luno.ui.ext.resolveColor
import hunoia.luno.settings.model.WaveStyle
import hunoia.luno.gesture.GestureButton
import hunoia.luno.gesture.Position
import hunoia.luno.gesture.TriggerDirection.Center
import hunoia.luno.gesture.horizontalMirror
import hunoia.luno.gesture.isVertical
import hunoia.luno.gesture.whenVertical
import hunoia.luno.gesture.TriggerDirection.Center2
import hunoia.luno.gesture.TriggerDirection.Down
import hunoia.luno.gesture.TriggerDirection.Down2
import hunoia.luno.gesture.TriggerDirection.Up
import hunoia.luno.gesture.TriggerDirection.Up2
import hunoia.luno.ui.screen.settings.gesture.getIcon
import hunoia.luno.ui.screen.settings.gesture.getIconInitialRotation

@Composable
fun GestureAnimation(
    animationStyle: AnimationStyle,
    SideGestureState: SideGestureState,
    modifier: Modifier = Modifier
) {
    when (animationStyle) {
        is WaveStyle -> WaveGestureAnimation(
            modifier = modifier,
            animationStyle = animationStyle,
            sideGestureState = SideGestureState
        )
        is CapsuleStyle -> CapsuleGestureAnimation(
            modifier = modifier,
            animationStyle = animationStyle,
            sideGestureState = SideGestureState
        )
        is BubbleStyle -> BubbleGestureAnimation(
            modifier = modifier,
            animationStyle = animationStyle,
            sideGestureState = SideGestureState
        )
        is LineStyle -> LineGestureAnimation(
            modifier = modifier,
            animationStyle = animationStyle,
            sideGestureState = SideGestureState
        )
    }
}

@Composable
private fun resolveColor(source: ColorSource, themeKey: ThemeColorKey, customColor: Int): Color = when (source) {
    ColorSource.Custom -> Color(customColor)
    ColorSource.Theme -> themeKey.resolveColor()
}

private fun triggerRotationOffset(triggerDirection: hunoia.luno.gesture.TriggerDirection, position: Position): Float {
    return when (triggerDirection) {
        Up -> when (position) {
            Position.Left -> -45f
            Position.Right -> 45f
            Position.Bottom -> -45f
        }
        Center, Center2 -> 0f
        Down -> when (position) {
            Position.Left -> 45f
            Position.Right -> -45f
            Position.Bottom -> 45f
        }
        Up2 -> when (position) {
            Position.Left -> -90f
            Position.Right -> 90f
            Position.Bottom -> -90f
        }
        Down2 -> when (position) {
            Position.Left -> 90f
            Position.Right -> -90f
            Position.Bottom -> 90f
        }
    }
}

@Composable
private fun CapsuleGestureAnimation(
    animationStyle: CapsuleStyle,
    sideGestureState: SideGestureState,
    modifier: Modifier = Modifier
) {
    val button = sideGestureState.button ?: return
    val icon = animationStyle.getIcon()
    val backgroundColor = resolveColor(animationStyle.backgroundColorSource, animationStyle.backgroundColorThemeKey, animationStyle.backgroundColor)
    val strokeColor = resolveColor(animationStyle.strokeColorSource, animationStyle.strokeColorThemeKey, animationStyle.strokeColor)
    val iconColor = resolveColor(animationStyle.iconColorSource, animationStyle.iconColorThemeKey, animationStyle.iconColor)
    val iconColorFilter = ColorFilter.tint(iconColor)

    Canvas(modifier = modifier) {
        val originXAnimVal = sideGestureState.originXAnimVal
        val originYAnimVal = sideGestureState.originYAnimVal
        val fingerXAnimVal = sideGestureState.fingerXAnimVal
        val fingerYAnimVal = sideGestureState.fingerYAnimVal
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

@Composable
private fun BubbleGestureAnimation(
    animationStyle: BubbleStyle,
    sideGestureState: SideGestureState,
    modifier: Modifier = Modifier
) {
    val button = sideGestureState.button ?: return
    val icon = animationStyle.getIcon()
    val backgroundColor = resolveColor(animationStyle.backgroundColorSource, animationStyle.backgroundColorThemeKey, animationStyle.backgroundColor)
    val strokeColor = resolveColor(animationStyle.strokeColorSource, animationStyle.strokeColorThemeKey, animationStyle.strokeColor)
    val iconColor = resolveColor(animationStyle.iconColorSource, animationStyle.iconColorThemeKey, animationStyle.iconColor)
    val iconColorFilter = ColorFilter.tint(iconColor)

    Canvas(modifier = modifier) {
        val originXAnimVal = sideGestureState.originXAnimVal
        val originYAnimVal = sideGestureState.originYAnimVal
        val fingerXAnimVal = sideGestureState.fingerXAnimVal
        val fingerYAnimVal = sideGestureState.fingerYAnimVal
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

@Composable
private fun WaveGestureAnimation(
    animationStyle: WaveStyle,
    sideGestureState: SideGestureState,
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
        val originXAnimVal = sideGestureState.originXAnimVal
        val originYAnimVal = sideGestureState.originYAnimVal
        val fingerXAnimVal = sideGestureState.fingerXAnimVal
        val fingerYAnimVal = sideGestureState.fingerYAnimVal
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

@Composable
private fun LineGestureAnimation(
    animationStyle: LineStyle,
    sideGestureState: SideGestureState,
    modifier: Modifier = Modifier
) {
    val button = sideGestureState.button ?: return
    val icon = animationStyle.getIcon()
    val backgroundColor = resolveColor(animationStyle.backgroundColorSource, animationStyle.backgroundColorThemeKey, animationStyle.backgroundColor)
    val strokeColor = resolveColor(animationStyle.strokeColorSource, animationStyle.strokeColorThemeKey, animationStyle.strokeColor)
    val iconColor = resolveColor(animationStyle.iconColorSource, animationStyle.iconColorThemeKey, animationStyle.iconColor)
    val iconColorFilter = ColorFilter.tint(iconColor)

    Canvas(modifier = modifier) {
        val originXAnimVal = sideGestureState.originXAnimVal
        val originYAnimVal = sideGestureState.originYAnimVal
        val fingerXAnimVal = sideGestureState.fingerXAnimVal
        val fingerYAnimVal = sideGestureState.fingerYAnimVal
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

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
