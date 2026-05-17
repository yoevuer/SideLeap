package hunoia.sideleap.ui.widget

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
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

import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.settings.api.SettingsUiDefaults.DimAlpha
import hunoia.sideleap.action.TriggerType
import hunoia.sideleap.action.Action
import hunoia.sideleap.settings.model.ActionPanelStyle
import hunoia.sideleap.settings.model.ArcStyle
import hunoia.sideleap.settings.model.GridStyle
import hunoia.sideleap.settings.model.ListStyle
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.system.vibration.Vibrations
import hunoia.sideleap.action.display.actionIcon
import hunoia.sideleap.action.display.actionText
import hunoia.sideleap.action.appInfo
import hunoia.sideleap.action.shortcutInfo
import hunoia.sideleap.system.api.tryVibrateForActionPanel
import hunoia.sideleap.ui.theme.AnimNormal
import hunoia.sideleap.ui.theme.AnimPanelResize
import hunoia.sideleap.ui.theme.RootPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.ceil
import kotlin.math.pow
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
    vibrations: Vibrations? = null
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
                                    targetValue = if (miniWindow) 200.dp else boxMaxWidth,
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
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }

            when (val style = actionPanelState.actionPanelStyle ?: actionPanelStyle) {
                is ArcStyle -> {
                    ArcActionPanel(
                        modifier = Modifier.fillMaxSize(),
                        actionPanelStyle = style,
                        actionPanelState = actionPanelState,
                        vibrations = vibrations
                    )
                }
                is ListStyle -> {
                    LinearActionPanel(
                        modifier = Modifier.fillMaxSize(),
                        actionPanelStyle = style,
                        actionPanelState = actionPanelState,
                        vibrations = vibrations
                    )
                }
                is GridStyle -> {
                    GridActionPanel(
                        modifier = Modifier.fillMaxSize(),
                        actionPanelStyle = style,
                        actionPanelState = actionPanelState,
                        vibrations = vibrations
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
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun AnimatedVisibilityScope.ArcActionPanel(
    actionPanelStyle: ArcStyle,
    actionPanelState: ActionPanelState,
    modifier: Modifier = Modifier,
    vibrations: Vibrations? = null
) {
    val itemSize = actionPanelStyle.itemSize.toDp()
    // 斜边，从origin原点到item中心的距离，值越大item散得越开
    val actionCount = actionPanelState.actions.size
    val hypot = itemSize.toPx() * if (actionCount > 5) 2.6f else 2f
    var parentSize by remember { mutableStateOf(Size.Zero) }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .onGloballyPositioned {
                    parentSize = it.size.toSize()
                }
                .matchParentSize()
        )

        val selectedLabel: String = actionText(actionPanelState.selectedAction)
        val animationSpec = spring<Float>(stiffness = Spring.StiffnessHigh)
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .displayCutoutPadding()
                .padding(RootPadding),
            visible = selectedLabel.isNotEmpty(),
            enter = fadeIn(animationSpec) + scaleIn(animationSpec, 0.9f),
            exit = fadeOut(animationSpec) + scaleOut(animationSpec, 0.9f)
        ) {
            Text(
                text = selectedLabel,
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(
                        color = Color.Black, offset = Offset(2.0f, 2.0f), blurRadius = 3f
                    )
                ),
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .run {
                    val origin = remember(actionPanelState) { actionPanelState.origin }
                    graphicsLayer {
                        if (parentSize.isEmpty()) return@graphicsLayer
                        val itemSizeHalf = itemSize.toPx() / 2f
                        // 限制展开位置，防止显示不全
                        val iconOffset = itemSize.toPx() * 3f
                        val ox = origin.x.let {
                            when (actionPanelState.position) {
                                Position.Left -> it.coerceAtMost(parentSize.width / 2f)
                                Position.Right -> it.coerceAtLeast(parentSize.width / 2f)
                                Position.Bottom -> it.coerceIn(
                                    iconOffset,
                                    parentSize.width - iconOffset
                                )
                            }
                        }
                        val oy = origin.y.let {
                            when (actionPanelState.position) {
                                Position.Left, Position.Right -> it.coerceIn(
                                    minimumValue = iconOffset,
                                    maximumValue = parentSize.height - iconOffset
                                )

                                Position.Bottom -> it.coerceAtLeast(parentSize.height / 2f)
                            }
                        }
                        translationX = ox - itemSizeHalf
                        translationY = oy - itemSizeHalf
                    }
                }
                .size(itemSize)
        ) {
            val transition = transition
            actionPanelState.actions.fastForEachIndexed { index, action ->
                key(index) {
                    val targetAnimOffset = remember {
                        // 平均每个块之间的角度
                        val avgAngleDegree = if (actionPanelState.actions.size > 5) {
                            180.0 / (actionPanelState.actions.size - 1).coerceAtLeast(1)
                        } else 35.0
                        val totalAngleDegree = avgAngleDegree * (actionPanelState.actions.size - 1)
                        val angleDegree = -90.0 - totalAngleDegree / 2.0 + avgAngleDegree * index
                        val radians = Math.toRadians(angleDegree)
                        val neighbor = hypot * cos(radians)
                        val opposite = sqrt(hypot.pow(2) - neighbor.pow(2))
                        // 需要移动的x距离
                        val transX = when (actionPanelState.position) {
                            Position.Left -> opposite
                            Position.Right -> -opposite
                            Position.Bottom -> neighbor
                        }
                        // 需要移动的y距离
                        val transY = when (actionPanelState.position) {
                            Position.Left, Position.Right -> neighbor
                            Position.Bottom -> -opposite
                        }
                        Offset(x = transX.toFloat(), y = transY.toFloat())
                    }
                    var isHovered by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(if (isHovered) 1.15f else 1f, tween(AnimNormal.toInt()), label = "actionScale")

                    var originBounds by remember { mutableStateOf(Rect.Zero) }
                    LaunchedEffect(transition, actionPanelState, index, action) {
                        snapshotFlow { actionPanelState.finger }
                            .filter {
                                it.isSpecified &&
                                        !transition.isRunning &&
                                        transition.currentState == Visible
                            }
                            .collect { finger ->
                                val transFinger = finger - targetAnimOffset
                                if (originBounds.contains(transFinger)) {
                                    if (!actionPanelState.isSelected(action)) {
                                        isHovered = true
                                        actionPanelState.select(index, action)
                                        vibrations?.tryVibrateForActionPanel()
                                    }
                                } else {
                                    if (actionPanelState.isSelected(action)) {
                                        isHovered = false
                                        actionPanelState.select(index, Action.NONE)
                                    }
                                }
                            }
                    }

                    val actionIcon = actionIcon(action = action)
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned {
                                originBounds = it.boundsInRoot()
                            }
                            .graphicsLayer {
                                translationX = targetAnimOffset.x
                                translationY = targetAnimOffset.y
                                scaleX = scale
                                scaleY = scale
                            }
                            .run animateEnterExit@{
                                val stiffness = Spring.StiffnessMedium
                                animateEnterExit(
                                    enter = scaleIn(spring(stiffness = stiffness)) +
                                            slideIn(animationSpec = spring(stiffness = stiffness)) {
                                                IntOffset(
                                                    x = -targetAnimOffset.x.toInt(),
                                                    y = -targetAnimOffset.y.toInt()
                                                )
                                            },
                                    exit = scaleOut(spring(stiffness = stiffness)) +
                                            slideOut(animationSpec = spring(stiffness = stiffness)) {
                                                IntOffset(
                                                    x = -targetAnimOffset.x.toInt(),
                                                    y = -targetAnimOffset.y.toInt()
                                                )
                                            }
                                )
                            }
                            .matchParentSize()
                            .clipToBackground(
                                color = when (action.value) {
                                    GlobalActions.EXTRA_LAUNCH_APP -> when (actionIcon is ImageVector) {
                                        true -> MaterialTheme.colorScheme.primary
                                        else -> Color(action.appInfo!!.iconBgColor)
                                    }

                                    GlobalActions.EXTRA_LAUNCH_SHORTCUT -> when (actionIcon is ImageVector) {
                                        true -> MaterialTheme.colorScheme.primary
                                        else -> Color(action.shortcutInfo!!.iconBgColor)
                                    }

                                    else -> MaterialTheme.colorScheme.primary
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (actionIcon is ImageVector) {
                            Image(
                                imageVector = actionIcon,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                            )
                        } else {
                            AsyncImage(
                                modifier = Modifier
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
                }
            }
        }
    }
}

@Composable
private fun AnimatedVisibilityScope.LinearActionPanel(
    actionPanelStyle: ListStyle,
    actionPanelState: ActionPanelState,
    modifier: Modifier = Modifier,
    vibrations: Vibrations? = null
) {
    val itemSize = actionPanelStyle.itemSize.toDp()
    val itemWidth = itemSize * 3.5f
    val itemGapPx = itemSize.toPx() * 0.3f
    val itemHeightPx = itemSize.toPx()
    val itemWidthPx = itemWidth.toPx()
    val origin = remember(actionPanelState) { actionPanelState.origin }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = origin.x - itemSize.toPx() / 2f
                    translationY = origin.y - itemSize.toPx() / 2f
                }
                .size(itemSize)
        ) {
            actionPanelState.actions.fastForEachIndexed { index, action ->
                key(index) {
                    val offset = remember(actionPanelState.position, actionPanelState.actions.size, index) {
                        val centerOffset = index - (actionPanelState.actions.size - 1) / 2f
                        when (actionPanelState.position) {
                            Position.Left -> Offset(itemHeightPx * 1.3f, centerOffset * (itemHeightPx + itemGapPx))
                            Position.Right -> Offset(-itemWidthPx - itemHeightPx * 0.3f, centerOffset * (itemHeightPx + itemGapPx))
                            Position.Bottom -> {
                                if (actionPanelState.actions.size <= 4) {
                                    Offset(centerOffset * (itemWidthPx + itemGapPx), -itemHeightPx * 1.4f)
                                } else {
                                    Offset(-itemWidthPx / 2f, -(index + 1) * (itemHeightPx + itemGapPx))
                                }
                            }
                        }
                    }
                    ActionPanelSelectableItem(
                        actionPanelState = actionPanelState,
                        index = index,
                        action = action,
                        targetAnimOffset = offset,
                        vibrations = vibrations,
                        modifier = Modifier.size(width = itemWidth, height = itemSize),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionPanelIcon(action = action, iconSize = itemSize * 0.58f)
                            Text(
                                text = actionText(action),
                                modifier = Modifier.width(itemWidth - itemSize),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedVisibilityScope.GridActionPanel(
    actionPanelStyle: GridStyle,
    actionPanelState: ActionPanelState,
    modifier: Modifier = Modifier,
    vibrations: Vibrations? = null
) {
    val itemSize = actionPanelStyle.itemSize.toDp()
    val itemSizePx = itemSize.toPx()
    val gapPx = itemSizePx * 0.35f
    val columns = when (actionPanelState.actions.size) {
        in 1..4 -> 1
        in 5..6 -> 2
        else -> 3
    }
    val rows = ceil(actionPanelState.actions.size / columns.toFloat()).toInt().coerceAtLeast(1)
    val origin = remember(actionPanelState) { actionPanelState.origin }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = origin.x - itemSizePx / 2f
                    translationY = origin.y - itemSizePx / 2f
                }
                .size(itemSize)
        ) {
            actionPanelState.actions.fastForEachIndexed { index, action ->
                key(index) {
                    val offset = remember(actionPanelState.position, actionPanelState.actions.size, index) {
                        val col = index % columns
                        val row = index / columns
                        val cell = itemSizePx + gapPx
                        val gridWidth = (columns - 1) * cell
                        val gridHeight = (rows - 1) * cell
                        when (actionPanelState.position) {
                            Position.Left -> Offset(itemSizePx * 1.4f + col * cell, row * cell - gridHeight / 2f)
                            Position.Right -> Offset(-itemSizePx * 1.4f - gridWidth + col * cell, row * cell - gridHeight / 2f)
                            Position.Bottom -> Offset(col * cell - gridWidth / 2f, -itemSizePx * 1.4f - gridHeight + row * cell)
                        }
                    }
                    ActionPanelSelectableItem(
                        actionPanelState = actionPanelState,
                        index = index,
                        action = action,
                        targetAnimOffset = offset,
                        vibrations = vibrations,
                        modifier = Modifier.size(itemSize),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        ActionPanelIcon(action = action, iconSize = itemSize * 0.58f)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedVisibilityScope.ActionPanelSelectableItem(
    actionPanelState: ActionPanelState,
    index: Int,
    action: Action,
    targetAnimOffset: Offset,
    vibrations: Vibrations?,
    modifier: Modifier,
    shape: androidx.compose.ui.graphics.Shape,
    content: @Composable () -> Unit
) {
    val transition = transition
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isHovered) 1.08f else 1f, tween(AnimNormal.toInt()), label = "actionScale")
    var originBounds by remember { mutableStateOf(Rect.Zero) }
    LaunchedEffect(transition, actionPanelState, index, action) {
        snapshotFlow { actionPanelState.finger }
            .filter { it.isSpecified && !transition.isRunning && transition.currentState == Visible }
            .collect { finger ->
                val transFinger = finger - targetAnimOffset
                if (originBounds.contains(transFinger)) {
                    if (!actionPanelState.isSelected(action)) {
                        isHovered = true
                        actionPanelState.select(index, action)
                        vibrations?.tryVibrateForActionPanel()
                    }
                } else if (actionPanelState.isSelected(action)) {
                    isHovered = false
                    actionPanelState.select(index, Action.NONE)
                }
            }
    }
    Box(
        modifier = modifier
            .onGloballyPositioned { originBounds = it.boundsInRoot() }
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
private fun ActionPanelIcon(action: Action, iconSize: Dp) {
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
                .size(iconSize)
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
private fun actionPanelItemColor(action: Action): Color {
    val actionIcon = actionIcon(action = action)
    return when (action.value) {
        GlobalActions.EXTRA_LAUNCH_APP -> when (actionIcon is ImageVector) {
            true -> MaterialTheme.colorScheme.primary
            else -> Color(action.appInfo!!.iconBgColor)
        }

        GlobalActions.EXTRA_LAUNCH_SHORTCUT -> when (actionIcon is ImageVector) {
            true -> MaterialTheme.colorScheme.primary
            else -> Color(action.shortcutInfo!!.iconBgColor)
        }

        else -> MaterialTheme.colorScheme.primary
    }
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
