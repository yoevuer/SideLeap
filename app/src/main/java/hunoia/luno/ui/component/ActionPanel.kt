package hunoia.luno.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState.Visible
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastForEachIndexed
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ktx.toPx

import hunoia.luno.action.GlobalActions
import hunoia.luno.settings.defaults.SettingsUiDefaults.DimAlpha
import hunoia.luno.action.TriggerType
import hunoia.luno.action.Action
import hunoia.luno.settings.model.ActionPanelStyle
import hunoia.luno.settings.model.ArcStyle
import hunoia.luno.settings.model.GridStyle
import hunoia.luno.settings.model.PieStyle
import hunoia.luno.gesture.Position
import hunoia.luno.ui.action.actionIcon
import hunoia.luno.ui.action.actionText
import hunoia.luno.action.appInfo
import hunoia.luno.action.shortcutInfo
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.system.vibration.vibrateForActionPanel
import hunoia.luno.ui.theme.AnimNormal
import hunoia.luno.ui.theme.AnimPanelResize
import hunoia.luno.ui.theme.MiniWindowWidth
import hunoia.luno.ui.theme.RootPadding
import hunoia.luno.ui.theme.ShapeSmall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/20
 */

@Composable
fun ActionPanel(
    actionPanelStyle: ActionPanelStyle,
    actionPanelState: ActionPanelState,
    modifier: Modifier = Modifier,
    longPressLaunchPopup: Boolean = false,
    gestureSettings: GestureSettings? = null
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = actionPanelState.visible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut(spring(stiffness = Spring.StiffnessMedium))
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = DimAlpha))
            )

            val selectedAction = actionPanelState.selectedAction
            val selectedLabel = actionText(selectedAction)
            val animationSpec = spring<Float>(stiffness = Spring.StiffnessHigh)
            val enter = fadeIn(animationSpec) + scaleIn(animationSpec, 0.9f)
            val exit = fadeOut(animationSpec) + scaleOut(animationSpec, 0.9f)


            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.Center)
                    .displayCutoutPadding()
                    .padding(RootPadding),
                visible = selectedAction.value == GlobalActions.EXTRA_LAUNCH_APP,
                enter = enter,
                exit = ExitTransition.None
            ) {
                    BoxWithConstraints {
                    Box(
                        modifier = Modifier
                            .let { thisModifier ->
                                val miniWindow = actionPanelState.triggerType.isMiniWindow(longPressLaunchPopup)
                                val boxMaxWidth = maxWidth
                                val boxMaxHeight = maxHeight
                                val spec = tween<Dp>(AnimPanelResize.toInt())
                                val width by animateDpAsState(
                                    targetValue = if (miniWindow) MiniWindowWidth else boxMaxWidth,
                                    animationSpec = spec
                                )
                                val height by animateDpAsState(
                                    targetValue = if (miniWindow) 267.dp else boxMaxHeight,
                                    animationSpec = spec
                                )
                                thisModifier.size(width = width, height = height)
                            }
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(ShapeSmall)
                            )
                    )
                }
            }

            when (val style = actionPanelState.actionPanelStyle ?: actionPanelStyle) {
                is GridStyle -> {
                    GridActionPanel(
                        modifier = Modifier.fillMaxSize(),
                        actionPanelStyle = style,
                        actionPanelState = actionPanelState,
                        gestureSettings = gestureSettings
                    )
                }

                is PieStyle -> {
                    PieActionPanel(
                        modifier = Modifier.fillMaxSize(),
                        actionPanelStyle = style,
                        actionPanelState = actionPanelState,
                        gestureSettings = gestureSettings
                    )
                }

                else -> {
                    ArcActionPanel(
                        modifier = Modifier.fillMaxSize(),
                        actionPanelStyle = style as? ArcStyle ?: ArcStyle(),
                        actionPanelState = actionPanelState,
                        gestureSettings = gestureSettings
                    )
                }
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .displayCutoutPadding()
                    .padding(RootPadding),
                visible = selectedLabel.isNotEmpty(),
                enter = enter,
                exit = exit
            ) {
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = Shadow(
                            color = Color.Black, offset = Offset(2.0f, 2.0f), blurRadius = 3f
                        )
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}





