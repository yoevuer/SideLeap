package hunoia.sideleap.ui.component

import android.graphics.Bitmap
import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import hunoia.sideleap.R
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.gesture.application.VirtualMousePointerAction
import hunoia.sideleap.action.withRuntimeTouchPosition
import hunoia.sideleap.settings.model.ActionPanelStyle
import hunoia.sideleap.settings.model.ActionPanelStyles
import hunoia.sideleap.settings.model.ActionSettings
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.settings.model.AnimationStyle
import hunoia.sideleap.settings.model.ArcStyle
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.settings.model.WaveStyle
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.gesture.TriggerDirection
import hunoia.sideleap.gesture.styleBy
import hunoia.sideleap.system.vibration.tryVibrateForSubGesture
import hunoia.sideleap.ui.component.DragGestureHandler
import hunoia.sideleap.system.volumeDown
import hunoia.sideleap.system.volumeUp
import hunoia.sideleap.system.feedback.showVersionTooLowToast
import androidx.compose.ui.graphics.Color
import hunoia.sideleap.action.payload.SubGestureActionData
import hunoia.sideleap.core.serialization.JsonHelper
import hunoia.sideleap.settings.model.SubGesture
import hunoia.sideleap.settings.model.SubGestureSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val vmHandle = rememberVirtualMouseHandle(
        gestureSettings = gestureSettings,
        onVirtualMouseStart = { curOnVirtualMouseStart() },
        onVirtualMouseEnd = { curOnVirtualMouseEnd() },
        onVirtualMouseSettingsUpdate = { settings -> curOnVirtualMouseSettingsUpdate(settings) },
        virtualMousePreviousPosition = { virtualMousePreviousPosition() },
        onPointerActionAtPosition = { x, y, keepActive, action ->
            curOnPointerActionAtPosition(x, y, keepActive, action)
        },
    )

    var isVolumeScrubMode by remember { mutableStateOf(false) }
    var volumeScrubAccumulator by remember { mutableStateOf(0f) }
    var volumeScrubAccumulatorX by remember { mutableStateOf(0f) }
    val volumeStepThreshold = remember(actionSettings.volumeScrub.stepThresholdDp) { context.resources.displayMetrics.density * actionSettings.volumeScrub.stepThresholdDp }

    var activeSubGesture by remember { mutableStateOf<SubGesture?>(null) }
    var subGestureAccum by remember { mutableStateOf(Offset.Zero) }
    var subGestureDepth by remember { mutableIntStateOf(0) }
    var subGestureTouchCount by remember { mutableIntStateOf(0) }
    var subGestureTimeoutJob by remember { mutableStateOf<Job?>(null) }
    val subGestureThresholdPx = remember(gestureSettings.subGestureTriggerDistance) { gestureSettings.subGestureTriggerDistance.toFloat() }

    fun clearSubGestureMode(notifyService: Boolean = true) {
        activeSubGesture = null
        subGestureAccum = Offset.Zero
        subGestureDepth = 0
        subGestureTouchCount = 0
        subGestureTimeoutJob?.cancel()
        subGestureTimeoutJob = null
        if (notifyService) curOnSubGestureModeChanged(false)
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
                    gestureSettings.vibrations.tryVibrateForSubGesture()
                    if (action != null && action != Action.NONE) {
                        when (action.value) {
                            GlobalActions.VOLUME_SCRUB -> {
                                isVolumeScrubMode = true
                                volumeScrubAccumulator = 0f
                                volumeScrubAccumulatorX = 0f
                                sideGestureState.cancel()
                                clearSubGestureMode(notifyService = false)
                            }
                            GlobalActions.VIRTUAL_MOUSE -> {
                                vmHandle.start(action, sideGestureState.finger, virtualMousePreviousPosition())
                                sideGestureState.cancel()
                                clearSubGestureMode(notifyService = false)
                            }
                            GlobalActions.MOVE_SCREEN -> {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    showVersionTooLowToast(context, R.string.action_move_screen)
                                    sideGestureState.cancel()
                                    clearSubGestureMode(notifyService = false)
                                } else {
                                    moveScreenState.onDragStart(sideGestureState.finger)
                                    sideGestureState.cancel()
                                    clearSubGestureMode(notifyService = false)
                                }
                            }
                            else -> {
                                handleResolvedAction(action, sideGestureState.button, sideGestureState.finger)
                                if (action.value != GlobalActions.SUB_GESTURE) clearSubGestureMode()
                            }
                        }
                    } else {
                        clearSubGestureMode()
                    }
                }
                return@onDrag
            }
            if (vmHandle.isActive) {
                if (!vmHandle.onDrag(dragAmount)) return@onDrag
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
                if (actionSettings.volumeScrub.horizontalEnabled) {
                    volumeScrubAccumulatorX += dragAmount.x
                    while (volumeScrubAccumulatorX >= volumeStepThreshold) {
                        context.volumeUp()
                        volumeScrubAccumulatorX -= volumeStepThreshold
                    }
                    while (volumeScrubAccumulatorX <= -volumeStepThreshold) {
                        context.volumeDown()
                        volumeScrubAccumulatorX += volumeStepThreshold
                    }
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
                            volumeScrubAccumulatorX = 0f
                            sideGestureState.cancel()
                        } else if (action.value == GlobalActions.VIRTUAL_MOUSE) {
                            vmHandle.start(action, sideGestureState.finger, virtualMousePreviousPosition())
                            sideGestureState.cancel()
                        } else if (action.value == GlobalActions.MOVE_SCREEN) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
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
            if (vmHandle.isActive) {
                curOnSubGestureModeChanged(false)
                vmHandle.onDragEnd()
                return@onDragEnd
            }
            if (isVolumeScrubMode) {
                curOnSubGestureModeChanged(false)
                isVolumeScrubMode = false
                volumeScrubAccumulator = 0f
                volumeScrubAccumulatorX = 0f
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
                return@onDragEnd
            }

            if (!sideGestureState.isCanceled) {
                val touchPosition = sideGestureState.finger
                val sourceButton = sideGestureState.button
                val action = sideGestureState.onDragEnd()

                handleResolvedAction(action, sourceButton, touchPosition)
            }
        },
        onDragCancel = onDragCancel@{
            if (activeSubGesture != null) {
                clearSubGestureMode()
                return@onDragCancel
            }
            if (vmHandle.isActive) {
                curOnSubGestureModeChanged(false)
                vmHandle.onDragCancel()
                return@onDragCancel
            }
            if (isVolumeScrubMode) {
                curOnSubGestureModeChanged(false)
                isVolumeScrubMode = false
                volumeScrubAccumulator = 0f
                volumeScrubAccumulatorX = 0f
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            var screenshot by remember { mutableStateOf<Bitmap?>(null) }
            LaunchedEffect(moveScreenState.visible) {
                if (!moveScreenState.visible) {
                    screenshot = null
                    return@LaunchedEffect
                }
                screenshot = try {
                    onTakeScreenshot?.invoke()
                } catch (_: Exception) {
                    null
                }
            }
            if (moveScreenState.visible) {
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
}

private fun Action.withTouchPosition(position: Offset): Action {
    if (!position.x.isFinite() || !position.y.isFinite()) return this
    return withRuntimeTouchPosition(position.x.roundToInt(), position.y.roundToInt())
}

@Composable
internal fun rememberSideGestureState(
    buttons: List<GestureButton>,
    advancedSettings: AdvancedSettings = AdvancedSettings(),
    gestureSettings: GestureSettings = GestureSettings()
): SideGestureState {
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope, buttons, advancedSettings, gestureSettings) {
        SideGestureState(coroutineScope, buttons, advancedSettings, gestureSettings)
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

