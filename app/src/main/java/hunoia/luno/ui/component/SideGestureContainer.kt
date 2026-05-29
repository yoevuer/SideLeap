package hunoia.luno.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import hunoia.luno.R
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.Action
import hunoia.luno.pointer.PointerAction
import hunoia.luno.pointer.rememberPointerHandle
import hunoia.luno.action.withRuntimeTouchPosition
import hunoia.luno.config.model.ActionPanelStyle
import hunoia.luno.config.model.ActionPanelStyles
import hunoia.luno.config.model.ActionSettings
import hunoia.luno.config.model.AdvancedSettings
import hunoia.luno.config.model.AnimationStyle
import hunoia.luno.config.model.ArcStyle
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.config.model.WaveStyle
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.TriggerDirection
import hunoia.luno.gesture.styleBy
import hunoia.luno.gesture.tryVibrate
import hunoia.luno.ui.component.DragGestureHandler
import hunoia.luno.bridge.volumeDown
import hunoia.luno.bridge.volumeUp
import hunoia.luno.action.payload.SubGestureActionData
import hunoia.luno.core.JsonHelper
import hunoia.luno.config.model.SubGesture
import hunoia.luno.config.model.SubGestureSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan
import kotlin.math.hypot
import kotlin.math.roundToInt



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
    onPointerStart: () -> Boolean = { false },
    onPointerEnd: () -> Unit = {},
    onPointerSettingsUpdate: (GestureSettings.Pointer) -> Unit = {},
    pointerPreviousPosition: () -> Offset = { Offset.Unspecified },
    onPointerActionAtPosition: (Int, Int, Boolean, PointerAction) -> Unit = { _, _, _, _ -> },
    subGestureSettings: SubGestureSettings = SubGestureSettings(),
    onSubGestureModeChanged: (Boolean) -> Unit = {},

    wallpaperChangeTrigger: Long = 0L,
) {
    val context = LocalContext.current
    val curOnAction by rememberUpdatedState(newValue = onAction)
    val curOnPointerStart by rememberUpdatedState(newValue = onPointerStart)
    val curOnPointerEnd by rememberUpdatedState(newValue = onPointerEnd)
    val curOnPointerSettingsUpdate by rememberUpdatedState(newValue = onPointerSettingsUpdate)
    val curOnPointerActionAtPosition by rememberUpdatedState(newValue = onPointerActionAtPosition)
    val curOnSubGestureModeChanged by rememberUpdatedState(newValue = onSubGestureModeChanged)
    val coroutineScope = rememberCoroutineScope()
    val sideGestureState = rememberSideGestureState(buttons, advancedSettings, gestureSettings)
    val actionPanelState = rememberActionPanelState()
    val pointerHandle = rememberPointerHandle(
        gestureSettings = gestureSettings,
        onPointerStart = { curOnPointerStart() },
        onPointerEnd = { curOnPointerEnd() },
        onPointerSettingsUpdate = { settings -> curOnPointerSettingsUpdate(settings) },
        pointerPreviousPosition = { pointerPreviousPosition() },
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
                val sg = activeSubGesture!!
                if (hypot(subGestureAccum.x, subGestureAccum.y) >= sg.triggerDistance) {
                    val direction = sg.angle.directionOf(subGestureAccum)
                    val actionId = sg.actionFor(direction)
                    subGestureAccum = Offset.Zero
                    sg.tryVibrate()
                    if (actionId != null && actionId != GlobalActions.NONE) {
                        when (actionId) {
                            GlobalActions.VOLUME_SCRUB -> {
                                isVolumeScrubMode = true
                                volumeScrubAccumulator = 0f
                                volumeScrubAccumulatorX = 0f
                                sideGestureState.cancel()
                                clearSubGestureMode(notifyService = false)
                            }
                            GlobalActions.POINTER -> {
                                pointerHandle.start(Action(actionId), sideGestureState.finger, pointerPreviousPosition())
                                sideGestureState.cancel()
                                clearSubGestureMode(notifyService = false)
                            }
                            else -> {
                                handleResolvedAction(Action(actionId), sideGestureState.button, sideGestureState.finger)
                                if (actionId != GlobalActions.SUB_GESTURE) clearSubGestureMode()
                            }
                        }
                    } else {
                        clearSubGestureMode()
                    }
                }
                return@onDrag
            }
            if (pointerHandle.isActive) {
                if (!pointerHandle.onDrag(dragAmount)) return@onDrag
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
                        } else if (action.value == GlobalActions.POINTER) {
                            pointerHandle.start(action, sideGestureState.finger, pointerPreviousPosition())
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
            if (pointerHandle.isActive) {
                curOnSubGestureModeChanged(false)
                pointerHandle.onDragEnd()
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
            if (pointerHandle.isActive) {
                curOnSubGestureModeChanged(false)
                pointerHandle.onDragCancel()
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
            sideGestureState.onDragCancel()
        }
    )
    Box(modifier = modifier) {
        ActionPanel(
            actionPanelStyle = actionPanelStyle,
            actionPanelState = actionPanelState,
            modifier = Modifier.matchParentSize(),
            gestureSettings = gestureSettings
        )

        if (animationStyle != null) {
            key(wallpaperChangeTrigger) {
                GestureAnimation(
                    modifier = Modifier.matchParentSize(),
                    animationStyle = animationStyle,
                    SideGestureState = sideGestureState
                )
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

