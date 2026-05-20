package hunoia.sideleap.ui.component

import android.graphics.Bitmap
import android.os.Build
import android.os.SystemClock
import android.view.ViewConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
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
import hunoia.sideleap.action.virtualMouseSettings
import hunoia.sideleap.gesture.application.VirtualMousePointerAction
import hunoia.sideleap.gesture.application.clampVirtualMousePosition
import hunoia.sideleap.gesture.application.isVirtualMouseCancelGesture
import hunoia.sideleap.gesture.application.isVirtualMouseWithinLongPressTolerance
import hunoia.sideleap.gesture.application.moveVirtualMouseCursor
import hunoia.sideleap.gesture.application.virtualMouseInitialPosition
import hunoia.sideleap.action.withRuntimeTouchPosition
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
import hunoia.sideleap.gesture.calcDirection
import hunoia.sideleap.gesture.canDistanceTriggered
import hunoia.sideleap.gesture.find
import hunoia.sideleap.gesture.getStickySlideValue
import hunoia.sideleap.gesture.getTriggerDirection
import hunoia.sideleap.gesture.isEmptyOrNone
import hunoia.sideleap.gesture.stickySlideValue
import hunoia.sideleap.gesture.styleBy
import hunoia.sideleap.system.vibration.tryVibrateForLongSlide
import hunoia.sideleap.system.vibration.tryVibrateForSlide
import hunoia.sideleap.ui.component.DragGestureHandler
import hunoia.sideleap.system.volumeDown
import hunoia.sideleap.system.volumeUp
import hunoia.sideleap.system.feedback.showVersionTooLowToast
import com.blankj.utilcode.util.ConvertUtils
import androidx.compose.ui.graphics.Color
import hunoia.sideleap.action.payload.SubGestureActionData
import hunoia.sideleap.core.serialization.JsonHelper
import hunoia.sideleap.settings.model.SubGesture
import hunoia.sideleap.settings.model.SubGestureSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/15
 */

