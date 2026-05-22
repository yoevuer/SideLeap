package hunoia.sideleap.gesture.application

import androidx.compose.ui.geometry.Offset
import hunoia.sideleap.core.DensityProvider
import hunoia.sideleap.settings.model.GestureSettings
import kotlin.math.hypot

enum class VirtualMousePointerAction {
    Click,
    LongPress,
}

fun virtualMouseInitialPosition(
    settings: GestureSettings.VirtualMouse,
    previousPosition: Offset = Offset.Unspecified,
): Offset {
    if (settings.continuousMode && previousPosition.x.isFinite() && previousPosition.y.isFinite()) {
        return clampVirtualMousePosition(previousPosition)
    }
    return Offset(
        x = DensityProvider.screenWidthPx * 0.5f,
        y = DensityProvider.screenHeightPx * settings.initialYRatio.coerceIn(0f, 1f),
    )
}

fun moveVirtualMouseCursor(
    position: Offset,
    dragAmount: Offset,
    settings: GestureSettings.VirtualMouse,
): Offset {
    val speed = hypot(dragAmount.x, dragAmount.y)
    if (speed < 0.1f) return position
    val precisionFactor = 0.38f + (speed / 36f).coerceIn(0f, 1f) * 0.62f
    val factor = 1f + settings.acceleration * (speed / 80f).coerceAtMost(2f)
    return clampVirtualMousePosition(
        Offset(
            x = position.x + dragAmount.x * settings.sensitivityX * precisionFactor * factor,
            y = position.y + dragAmount.y * settings.sensitivityY * precisionFactor * factor,
        )
    )
}

fun isVirtualMouseStillMovement(
    dragAmount: Offset,
    settings: GestureSettings.VirtualMouse,
): Boolean {
    val deadZone = DensityProvider.dp2px(settings.movementDeadZoneDp.toFloat()).toFloat()
    return hypot(dragAmount.x, dragAmount.y) <= deadZone
}

fun isVirtualMouseWithinLongPressTolerance(
    anchor: Offset,
    current: Offset,
    settings: GestureSettings.VirtualMouse,
): Boolean {
    if (!anchor.x.isFinite() || !anchor.y.isFinite() || !current.x.isFinite() || !current.y.isFinite()) return false
    val tolerance = DensityProvider.dp2px(settings.longPressMoveToleranceDp.toFloat()).toFloat()
    return hypot(current.x - anchor.x, current.y - anchor.y) <= tolerance
}

fun isVirtualMouseCancelGesture(
    touchPosition: Offset,
    settings: GestureSettings.VirtualMouse,
): Boolean {
    if (!touchPosition.x.isFinite() || !touchPosition.y.isFinite()) return false
    val threshold = DensityProvider.dp2px(settings.edgeCancelThresholdDp.toFloat()).toFloat()
    val width = DensityProvider.screenWidthPx.toFloat()
    val height = DensityProvider.screenHeightPx.toFloat()
    val atBottom = touchPosition.y >= height - threshold
    return (touchPosition.x <= threshold && atBottom) ||
        (touchPosition.x >= width - threshold && atBottom)
}

fun clampVirtualMousePosition(position: Offset): Offset {
    return Offset(
        x = position.x.coerceIn(0f, DensityProvider.screenWidthPx.toFloat()),
        y = position.y.coerceIn(0f, DensityProvider.screenHeightPx.toFloat()),
    )
}
