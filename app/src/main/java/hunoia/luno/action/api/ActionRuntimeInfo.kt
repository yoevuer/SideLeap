package hunoia.luno.action.api

import hunoia.luno.config.model.Action
import hunoia.luno.action.TriggerType

data class ActionRuntimeInfo(
    val triggerType: TriggerType? = null,
    val touchX: Int? = null,
    val touchY: Int? = null,
)

fun Action.withRuntimeTouchPosition(x: Int, y: Int): Action {
    val runtimeInfo = when (val current = extra) {
        is ActionRuntimeInfo -> current.copy(touchX = x, touchY = y)
        is TriggerType -> ActionRuntimeInfo(triggerType = current, touchX = x, touchY = y)
        else -> ActionRuntimeInfo(touchX = x, touchY = y)
    }
    return copy(extra = runtimeInfo)
}

fun Action.runtimeTriggerType(): TriggerType? {
    return when (val current = extra) {
        is ActionRuntimeInfo -> current.triggerType
        is TriggerType -> current
        else -> null
    }
}

fun Action.runtimeTouchPosition(): Pair<Int, Int>? {
    val runtimeInfo = extra as? ActionRuntimeInfo ?: return null
    val x = runtimeInfo.touchX ?: return null
    val y = runtimeInfo.touchY ?: return null
    return x to y
}
