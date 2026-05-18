package hunoia.sideleap.ui.widget

import androidx.compose.ui.geometry.Offset
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import hunoia.sideleap.settings.model.GestureSettings
import kotlin.math.hypot

fun virtualMouseInitialPosition(settings: GestureSettings.VirtualMouse): Offset {
    return Offset(
        x = ScreenUtils.getScreenWidth() * 0.5f,
        y = ScreenUtils.getScreenHeight() * settings.initialYRatio.coerceIn(0f, 1f),
    )
}

fun moveVirtualMouseCursor(
    position: Offset,
    dragAmount: Offset,
    settings: GestureSettings.VirtualMouse,
): Offset {
    val speed = hypot(dragAmount.x, dragAmount.y)
    val factor = 1f + settings.acceleration * (speed / 80f).coerceAtMost(2f)
    return clampVirtualMousePosition(
        Offset(
            x = position.x + dragAmount.x * settings.sensitivityX * factor,
            y = position.y + dragAmount.y * settings.sensitivityY * factor,
        )
    )
}

fun isVirtualMouseCancelGesture(
    touchPosition: Offset,
    settings: GestureSettings.VirtualMouse,
): Boolean {
    if (!touchPosition.x.isFinite() || !touchPosition.y.isFinite()) return false
    val threshold = ConvertUtils.dp2px(settings.edgeCancelThresholdDp.toFloat()).toFloat()
    val width = ScreenUtils.getScreenWidth().toFloat()
    val height = ScreenUtils.getScreenHeight().toFloat()
    return touchPosition.x <= threshold ||
        touchPosition.y <= threshold ||
        touchPosition.x >= width - threshold ||
        touchPosition.y >= height - threshold
}

private fun clampVirtualMousePosition(position: Offset): Offset {
    return Offset(
        x = position.x.coerceIn(0f, ScreenUtils.getScreenWidth().toFloat()),
        y = position.y.coerceIn(0f, ScreenUtils.getScreenHeight().toFloat()),
    )
}
