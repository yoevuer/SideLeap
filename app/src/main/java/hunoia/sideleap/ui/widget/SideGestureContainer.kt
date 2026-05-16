package hunoia.sideleap.ui.widget

import android.graphics.Bitmap
import android.os.Build
import android.os.SystemClock
import android.view.ViewConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import hunoia.sideleap.R
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.settings.model.ActionPanelStyle
import hunoia.sideleap.settings.model.ActionPanelStyles
import hunoia.sideleap.settings.model.AnimationStyle
import hunoia.sideleap.settings.model.ArcStyle
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.gesture.TriggerDirection
import hunoia.sideleap.gesture.TriggerDirection.Center
import hunoia.sideleap.gesture.TriggerDirection.Center2
import hunoia.sideleap.gesture.TriggerDirection.Down
import hunoia.sideleap.gesture.TriggerDirection.Down2
import hunoia.sideleap.gesture.TriggerDirection.Up
import hunoia.sideleap.gesture.TriggerDirection.Up2
import hunoia.sideleap.settings.model.WaveStyle
import hunoia.sideleap.settings.model.ActionSettings
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.gesture.GESTURE_ANGLE_BASE
import hunoia.sideleap.gesture.actionsBy
import hunoia.sideleap.gesture.bounds
import hunoia.sideleap.gesture.find
import hunoia.sideleap.gesture.getTriggerDirection
import hunoia.sideleap.gesture.isEmptyOrNone
import hunoia.sideleap.system.api.tryVibrateForLongSlide
import hunoia.sideleap.system.api.tryVibrateForSlide
import hunoia.sideleap.ui.widget.DragGestureHandler
import hunoia.sideleap.system.feedback.showVersionTooLowToast
import com.blankj.utilcode.util.ConvertUtils
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan
import kotlin.math.hypot

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/15
 */

@Composable
fun SideGestureContainer(
    onAction: (Action) -> Unit,
    buttons: List<GestureButton>,
    modifier: Modifier = Modifier,
    imePadding: Int = 0,
    animationStyle: AnimationStyle? = WaveStyle(),
    actionPanelStyle: ActionPanelStyle = ArcStyle(),
    actionSettings: ActionSettings = ActionSettings(),
    advancedSettings: AdvancedSettings = AdvancedSettings(),
    gestureSettings: GestureSettings = GestureSettings(),
    onTakeScreenshot: (suspend () -> Bitmap?)? = null
) {
    val context = LocalContext.current
    val curOnAction by rememberUpdatedState(newValue = onAction)
    val sideGestureState = rememberSideGestureState(buttons, advancedSettings, gestureSettings)
    val actionPanelState = rememberActionPanelState()
    val moveScreenState = rememberMoveScreenState(gestureSettings, actionSettings.moveScreen)

    SideEffect {
        sideGestureState.onLongPress = { action ->
            curOnAction(action)
            sideGestureState.cancel()
        }
    }

    DragGestureHandler(
        onDragStart = onDragStart@{ offset ->
            sideGestureState.onDragStart(offset, imePadding)
        },
        onDrag = onDrag@{ dragAmount ->
            if (actionPanelState.visible) {
                actionPanelState.onDrag(dragAmount)
                return@onDrag
            }
            if (moveScreenState.visible) {
                moveScreenState.onDrag(dragAmount)
                return@onDrag
            }
            if (!sideGestureState.isCanceled) {
                val actions = sideGestureState.onDrag(dragAmount)
                val button = sideGestureState.button
                if (button != null && actions != null) {
                    if (actions.size > 1) {
                        actionPanelState.onDragStart(sideGestureState.finger)
                        actionPanelState.ready(button.position, actions)
                        sideGestureState.cancel()
                    } else if (actions.isNotEmpty()) {
                        if (actions.first().value == GlobalActions.MOVE_SCREEN) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                                showVersionTooLowToast(context, R.string.action_move_screen)
                                sideGestureState.cancel()
                                return@onDrag
                            }
                            moveScreenState.onDragStart(sideGestureState.finger)
                            sideGestureState.cancel()
                        } else {
                            curOnAction(actions.first())
                            sideGestureState.cancel()
                        }
                    }
                } else {
                    sideGestureState.cancel()
                }
            }
        },
        onDragEnd = onDragEnd@{
            if (actionPanelState.visible) {
                val action = actionPanelState.done()
                actionPanelState.onDragEnd()
                curOnAction(action)
            }
            if (moveScreenState.visible) {
                val action = moveScreenState.done()
                moveScreenState.onDragEnd()
                curOnAction(action)
            }

            if (!sideGestureState.isCanceled) {
                val action = sideGestureState.onDragEnd()
                curOnAction(action)
            }
        },
        onDragCancel = onDragCancel@{
            if (actionPanelState.visible) {
                actionPanelState.onDragCancel()
            }
            if (moveScreenState.visible) {
                moveScreenState.onDragCancel()
            }
            sideGestureState.onDragCancel()
        }
    )
    Box(modifier = modifier) {
        ActionPanel(
            actionPanelStyle = actionPanelStyle,
            actionPanelState = actionPanelState,
            modifier = Modifier.matchParentSize(),
            longPressLaunchPopup = advancedSettings.actionPanelAppLongPressLaunchPopup,
            vibrations = gestureSettings.vibrations
        )

        if (!moveScreenState.visible && animationStyle != null) {
            GestureAnimation(
                modifier = Modifier.matchParentSize(),
                animationStyle = animationStyle,
                SideGestureState = sideGestureState
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && moveScreenState.visible) {
            var screenshot by remember { mutableStateOf<Bitmap?>(null) }
            LaunchedEffect(Unit) {
                delay(20)
                screenshot = onTakeScreenshot?.invoke()
            }
            val ss = screenshot
            if (ss != null) {
                MoveScreen(
                    modifier = Modifier.matchParentSize(),
                    screenshot = ss,
                    state = moveScreenState
                )
            } else {
                Box(Modifier.matchParentSize().background(Color.Black))
            }
        }
    }
}