internal fun actionPanelOrigin(
    parentSize: Size,
    origin: Offset,
    position: Position,
    itemSizePx: Float
): Offset {
    if (parentSize.isEmpty()) return origin
    val safePadding = itemSizePx * 1.5f
    val x = when (position) {
        Position.Left -> origin.x.coerceAtMost(parentSize.width / 2f)
        Position.Right -> origin.x.coerceAtLeast(parentSize.width / 2f)
        Position.Bottom -> origin.x.coerceIn(safePadding, parentSize.width - safePadding)
    }
    val y = when (position) {
        Position.Left, Position.Right -> origin.y.coerceIn(safePadding, parentSize.height - safePadding)
        Position.Bottom -> (origin.y - itemSizePx * 2f).coerceIn(
            parentSize.height * 0.35f,
            parentSize.height - safePadding
        )
    }
    return Offset(x, y)
}



internal fun Offset.coerceInside(parentSize: Size, itemSizePx: Float): Offset {
    val padding = itemSizePx / 2f
    return Offset(
        x = x.coerceIn(padding, parentSize.width - padding),
        y = y.coerceIn(padding, parentSize.height - padding)
    )
}

internal fun Float.coerceSafely(minimumValue: Float, maximumValue: Float): Float {
    return if (minimumValue <= maximumValue) coerceIn(minimumValue, maximumValue) else minimumValue
}

internal fun arcLayerCapacity(radius: Float, itemSizePx: Float, minGapPx: Float, arcLength: Int): Int {
    val minDistance = itemSizePx + minGapPx
    val diameter = radius * 2f
    if (diameter <= minDistance) return 1
    val minAngle = Math.toDegrees(2.0 * kotlin.math.asin((minDistance / diameter).coerceAtMost(1f).toDouble())).toFloat()
    return kotlin.math.floor(arcLength.toFloat() / minAngle).toInt().coerceAtLeast(1) + 1
}

internal fun actionPanelHitContains(
    finger: Offset,
    panelOrigin: Offset,
    targetAnimOffset: Offset,
    itemSizePx: Float
): Boolean {
    val targetCenter = panelOrigin + targetAnimOffset
    val halfSize = itemSizePx / 2f
    return finger.x in (targetCenter.x - halfSize)..(targetCenter.x + halfSize) &&
            finger.y in (targetCenter.y - halfSize)..(targetCenter.y + halfSize)
}

