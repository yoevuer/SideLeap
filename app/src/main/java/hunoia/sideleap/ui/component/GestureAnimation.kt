package hunoia.sideleap.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.toPx
import hunoia.sideleap.settings.model.AnimationStyle
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.gesture.TriggerDirection.Center
import hunoia.sideleap.gesture.horizontalMirror
import hunoia.sideleap.gesture.isVertical
import hunoia.sideleap.gesture.whenVertical
import hunoia.sideleap.gesture.TriggerDirection.Center2
import hunoia.sideleap.gesture.TriggerDirection.Down
import hunoia.sideleap.gesture.TriggerDirection.Down2
import hunoia.sideleap.gesture.TriggerDirection.Up
import hunoia.sideleap.gesture.TriggerDirection.Up2
import hunoia.sideleap.settings.model.WaveStyle
import hunoia.sideleap.ui.screen.settings.gesture.getIcon
import hunoia.sideleap.ui.screen.settings.gesture.getIconInitialRotation

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/20
 */

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
    // 贝塞尔偏移值
    val bezierOffset = button.whenVertical(
        vertical = if (animationStyle.safeBounds) 70.dp.toPx() else 0f,
        horizontal = 0f
    )
    // 贝塞尔与边界间距
    val bezierSpacing = if (animationStyle.safeBounds) 40.dp.toPx() else 0f
    // 贝塞尔的最大宽度
    val bezierMaxWidth = animationStyle.width.toFloat()
    // 贝塞尔长度的一半
    val bezierLengthHalf = bezierMaxWidth * animationStyle.bezierLengthHalfRatio
    // 贝塞尔沿边缘滑动变形约束
    val bezierTransformOffsetCoerce = if (animationStyle.transformEnabled) bezierLengthHalf / 2f else 0f

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

        // 贝塞尔形变偏移值
        val transformOffset = button.whenVertical(
            vertical = originYAnimVal - fingerYAnimVal,
            horizontal = originXAnimVal - fingerXAnimVal
        ).coerceIn(-bezierTransformOffsetCoerce, bezierTransformOffsetCoerce)
        // 能完整显示整个贝塞尔并且留有间距
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
        val bezierPath = Path().also { path ->

            val moveToX = button.horizontalMirror(
                pos = 0f,
                neg = size.width
            ).let { if (button.isVertical) it else safeOrigin - bezierLengthHalf }
            val moveToY = if (button.isVertical) safeOrigin - bezierLengthHalf else size.height
            path.moveTo(moveToX, moveToY)

            // 避免边缘出现没覆盖全的白边
            val factor = 1.dp.toPx()
            var safeFingerX: Float
            var safeFingerY: Float
            when (button.position) {
                Position.Left, Position.Right -> {
                    safeFingerX = when (button.position) {
                        Position.Left -> fingerXAnimVal.coerceAtMost(bezierMaxWidth)
                        else -> (size.width + fingerXAnimVal).coerceAtLeast(size.width - bezierMaxWidth)
                    }
                    safeFingerY = safeOrigin - bezierLengthHalf / 2.5f - transformOffset
                    path.cubicTo(
                        x1 = when (button.position) {
                            Position.Left -> -factor
                            else -> size.width + factor
                        },
                        y1 = safeFingerY,
                        x2 = safeFingerX,
                        y2 = safeFingerY,
                        x3 = safeFingerX,
                        y3 = safeOrigin - transformOffset
                    )

                    safeFingerY = safeOrigin + bezierLengthHalf / 2.5f - transformOffset
                    path.cubicTo(
                        x1 = safeFingerX,
                        y1 = safeFingerY,
                        x2 = when (button.position) {
                            Position.Left -> 0f
                            else -> size.width
                        },
                        y2 = safeFingerY,
                        x3 = when (button.position) {
                            Position.Left -> -factor
                            else -> size.width + factor
                        },
                        y3 = safeOrigin + bezierLengthHalf
                    )
                }
                Position.Bottom -> {
                    safeFingerX = safeOrigin - bezierLengthHalf / 2.5f - transformOffset
                    safeFingerY = (size.height + fingerYAnimVal).coerceAtLeast(size.height - bezierMaxWidth)
                    path.cubicTo(
                        x1 = safeFingerX,
                        y1 = size.height + factor,
                        x2 = safeFingerX,
                        y2 = safeFingerY,
                        x3 = safeOrigin - transformOffset,
                        y3 = safeFingerY
                    )

                    safeFingerX = safeOrigin + bezierLengthHalf / 2.5f - transformOffset
                    path.cubicTo(
                        x1 = safeFingerX,
                        y1 = safeFingerY,
                        x2 = safeFingerX,
                        y2 = size.height,
                        x3 = safeOrigin + bezierLengthHalf,
                        y3 = size.height + factor
                    )
                }
            }

            if (animationStyle.strokeWidth > 0) {
                val offset2 = button.whenVertical(
                    vertical = Offset(button.horizontalMirror(-1f, 1f) * animationStyle.strokeWidth, 0f),
                    horizontal = Offset(0f, animationStyle.strokeWidth.toFloat())
                )
                path.translate(offset2)
            }
        }
        // 绘制背景
        drawPath(path = bezierPath, color = Color(animationStyle.backgroundColor))
        if (animationStyle.strokeWidth > 0) {
            // 绘制轮廓
            drawPath(
                path = bezierPath,
                color = Color(animationStyle.strokeColor),
                style = Stroke(animationStyle.strokeWidth.toFloat())
            )
        }

        val bezierBounds = when (button.position) {
            Position.Left, Position.Right -> bezierPath.getBounds().translate(Offset(0f, -transformOffset))
            Position.Bottom -> bezierPath.getBounds().translate(Offset(-transformOffset, 0f))
        }
        icon.run {
            val initialDegree = animationStyle.getIconInitialRotation(button.position)
            val degree = initialDegree + when (triggerDirection) {
                Up -> when (button.position) {
                    Position.Left -> -45f
                    Position.Right -> 45f
                    Position.Bottom -> -45f
                }
                Center, Center2 -> 0f
                Down -> when (button.position) {
                    Position.Left -> 45f
                    Position.Right -> -45f
                    Position.Bottom -> 45f
                }
                Up2 -> when (button.position) {
                    Position.Left -> -90f
                    Position.Right -> 90f
                    Position.Bottom -> -90f
                }
                Down2 -> when (button.position) {
                    Position.Left -> 90f
                    Position.Right -> -90f
                    Position.Bottom -> 90f
                }
            }
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
                        colorFilter = ColorFilter.tint(Color(animationStyle.iconColor)),
                        alpha = if (canTriggered) 1f else 0.25f
                    )
                }
            }
        }
    }
}