@Composable
private fun rememberSideGestureState(
    buttons: List<GestureButton>,
    advancedSettings: AdvancedSettings = AdvancedSettings(),
    gestureSettings: GestureSettings = GestureSettings()
): SideGestureState {
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope, buttons, advancedSettings, gestureSettings) {
        SideGestureState(coroutineScope, buttons, advancedSettings, gestureSettings)
    }
}

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

    var originXAnimVal by mutableStateOf(Float.NaN); private set
    var originYAnimVal by mutableStateOf(Float.NaN); private set
    val fingerXAnimVal: Float get() = fingerXDisplay
    val fingerYAnimVal: Float get() = fingerYDisplay
    private var fingerXDisplay by mutableStateOf(Float.NaN)
    private var fingerYDisplay by mutableStateOf(Float.NaN)

    var onLongPress: (Action) -> Unit = {}

    private var longSlideFirstTriggerMs = 0L
    private var calcLongPressJob: Job? = null

    private val stickySlideValue = run {
        val waveStyle = advancedSettings.animationStyles.value as? WaveStyle
        if (waveStyle?.stickySlideEnabled == true) {
            ConvertUtils.dp2px(36f) .toFloat()
        } else 0f
    }

    /**
     * 区分上下滑和侧滑，当可以触发侧滑时，即使后面触发方向变成上下滑也需要取消手势
     */
    private var isOhoGestureEverCanTriggered = false

    private var slideVibrationFlags = false
    private var animationResetJob: Job? = null

    private val viewConfiguration = ViewConfiguration.get(hunoia.sideleap.core.AppContext.get())

    fun onDragStart(offset: Offset, imePadding: Int) {
        animationResetJob?.cancel()
        isCanceled = false
        origin = offset
        finger = offset
        button = buttons.find(offset, imePadding)
        buttonBounds = button?.bounds(imePadding)

        val button = button ?: return
        val gestureSettings = gestureSettings

        val action = button.slideActions.center2.firstOrNull()
        if (action != null && action != Action.NONE) {
            calcLongPressJob = coroutineScope.launch {
                delay(gestureSettings.longPressTriggerDelayMs)
                gestureSettings.vibrations.tryVibrateForSlide()
                onLongPress(action)
            }
        }

        originXAnimVal = offset.x
        originYAnimVal = offset.y

        when (button.position) {
            Position.Left, Position.Right -> {
                fingerXDisplay = getStickySlideValue(button, true)
                fingerYDisplay = offset.y
            }
            Position.Bottom -> {
                fingerXDisplay = offset.x
                fingerYDisplay = getStickySlideValue(button, false)
            }
        }
    }

    /**
     * @return 返回null表示不识别任何手势，emptyList()表示还没触发动作，
     * 长列表表示触发长动作，否则表示触发一个动作
     */
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

        // 理论上能到这里button不应该为空
        val button = button ?: return null
        // 没触发方向，这一轮不再识别手势
        val newDirection = calcDirection(button) ?: return null
        triggerDirection = newDirection

        val gestureSettings = gestureSettings
        if (gestureSettings.isPreciseSlideType) {
            if (newDirection == Center) {
                if (!isOhoGestureEverCanTriggered) {
                    isOhoGestureEverCanTriggered = canDistanceTriggered(button, isLongSlide = false, judgeAction = false)
                }
            } else if (isOhoGestureEverCanTriggered &&
                (newDirection == Up2 || newDirection == Down2)
            ) {
                return null
            }
        }

        fingerXDisplay += dragAmount.x
        fingerYDisplay += dragAmount.y

        val canTriggerLong = canDistanceTriggered(button, true)
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
            !slideVibrationFlags && canDistanceTriggered(button, false)
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
            canDistanceTriggered(button, true) &&
            SystemClock.uptimeMillis() - longSlideFirstTriggerMs >= longSlideDelayMs
        ) {
            val actions = button.longSlideActions.actionsBy(triggerDirection)
            val action = actions.firstOrNull()
            if (action != null && action != Action.NONE) {
                gestureSettings.vibrations.tryVibrateForLongSlide()
                returnAction = action
            }
        } else if (canDistanceTriggered(button, false)) {
            val actions = button.slideActions.actionsBy(triggerDirection)
            val action = actions.firstOrNull()
            if (action != null && action != Action.NONE) {
                if (!slideVibrationFlags) {
                    gestureSettings.vibrations.tryVibrateForSlide()
                }
                returnAction = action
            }
        }

        val startX = fingerXDisplay
        val startY = fingerYDisplay
        val targetX = when (button.position) {
            Position.Left, Position.Right -> getStickySlideValue(button, true)
            Position.Bottom -> origin.x
        }
        val targetY = when (button.position) {
            Position.Left, Position.Right -> origin.y
            Position.Bottom -> getStickySlideValue(button, false)
        }
        reset()
        animateDisplayBack(startX, startY, targetX, targetY)
        return returnAction
    }

    private fun animateDisplayBack(startX: Float, startY: Float, targetX: Float, targetY: Float) {
        if (startX.isNaN() || startY.isNaN()) return
        animationResetJob?.cancel()
        animationResetJob = coroutineScope.launch {
            val duration = 180L
            val startMs = SystemClock.uptimeMillis()
            while (true) {
                val elapsed = SystemClock.uptimeMillis() - startMs
                val fraction = (elapsed.toFloat() / duration).coerceAtMost(1f)
                val eased = 1f - (1f - fraction) * (1f - fraction)
                fingerXDisplay = startX + (targetX - startX) * eased
                fingerYDisplay = startY + (targetY - startY) * eased
                if (fraction >= 1f) break
                delay(16)
            }
            reset()
        }
    }

    fun onDragCancel() {
        reset()
    }

    fun cancel() {
        if (isCanceled) return
        animationResetJob?.cancel()
        val btn = button
        val sx = fingerXDisplay
        val sy = fingerYDisplay
        val tx: Float
        val ty: Float
        if (btn != null && !sx.isNaN() && !sy.isNaN()) {
            tx = when (btn.position) {
                Position.Left, Position.Right -> getStickySlideValue(btn, true)
                Position.Bottom -> origin.x
            }
            ty = when (btn.position) {
                Position.Left, Position.Right -> origin.y
                Position.Bottom -> getStickySlideValue(btn, false)
            }
        } else {
            tx = Float.NaN
            ty = Float.NaN
        }
        reset()
        isCanceled = true
        if (!tx.isNaN() && !ty.isNaN()) {
            animateDisplayBack(sx, sy, tx, ty)
        }
    }

    fun reset() {
        animationResetJob?.cancel()
        animationResetJob = null
        calcLongPressJob?.cancel()
        calcLongPressJob = null
        isCanceled = false
        origin = Offset.Unspecified
        finger = Offset.Unspecified
        longSlideFirstTriggerMs = 0L
        isOhoGestureEverCanTriggered = false
        slideVibrationFlags = false

        fingerXDisplay = Float.NaN
        fingerYDisplay = Float.NaN
    }

    /**
     * 手指划过的距离是否足够触发动作
     */
    fun canDistanceTriggered(
        button: GestureButton,
        isLongSlide: Boolean,
        judgeAction: Boolean = true
    ): Boolean {
        val gestureSettings = gestureSettings
        val slideAction = button.slideActions
        val longSlideAction = button.longSlideActions
        val originX = origin.x
        val originY = origin.y
        val fingerX = finger.x + getStickySlideValue(button, true)
        val fingerY = finger.y + getStickySlideValue(button, false)
        val triggerDirection = triggerDirection

        if (triggerDirection == Center2) return false

        val slideDistance = if (triggerDirection == Up2 || triggerDirection == Down2) {
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
        if (slideDistance < 0 && triggerDirection != Up2 && triggerDirection != Down2) {
            return false
        }

        val effectiveDistance = when (triggerDirection) {
            Center -> slideDistance
            Up, Down -> {
                val edge2 = when (button.position) {
                    Position.Left, Position.Right -> abs(fingerY - originY)
                    Position.Bottom -> abs(fingerX - originX)
                }
                hypot(slideDistance, edge2)
            }
            Up2, Down2 -> slideDistance.absoluteValue
            else -> return false
        }

        val triggerThreshold = if (isLongSlide) gestureSettings.longSlideTriggerDistance
        else gestureSettings.slideTriggerDistance
        val canTrigger = effectiveDistance >= triggerThreshold

        if (!judgeAction) return canTrigger

        val actionList = (if (isLongSlide) longSlideAction else slideAction).let { actions ->
            when (triggerDirection) {
                Center -> actions.center
                Up -> actions.up
                Down -> actions.down
                Up2 -> actions.up2
                Down2 -> actions.down2
                else -> return false
            }
        }
        return canTrigger && actionList.isEmptyOrNone().not()
    }

    private fun calcDirection(button: GestureButton): TriggerDirection? {
        val buttonBounds = buttonBounds ?: return null
        val origin = origin
        val finger = finger
        val opposite = when (button.position) {
            Position.Left -> finger.x - buttonBounds.left
            Position.Right -> buttonBounds.right - finger.x
            Position.Bottom -> buttonBounds.bottom - finger.y
        }
        val neighbor = when (button.position) {
            Position.Left, Position.Right -> abs(finger.y - origin.y)
            Position.Bottom -> abs(finger.x - origin.x)
        }
        if (neighbor == 0f) {
            val angle = when (button.position) {
                Position.Left -> gestureSettings.angles.left
                Position.Right -> gestureSettings.angles.right
                Position.Bottom -> gestureSettings.angles.bottom
            }
            return angle.getTriggerDirection(90f)
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
        val angle = when (button.position) {
            Position.Left -> gestureSettings.angles.left
            Position.Right -> gestureSettings.angles.right
            Position.Bottom -> gestureSettings.angles.bottom
        }
        return angle.getTriggerDirection(degree.toFloat())
    }

    private fun getStickySlideValue(button: GestureButton, isX: Boolean): Float {
        val stickySlideValue = stickySlideValue
        if (isX) {
            return when (button.position) {
                Position.Left -> -stickySlideValue
                else -> stickySlideValue
            }
        }
        return stickySlideValue
    }
}

abstract class LongSlideState {

    var origin: Offset by mutableStateOf(Offset.Unspecified)
        protected set
    var finger: Offset by mutableStateOf(Offset.Unspecified)
        protected set

    open fun onDragStart(offset: Offset) {
        origin = offset
        finger = offset
    }

    open fun onDrag(dragAmount: Offset) {
        finger += dragAmount
    }

    open fun onDragEnd() {
        reset()
    }

    open fun onDragCancel() {
        reset()
    }

    protected open fun reset() {
        origin = Offset.Unspecified
        finger = Offset.Unspecified
    }
}