@Composable
fun SideGestureContainer(
    onAction: (Action, GestureButton?) -> Unit,
    buttons: List<GestureButton>,
    modifier: Modifier = Modifier,
    imePadding: Int = 0,
    animationStyle: AnimationStyle? = WaveStyle(),
    actionPanelStyle: ActionPanelStyle = ArcStyle(),
    actionSettings: ActionSettings = ActionSettings(),
    advancedSettings: AdvancedSettings = AdvancedSettings(),
    gestureSettings: GestureSettings = GestureSettings(),
    onTakeScreenshot: (suspend () -> Bitmap?)? = null,
    onVirtualMouseStart: () -> Boolean = { false },
    onVirtualMouseEnd: () -> Unit = {},
    onVirtualMouseSettingsUpdate: (GestureSettings.VirtualMouse) -> Unit = {},
    virtualMousePreviousPosition: () -> Offset = { Offset.Unspecified },
    onPointerActionAtPosition: (Int, Int, Boolean, VirtualMousePointerAction) -> Unit = { _, _, _, _ -> },
    subGestureSettings: SubGestureSettings = SubGestureSettings(),
    onSubGestureModeChanged: (Boolean) -> Unit = {},

    wallpaperChangeTrigger: Long = 0L,
) {
    val context = LocalContext.current
    val curOnAction by rememberUpdatedState(newValue = onAction)
    val curOnVirtualMouseStart by rememberUpdatedState(newValue = onVirtualMouseStart)
    val curOnVirtualMouseEnd by rememberUpdatedState(newValue = onVirtualMouseEnd)
    val curOnVirtualMouseSettingsUpdate by rememberUpdatedState(newValue = onVirtualMouseSettingsUpdate)
    val curOnPointerActionAtPosition by rememberUpdatedState(newValue = onPointerActionAtPosition)
    val curOnSubGestureModeChanged by rememberUpdatedState(newValue = onSubGestureModeChanged)
    val coroutineScope = rememberCoroutineScope()
    val sideGestureState = rememberSideGestureState(buttons, advancedSettings, gestureSettings)
    val actionPanelState = rememberActionPanelState()
    val moveScreenState = rememberMoveScreenState(gestureSettings, actionSettings.moveScreen)
    var isVirtualMouseMode by remember { mutableStateOf(false) }
    var cursorPosition by remember { mutableStateOf(virtualMouseInitialPosition(gestureSettings.virtualMouse)) }
    var virtualMouseSettings by remember { mutableStateOf(gestureSettings.virtualMouse) }
    var virtualMouseTouchPosition by remember { mutableStateOf(Offset.Unspecified) }
    var virtualMouseLeftCancelEdge by remember { mutableStateOf(false) }
    var virtualMouseClickPulseKey by remember { mutableStateOf(0) }
    var virtualMouseLongPressJob by remember { mutableStateOf<Job?>(null) }
    var virtualMouseLongPressTriggered by remember { mutableStateOf(false) }
    var virtualMouseLongPressAnchor by remember { mutableStateOf(Offset.Unspecified) }

    var isVolumeScrubMode by remember { mutableStateOf(false) }
    var volumeScrubAccumulator by remember { mutableStateOf(0f) }
    val volumeStepThreshold = remember { context.resources.displayMetrics.density * 18f }

    var activeSubGesture by remember { mutableStateOf<SubGesture?>(null) }
    var subGestureAccum by remember { mutableStateOf(Offset.Zero) }
    var subGestureDepth by remember { mutableIntStateOf(0) }
    var subGestureTouchCount by remember { mutableIntStateOf(0) }
    var subGestureTimeoutJob by remember { mutableStateOf<Job?>(null) }
    val subGestureThresholdPx = remember { ConvertUtils.dp2px(30f).toFloat() }

    fun scheduleVirtualMouseLongPress() {
        virtualMouseLongPressJob?.cancel()
        virtualMouseLongPressJob = null
        val settings = virtualMouseSettings
        if (!settings.longPressEnabled || settings.longPressDelayMs <= 0L || virtualMouseLongPressTriggered) return
        virtualMouseLongPressAnchor = virtualMouseTouchPosition
        virtualMouseLongPressJob = coroutineScope.launch {
            delay(settings.longPressDelayMs)
            if (!isVirtualMouseMode || virtualMouseLongPressTriggered) return@launch
            if (!isVirtualMouseWithinLongPressTolerance(virtualMouseLongPressAnchor, virtualMouseTouchPosition, settings)) return@launch
            val target = cursorPosition
            virtualMouseLongPressTriggered = true
            virtualMouseClickPulseKey += 1
            curOnPointerActionAtPosition(
                target.x.roundToInt(),
                target.y.roundToInt(),
                settings.continuousMode,
                VirtualMousePointerAction.LongPress,
            )
        }
    }

    fun startVirtualMouseMode(action: Action): Boolean {
        if (isVirtualMouseMode) return false
        if (!curOnVirtualMouseStart()) return false
        virtualMouseSettings = action.virtualMouseSettings(gestureSettings.virtualMouse)
        curOnVirtualMouseSettingsUpdate(virtualMouseSettings)
        cursorPosition = virtualMouseInitialPosition(virtualMouseSettings, virtualMousePreviousPosition())
        virtualMouseTouchPosition = sideGestureState.finger
        virtualMouseLeftCancelEdge = false
        virtualMouseLongPressTriggered = false
        virtualMouseLongPressAnchor = Offset.Unspecified
        isVirtualMouseMode = true
        scheduleVirtualMouseLongPress()
        return true
    }

    fun finishVirtualMouseMode(click: Boolean) {
        if (!isVirtualMouseMode) return
        virtualMouseLongPressJob?.cancel()
        virtualMouseLongPressJob = null
        val target = cursorPosition
        isVirtualMouseMode = false
        if (click && !virtualMouseLongPressTriggered) {
            virtualMouseClickPulseKey += 1
            curOnPointerActionAtPosition(
                target.x.roundToInt(),
                target.y.roundToInt(),
                virtualMouseSettings.continuousMode,
                VirtualMousePointerAction.Click,
            )
        } else if (!virtualMouseLongPressTriggered) {
            curOnVirtualMouseEnd()
        }
        virtualMouseTouchPosition = Offset.Unspecified
        virtualMouseLeftCancelEdge = false
        virtualMouseLongPressTriggered = false
        virtualMouseLongPressAnchor = Offset.Unspecified
    }

    fun clearSubGestureMode() {
        activeSubGesture = null
        subGestureAccum = Offset.Zero
        subGestureDepth = 0
        subGestureTouchCount = 0
        subGestureTimeoutJob?.cancel()
        subGestureTimeoutJob = null
        curOnSubGestureModeChanged(false)
    }

    fun scheduleSubGestureTimeout() {
        subGestureTimeoutJob?.cancel()
        subGestureTimeoutJob = coroutineScope.launch {
            delay(gestureSettings.subGestureTimeoutMs)
            clearSubGestureMode()
        }
    }

    fun restartSubGestureTimeout() {
        scheduleSubGestureTimeout()
    }

    fun tryEnterSubGesture(action: Action): Boolean {
        if (action.value != GlobalActions.SUB_GESTURE) return false
        val id = try {
            JsonHelper.decodeFromString<SubGestureActionData>(action.data).id
        } catch (_: Exception) { return false }
        val target = subGestureSettings.subGestures.firstOrNull { it.id == id && it.enabled }
            ?: return true
        if (subGestureDepth >= 3) return true
        activeSubGesture = target
        subGestureAccum = Offset.Zero
        subGestureTouchCount = 0
        subGestureDepth += 1
        scheduleSubGestureTimeout()
        sideGestureState.cancel()
        curOnSubGestureModeChanged(true)
        return true
    }

    fun handleResolvedAction(action: Action, sourceButton: GestureButton?, touchPosition: Offset) {
        if (tryEnterSubGesture(action)) return
        curOnAction(action.withTouchPosition(touchPosition), sourceButton)
    }

    SideEffect {
        sideGestureState.onLongPress = { action ->
            handleResolvedAction(action, sideGestureState.button, sideGestureState.finger)
            sideGestureState.cancel()
        }
    }

    DragGestureHandler(
        onDragStart = onDragStart@{ offset ->
            if (activeSubGesture != null) {
                subGestureAccum = Offset.Zero
                restartSubGestureTimeout()
                return@onDragStart
            }
            sideGestureState.onDragStart(offset, imePadding)
        },
        onDrag = onDrag@{ dragAmount ->
            if (activeSubGesture != null) {
                subGestureAccum += dragAmount
                if (hypot(subGestureAccum.x, subGestureAccum.y) >= subGestureThresholdPx) {
                    val direction = activeSubGesture!!.angle.directionOf(subGestureAccum)
                    val action = activeSubGesture!!.actionFor(direction)
                    subGestureAccum = Offset.Zero
                    if (action != null && action != Action.NONE) {
                        handleResolvedAction(action, sideGestureState.button, sideGestureState.finger)
                        if (action.value != GlobalActions.SUB_GESTURE) clearSubGestureMode()
                    }
                }
                return@onDrag
            }
            if (isVirtualMouseMode) {
                virtualMouseTouchPosition = virtualMouseTouchPosition + dragAmount
                val stillForLongPress = isVirtualMouseWithinLongPressTolerance(
                    virtualMouseLongPressAnchor,
                    virtualMouseTouchPosition,
                    virtualMouseSettings
                )
                if (!stillForLongPress && !virtualMouseLongPressTriggered) {
                    scheduleVirtualMouseLongPress()
                }
                val inCancelEdge = virtualMouseSettings.continuousMode &&
                    isVirtualMouseCancelGesture(virtualMouseTouchPosition, virtualMouseSettings)
                if (!inCancelEdge) {
                    virtualMouseLeftCancelEdge = true
                } else if (virtualMouseLeftCancelEdge) {
                    finishVirtualMouseMode(click = false)
                    return@onDrag
                }
                cursorPosition = moveVirtualMouseCursor(cursorPosition, dragAmount, virtualMouseSettings)
                return@onDrag
            }
            if (isVolumeScrubMode) {
                volumeScrubAccumulator += dragAmount.y
                while (volumeScrubAccumulator >= volumeStepThreshold) {
                    context.volumeDown()
                    volumeScrubAccumulator -= volumeStepThreshold
                }
                while (volumeScrubAccumulator <= -volumeStepThreshold) {
                    context.volumeUp()
                    volumeScrubAccumulator += volumeStepThreshold
                }
                return@onDrag
            }
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
                            actionPanelState.ready(
                                button.position,
                                actions,
                                button.longSlideActionPanelStyles.styleBy(sideGestureState.triggerDirection).value
                            )
                            sideGestureState.cancel()
                    } else if (actions.isNotEmpty()) {
                        val action = actions.first()
                        if (action.value == GlobalActions.VOLUME_SCRUB) {
                            isVolumeScrubMode = true
                            volumeScrubAccumulator = 0f
                            sideGestureState.cancel()
                        } else if (action.value == GlobalActions.VIRTUAL_MOUSE) {
                            startVirtualMouseMode(action)
                            sideGestureState.cancel()
                        } else if (action.value == GlobalActions.MOVE_SCREEN) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                                showVersionTooLowToast(context, R.string.action_move_screen)
                                sideGestureState.cancel()
                                return@onDrag
                            }
                            moveScreenState.onDragStart(sideGestureState.finger)
                            sideGestureState.cancel()
                        } else {
                            handleResolvedAction(action, button, sideGestureState.finger)
                            sideGestureState.cancel()
                        }
                    }
                } else {
                    sideGestureState.cancel()
                }
            }
        },
        onDragEnd = onDragEnd@{
            if (activeSubGesture != null) {
                subGestureAccum = Offset.Zero
                subGestureTouchCount += 1
                if (subGestureTouchCount >= 5) clearSubGestureMode()
                return@onDragEnd
            }
            if (isVirtualMouseMode) {
                finishVirtualMouseMode(click = true)
                return@onDragEnd
            }
            if (isVolumeScrubMode) {
                isVolumeScrubMode = false
                volumeScrubAccumulator = 0f
                return@onDragEnd
            }
            if (actionPanelState.visible) {
                val touchPosition = actionPanelState.finger
                val action = actionPanelState.done()
                actionPanelState.onDragEnd()
                handleResolvedAction(action, sideGestureState.button, touchPosition)
            }
            if (moveScreenState.visible) {
                val touchPosition = moveScreenState.finger
                val action = moveScreenState.done()
                moveScreenState.onDragEnd()
                handleResolvedAction(action, sideGestureState.button, touchPosition)
            }

            if (!sideGestureState.isCanceled) {
                val touchPosition = sideGestureState.finger
                val sourceButton = sideGestureState.button
                val action = sideGestureState.onDragEnd()

                if (action.value == GlobalActions.BACK) {
                    coroutineScope.launch {
                        delay(advancedSettings.backActionDelayMs)
                        handleResolvedAction(action, sourceButton, touchPosition)
                    }
                } else {
                    handleResolvedAction(action, sourceButton, touchPosition)
                }
            }
        },
        onDragCancel = onDragCancel@{
            if (activeSubGesture != null) {
                clearSubGestureMode()
                return@onDragCancel
            }
            if (isVirtualMouseMode) {
                finishVirtualMouseMode(click = false)
                return@onDragCancel
            }
            if (isVolumeScrubMode) {
                isVolumeScrubMode = false
                volumeScrubAccumulator = 0f
                return@onDragCancel
            }
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
            key(wallpaperChangeTrigger) {
                GestureAnimation(
                    modifier = Modifier.matchParentSize(),
                    animationStyle = animationStyle,
                    SideGestureState = sideGestureState
                )
            }
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

        if (isVirtualMouseMode) {
            VirtualMouseCursor(
                position = cursorPosition,
                modifier = Modifier.matchParentSize(),
                settings = virtualMouseSettings,
                clickPulseKey = virtualMouseClickPulseKey,
            )
        }
    }
}

