package hunoia.luno.ui.component

import android.os.SystemClock
import android.view.ViewConfiguration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import hunoia.luno.action.Action
import hunoia.luno.gesture.GestureButton
import hunoia.luno.gesture.Position
import hunoia.luno.gesture.TriggerDirection
import hunoia.luno.gesture.TriggerDirection.Center
import hunoia.luno.gesture.TriggerDirection.Center2
import hunoia.luno.gesture.TriggerDirection.Down2
import hunoia.luno.gesture.TriggerDirection.Up2
import hunoia.luno.gesture.actionsBy
import hunoia.luno.gesture.bounds
import hunoia.luno.gesture.calcDirection
import hunoia.luno.gesture.canDistanceTriggered
import hunoia.luno.gesture.find
import hunoia.luno.gesture.getStickySlideValue
import hunoia.luno.gesture.stickySlideValue
import hunoia.luno.settings.model.AdvancedSettings
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.system.vibration.tryVibrateForLongPress
import hunoia.luno.system.vibration.tryVibrateForLongSlide
import hunoia.luno.system.vibration.tryVibrateForSlide
import hunoia.luno.system.vibration.tryVibrateForTap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.hypot

class SideGestureState(
    private val coroutineScope: CoroutineScope,
    private val buttons: List<GestureButton>,
    private val advancedSettings: AdvancedSettings = AdvancedSettings(),
    private val gestureSettings: GestureSettings = GestureSettings()
) {

    var isCanceled: Boolean by mutableStateOf(false)
        private set

    var button: GestureButton? by mutableStateOf(null)
        private set
    var triggerDirection: TriggerDirection by mutableStateOf(Center2)
        private set

    var origin = Offset.Unspecified
        private set
    var finger = Offset.Unspecified
        private set
    private var buttonBounds: Rect? = null

    data class AnimState(
        val originX: Float = Float.NaN,
        val originY: Float = Float.NaN,
        val fingerX: Float = Float.NaN,
        val fingerY: Float = Float.NaN,
    )

    private var animState by mutableStateOf(AnimState())

    val originXAnimVal: Float get() = animState.originX
    val originYAnimVal: Float get() = animState.originY
    val fingerXAnimVal: Float get() = animState.fingerX
    val fingerYAnimVal: Float get() = animState.fingerY

    var onLongPress: (Action) -> Unit = {}

    private var longSlideFirstTriggerMs = 0L
    private var calcLongPressJob: Job? = null

    private val curStickySlideValue: Float
        get() = stickySlideValue(advancedSettings.animationStyles)

    private var isOhoGestureEverCanTriggered = false

    private var slideVibrationFlags = false

    private val viewConfiguration = ViewConfiguration.get(hunoia.luno.core.AppContext.get())

    fun onDragStart(offset: Offset, imePadding: Int) {
        isCanceled = false
        origin = offset
        finger = offset
        button = buttons.find(offset, imePadding)
        buttonBounds = button?.bounds(imePadding)

        val button = button ?: run {
            animState = AnimState()
            return
        }
        val gestureSettings = gestureSettings

        val longPressAction = button.slideActions.center2.firstOrNull()
        if (longPressAction != null && longPressAction != Action.NONE) {
            calcLongPressJob = coroutineScope.launch {
                delay(gestureSettings.longPressTriggerDelayMs)
                gestureSettings.vibrations.tryVibrateForLongPress()
                onLongPress(longPressAction)
            }
        }

        val (fx, fy) = when (button.position) {
            Position.Left, Position.Right -> getStickySlideValue(button, curStickySlideValue, true) to offset.y
            Position.Bottom -> offset.x to getStickySlideValue(button, curStickySlideValue, false)
        }
        animState = AnimState(originX = offset.x, originY = offset.y, fingerX = fx, fingerY = fy)
    }

    fun onDrag(dragAmount: Offset): List<Action>? {
        finger += dragAmount

        val touchSlop = viewConfiguration.scaledTouchSlop
        val minus = finger - origin
        if (calcLongPressJob?.isActive == true &&
            (minus.x.absoluteValue > touchSlop ||
            minus.y.absoluteValue > touchSlop)
        ) {
            calcLongPressJob?.cancel()
        }

        val button = button ?: return null
        val newDirection = calcDirection(button, origin, finger, buttonBounds, gestureSettings) ?: return null
        triggerDirection = newDirection

        val gestureSettings = gestureSettings
        if (gestureSettings.isPreciseSlideType) {
            if (newDirection == Center) {
                if (!isOhoGestureEverCanTriggered) {
                    isOhoGestureEverCanTriggered = canDistanceTriggered(button, origin, finger, newDirection, gestureSettings, false, curStickySlideValue, judgeAction = false)
                }
            } else if (isOhoGestureEverCanTriggered &&
                (newDirection == Up2 || newDirection == Down2)
            ) {
                return null
            }
        }

        val prev = animState
        animState = prev.copy(fingerX = prev.fingerX + dragAmount.x, fingerY = prev.fingerY + dragAmount.y)

        val canTriggerLong = canDistanceTriggered(button, origin, finger, triggerDirection, gestureSettings, true, curStickySlideValue)
        if (canTriggerLong) {
            val longSlideDelayMs = gestureSettings.longSlideTriggerDelayMs
            val timeMs = SystemClock.uptimeMillis()
            if (longSlideFirstTriggerMs == 0L) {
                longSlideFirstTriggerMs = timeMs
            } else if (timeMs - longSlideFirstTriggerMs >= longSlideDelayMs) {
                val actions = button.longSlideActions.actionsBy(newDirection)
                if (gestureSettings.longSlideTriggerImmediately) {
                    gestureSettings.vibrations.tryVibrateForLongSlide()
                    return actions
                }
            }
        } else {
            longSlideFirstTriggerMs = 0L
        }

        if (gestureSettings.vibrations.vibrateImmediately &&
            !slideVibrationFlags && canDistanceTriggered(button, origin, finger, triggerDirection, gestureSettings, false, curStickySlideValue)
        ) {
            slideVibrationFlags = true
            gestureSettings.vibrations.tryVibrateForSlide()
        }

        return emptyList()
    }

    fun onDragEnd(): Action {
        calcLongPressJob?.cancel()
        val button = button ?: return Action.NONE
        val gestureSettings = gestureSettings
        val triggerDirection = triggerDirection
        val longSlideDelayMs = gestureSettings.longSlideTriggerDelayMs
        var returnAction = Action.NONE
        if (!gestureSettings.longSlideTriggerImmediately &&
            canDistanceTriggered(button, origin, finger, triggerDirection, gestureSettings, true, curStickySlideValue) &&
            SystemClock.uptimeMillis() - longSlideFirstTriggerMs >= longSlideDelayMs
        ) {
            val actions = button.longSlideActions.actionsBy(triggerDirection)
            val action = actions.firstOrNull()
            if (action != null && action != Action.NONE) {
                gestureSettings.vibrations.tryVibrateForLongSlide()
                returnAction = action
            }
        } else if (canDistanceTriggered(button, origin, finger, triggerDirection, gestureSettings, false, curStickySlideValue)) {
            val actions = button.slideActions.actionsBy(triggerDirection)
            val action = actions.firstOrNull()
            if (action != null && action != Action.NONE) {
                if (!slideVibrationFlags) {
                    gestureSettings.vibrations.tryVibrateForSlide()
                }
                returnAction = action
            }
        }

        if (returnAction == Action.NONE) {
            val distance = hypot(finger.x - origin.x, finger.y - origin.y)
            if (distance <= viewConfiguration.scaledTouchSlop) {
                val tapAction = button.tapActions.center.firstOrNull()
                if (tapAction != null && tapAction != Action.NONE) {
                    if (!slideVibrationFlags) {
                        gestureSettings.vibrations.tryVibrateForTap()
                    }
                    returnAction = tapAction
                }
            }
        }

        reset()
        return returnAction
    }

    fun cancel() {
        if (isCanceled) return
        reset()
        isCanceled = true
    }

    fun onDragCancel() {
        reset()
    }

    fun reset() {
        calcLongPressJob?.cancel()
        calcLongPressJob = null
        isCanceled = false
        origin = Offset.Unspecified
        finger = Offset.Unspecified
        animState = AnimState()
        longSlideFirstTriggerMs = 0L
        isOhoGestureEverCanTriggered = false
        slideVibrationFlags = false
    }

    fun canDistanceTriggered(button: GestureButton, isLongSlide: Boolean, judgeAction: Boolean = true): Boolean {
        return hunoia.luno.gesture.canDistanceTriggered(button, origin, finger, triggerDirection, gestureSettings, isLongSlide, curStickySlideValue, judgeAction)
    }
}