@Composable
internal fun AnimatedVisibilityScope.ActionPanelSelectableItem(
    actionPanelState: ActionPanelState,
    index: Int,
    action: Action,
    targetAnimOffset: Offset,
    panelOrigin: Offset,
    itemSizePx: Float,
    gestureSettings: GestureSettings?,
    modifier: Modifier,
    shape: androidx.compose.ui.graphics.Shape,
    content: @Composable () -> Unit
) {
    val transition = transition
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isHovered) 1.15f else 1f, spring(stiffness = Spring.StiffnessHigh), label = "actionScale")
    LaunchedEffect(transition, actionPanelState, index, action, panelOrigin, targetAnimOffset) {
        snapshotFlow { actionPanelState.finger }
            .filter { it.isSpecified && !transition.isRunning && transition.currentState == Visible }
            .collect { finger ->
                if (actionPanelHitContains(finger, panelOrigin, targetAnimOffset, itemSizePx)) {
                    if (!actionPanelState.isSelected(action)) {
                        isHovered = true
                        actionPanelState.select(index, action)
                        gestureSettings?.let { vibrateForActionPanel(it) }
                    }
                } else if (actionPanelState.isSelected(action)) {
                    isHovered = false
                    actionPanelState.select(index, Action.NONE)
                }
            }
    }
    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = targetAnimOffset.x
                translationY = targetAnimOffset.y
                scaleX = scale
                scaleY = scale
            }
            .clipToBackground(color = actionPanelItemColor(action), shape = shape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
internal fun ActionPanelIcon(action: Action, iconSize: Dp, bitmapIconSize: Dp = iconSize) {
    val actionIcon = actionIcon(action = action)
    if (actionIcon is ImageVector) {
        Image(
            modifier = Modifier.size(iconSize),
            imageVector = actionIcon,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
        )
    } else {
        AsyncImage(
            modifier = Modifier
                .size(bitmapIconSize)
                .graphicsLayer {
                    val appInfo = action.appInfo
                    if (appInfo != null) {
                        scaleX = appInfo.iconScale
                        scaleY = appInfo.iconScale
                        return@graphicsLayer
                    }
                    val shortcutInfo = action.shortcutInfo
                    if (shortcutInfo != null) {
                        scaleX = shortcutInfo.iconScale
                        scaleY = shortcutInfo.iconScale
                    }
                },
            model = actionIcon,
            contentDescription = null,
            imageLoader = LocalContext.current.imageLoader,
        )
    }
}

@Composable
internal fun actionPanelItemColor(action: Action): Color {
    val actionIcon = actionIcon(action = action)
    return when (action.value) {
        GlobalActions.EXTRA_LAUNCH_APP -> action.appInfo?.iconBgColor.toActionPanelColor()
        GlobalActions.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.iconBgColor.toActionPanelColor()

        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
internal fun Int?.toActionPanelColor(): Color {
    return if (this == null || this == 0) MaterialTheme.colorScheme.primary else Color(this)
}

@Composable
fun rememberActionPanelState(): ActionPanelState {
    val coroutineScope = rememberCoroutineScope()
    return remember {
        ActionPanelState(coroutineScope)
    }
}

class ActionPanelState(private val coroutineScope: CoroutineScope) : LongSlideState() {

    var visible: Boolean by mutableStateOf(false)
        private set
    var actions: List<Action> by mutableStateOf(emptyList())
        private set
    var position: Position by mutableStateOf(Position.Left)
        private set
    var actionPanelStyle: ActionPanelStyle? by mutableStateOf(null)
        private set
    private val pendingActions: MutableMap<Int, Action> = mutableStateMapOf()

    private val selectedBaseAction: Action by derivedStateOf {
        pendingActions.values.find { it != Action.NONE } ?: Action.NONE
    }
    val selectedAction: Action by derivedStateOf {
        when (triggerType) {
            TriggerType.Press -> selectedBaseAction
            TriggerType.LongPress -> selectedBaseAction.longPressAction ?: selectedBaseAction
        }
    }
    var triggerType: TriggerType by mutableStateOf(TriggerType.Press)
        private set
    private var delayTriggerTypeChangedJob: Job? = null

    override fun onDragStart(offset: Offset) {
        super.onDragStart(offset)
        visible = true
    }

    fun ready(position: Position, actions: List<Action>, actionPanelStyle: ActionPanelStyle) {
        this.position = position
        this.actions = actions
        this.actionPanelStyle = actionPanelStyle
    }

    fun done(): Action {
        val action = selectedAction
        val triggerType = triggerType
        reset()
        return action.copy(extra = triggerType)
    }

    fun isSelected(action: Action): Boolean {
        return pendingActions.values.find { it == action } != null
    }

    fun select(index: Int, action: Action) {
        pendingActions[index] = action

        delayTriggerTypeChangedJob?.cancel()
        triggerType = TriggerType.Press
        delayTriggerTypeChangedJob = coroutineScope.launch {
            delay(500)
            triggerType = TriggerType.LongPress
        }
    }

    override fun reset() {
        visible = false
        actionPanelStyle = null
        pendingActions.clear()
        origin = Offset.Unspecified
        finger = Offset.Unspecified
        delayTriggerTypeChangedJob?.cancel()
        triggerType = TriggerType.Press
    }

    /**
     * 用于实现短按和长按
     */
}