private fun Action.withTouchPosition(position: Offset): Action {
    if (!position.x.isFinite() || !position.y.isFinite()) return this
    return withRuntimeTouchPosition(position.x.roundToInt(), position.y.roundToInt())
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

    private val curStickySlideValue: Float
        get() = stickySlideValue(advancedSettings.animationStyles)

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

        val longPressAction = button.slideActions.center2.firstOrNull()
        if (longPressAction != null && longPressAction != Action.NONE) {
            calcLongPressJob = coroutineScope.launch {
                delay(gestureSettings.longPressTriggerDelayMs)
                gestureSettings.vibrations.tryVibrateForSlide()
                onLongPress(longPressAction)
            }
        }

        originXAnimVal = offset.x
        originYAnimVal = offset.y

        when (button.position) {
            Position.Left, Position.Right -> {
                fingerXDisplay = getStickySlideValue(button, curStickySlideValue, true)
                fingerYDisplay = offset.y
            }
            Position.Bottom -> {
                fingerXDisplay = offset.x
                fingerYDisplay = getStickySlideValue(button, curStickySlideValue, false)
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

        fingerXDisplay += dragAmount.x
        fingerYDisplay += dragAmount.y

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
                        gestureSettings.vibrations.tryVibrateForSlide()
                    }
                    returnAction = tapAction
                }
            }
        }

        val startX = fingerXDisplay
        val startY = fingerYDisplay
        val (targetX, targetY) = retractTarget(button, curStickySlideValue)
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
            val (t1, t2) = retractTarget(btn, curStickySlideValue)
            tx = t1; ty = t2
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

    fun canDistanceTriggered(button: GestureButton, isLongSlide: Boolean, judgeAction: Boolean = true): Boolean {
        return hunoia.sideleap.gesture.canDistanceTriggered(button, origin, finger, triggerDirection, gestureSettings, isLongSlide, curStickySlideValue, judgeAction)
    }

    private fun retractTarget(button: GestureButton, stickyValue: Float): Pair<Float, Float> {
        return when (button.position) {
            Position.Left, Position.Right -> getStickySlideValue(button, stickyValue, true) to origin.y
            Position.Bottom -> origin.x to getStickySlideValue(button, stickyValue, false)
        }
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

