package hunoia.luno.gesture

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import hunoia.luno.config.model.GESTURE_ANGLE_BASE
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.TriggerDirection
import hunoia.luno.config.model.getTriggerDirection
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan
import kotlin.math.hypot

fun calcDirection(
    button: GestureButton,
    origin: Offset,
    finger: Offset,
    buttonBounds: Rect?,
    gestureSettings: GestureSettings
): TriggerDirection? {
    val bounds = buttonBounds ?: return null
    val opposite = when (button.position) {
        Position.Left -> finger.x - bounds.left
        Position.Right -> bounds.right - finger.x
        Position.Bottom -> bounds.bottom - finger.y
    }
    val neighbor = when (button.position) {
        Position.Left, Position.Right -> abs(finger.y - origin.y)
        Position.Bottom -> abs(finger.x - origin.x)
    }
    if (neighbor == 0f) {
        return button.angle.getTriggerDirection(90f)
    }
    val tanVal = opposite / neighbor
    val radians = atan(tanVal)
    val isPreviousArea = when (button.position) {
        Position.Left, Position.Right -> finger.y < origin.y
        Position.Bottom -> finger.x < origin.x
    }
    val degree = if (isPreviousArea) {
        Math.toDegrees(radians.toDouble())
    } else {
        GESTURE_ANGLE_BASE - Math.toDegrees(radians.toDouble())
    }
    return button.angle.getTriggerDirection(degree.toFloat())
}

fun canDistanceTriggered(
    button: GestureButton,
    origin: Offset,
    finger: Offset,
    triggerDirection: TriggerDirection,
    isLongSlide: Boolean,
    stickySlideValue: Float,
    judgeAction: Boolean = true
): Boolean {
    val slideAction = button.slideActions
    val longSlideAction = button.longSlideActions
    val originX = origin.x
    val originY = origin.y
    val fingerX = finger.x + getStickySlideValue(button, stickySlideValue, true)
    val fingerY = finger.y + getStickySlideValue(button, stickySlideValue, false)
    val td = triggerDirection

    if (td == TriggerDirection.Center2) return false

    val slideDistance = if (td == TriggerDirection.Up2 || td == TriggerDirection.Down2) {
        when (button.position) {
            Position.Left, Position.Right -> originY - fingerY
            Position.Bottom -> fingerX - originX
        }
    } else {
        when (button.position) {
            Position.Left -> fingerX - originX
            Position.Right -> originX - fingerX
            Position.Bottom -> originY - fingerY
        }
    }
    if (slideDistance < 0 && td != TriggerDirection.Up2 && td != TriggerDirection.Down2) {
        return false
    }

    val effectiveDistance = when (td) {
        TriggerDirection.Center -> slideDistance
        TriggerDirection.Up, TriggerDirection.Down -> {
            val edge2 = when (button.position) {
                Position.Left, Position.Right -> abs(fingerY - originY)
                Position.Bottom -> abs(fingerX - originX)
            }
            hypot(slideDistance, edge2)
        }
        TriggerDirection.Up2, TriggerDirection.Down2 -> slideDistance.absoluteValue
        else -> return false
    }

    val triggerThreshold = if (isLongSlide) button.longSlideTriggerDistance
    else button.slideTriggerDistance
    val canTrigger = effectiveDistance >= triggerThreshold

    if (!judgeAction) return canTrigger

    val actionList = (if (isLongSlide) longSlideAction else slideAction).let { actions ->
        when (td) {
            TriggerDirection.Center -> actions.center
            TriggerDirection.Up -> actions.up
            TriggerDirection.Down -> actions.down
            TriggerDirection.Up2 -> actions.up2
            TriggerDirection.Down2 -> actions.down2
            else -> return false
        }
    }
    return canTrigger && actionList.isEmptyOrNone().not()
}

fun getStickySlideValue(button: GestureButton, stickySlideValue: Float, isX: Boolean): Float {
    if (isX) {
        return when (button.position) {
            Position.Left -> -stickySlideValue
            else -> stickySlideValue
        }
    }
    return stickySlideValue
}

fun stickySlideValue(): Float = 0f

fun triggerRotationOffset(triggerDirection: TriggerDirection, position: Position): Float {
    return when (triggerDirection) {
        TriggerDirection.Up -> when (position) {
            Position.Left -> -45f
            Position.Right -> 45f
            Position.Bottom -> -45f
        }
        TriggerDirection.Center, TriggerDirection.Center2 -> 0f
        TriggerDirection.Down -> when (position) {
            Position.Left -> 45f
            Position.Right -> -45f
            Position.Bottom -> 45f
        }
        TriggerDirection.Up2 -> when (position) {
            Position.Left -> -90f
            Position.Right -> 90f
            Position.Bottom -> -90f
        }
        TriggerDirection.Down2 -> when (position) {
            Position.Left -> 90f
            Position.Right -> -90f
            Position.Bottom -> 90f
        }
    }
}


