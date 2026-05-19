package hunoia.sideleap.ui.component

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

import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.DimAlpha
import hunoia.sideleap.action.TriggerType
import hunoia.sideleap.action.Action
import hunoia.sideleap.settings.model.ActionPanelStyle
import hunoia.sideleap.settings.model.ArcStyle
import hunoia.sideleap.settings.model.GridStyle
import hunoia.sideleap.settings.model.PieStyle
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.system.vibration.Vibrations
import hunoia.sideleap.action.display.actionIcon
import hunoia.sideleap.action.display.actionText
import hunoia.sideleap.action.appInfo
import hunoia.sideleap.action.shortcutInfo
import hunoia.sideleap.system.vibration.tryVibrateForActionPanel
import hunoia.sideleap.ui.theme.AnimNormal
import hunoia.sideleap.ui.theme.AnimPanelResize
import hunoia.sideleap.ui.theme.RootPadding
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
                is GridStyle -> {
                    GridActionPanel(
                        modifier = Modifier.fillMaxSize(),
                        actionPanelStyle = style,
                        actionPanelState = actionPanelState,
                        vibrations = vibrations
                    )
                }

                is PieStyle -> {
                    PieActionPanel(
                        modifier = Modifier.fillMaxSize(),
                        actionPanelStyle = style,
                        actionPanelState = actionPanelState,
                        vibrations = vibrations
                    )
                }

                else -> {
                    ArcActionPanel(
                        modifier = Modifier.fillMaxSize(),
                        actionPanelStyle = style as? ArcStyle ?: ArcStyle(),
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
    val actionCount = actionPanelState.actions.size
    val itemSizePx = itemSize.toPx()
    var parentSize by remember { mutableStateOf(Size.Zero) }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .onGloballyPositioned {
                    parentSize = it.size.toSize()
                }
                .matchParentSize()
        )

        Box(
            modifier = Modifier
                .run {
                    val origin = remember(actionPanelState) { actionPanelState.origin }
                    graphicsLayer {
                        if (parentSize.isEmpty()) return@graphicsLayer
                        val itemSizeHalf = itemSize.toPx() / 2f
                        val panelOrigin = actionPanelOrigin(parentSize, origin, actionPanelState.position, itemSize.toPx())
                        val ox = panelOrigin.x
                        val oy = panelOrigin.y
                        translationX = ox - itemSizeHalf
                        translationY = oy - itemSizeHalf
                    }
                }
                .size(itemSize)
        ) {
            val transition = transition
            actionPanelState.actions.fastForEachIndexed { index, action ->
                key(index) {
                    val targetAnimOffset = remember(parentSize, actionPanelState.position, actionPanelState.actions.size, index) {
                        arcActionOffset(
                            parentSize = parentSize,
                            origin = actionPanelOrigin(parentSize, actionPanelState.origin, actionPanelState.position, itemSizePx),
                            position = actionPanelState.position,
                            actionCount = actionCount,
                            index = index,
                            itemSizePx = itemSizePx
                        )
                    }
                    val panelOrigin = actionPanelOrigin(parentSize, actionPanelState.origin, actionPanelState.position, itemSizePx)
                    var isHovered by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(if (isHovered) 1.15f else 1f, tween(AnimNormal.toInt()), label = "actionScale")

                    LaunchedEffect(transition, actionPanelState, index, action, panelOrigin, targetAnimOffset) {
                        snapshotFlow { actionPanelState.finger }
                            .filter {
                                it.isSpecified &&
                                        !transition.isRunning &&
                                        transition.currentState == Visible
                            }
                            .collect { finger ->
                                if (actionPanelHitContains(finger, panelOrigin, targetAnimOffset, itemSizePx)) {
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
                                    GlobalActions.EXTRA_LAUNCH_APP -> action.appInfo?.iconBgColor.toActionPanelColor()
                                    GlobalActions.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.iconBgColor.toActionPanelColor()

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
private fun AnimatedVisibilityScope.GridActionPanel(
    actionPanelStyle: GridStyle,
    actionPanelState: ActionPanelState,
    modifier: Modifier = Modifier,
    vibrations: Vibrations? = null
) {
    val itemSize = actionPanelStyle.itemSize.toDp()
    val itemSizePx = itemSize.toPx()
    val gapPx = itemSizePx * 0.35f
    val origin = remember(actionPanelState) { actionPanelState.origin }
    var parentSize by remember { mutableStateOf(Size.Zero) }
    val panelOrigin = actionPanelOrigin(parentSize, origin, actionPanelState.position, itemSizePx)
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { parentSize = it.size.toSize() }
                .matchParentSize()
        )
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = panelOrigin.x - itemSizePx / 2f
                    translationY = panelOrigin.y - itemSizePx / 2f
                }
                .size(itemSize)
        ) {
            actionPanelState.actions.fastForEachIndexed { index, action ->
                key(index) {
                    val offset = remember(parentSize, actionPanelState.position, actionPanelState.actions.size, index) {
                        gridActionOffset(
                            parentSize = parentSize,
                            origin = panelOrigin,
                            position = actionPanelState.position,
                            actionCount = actionPanelState.actions.size,
                            index = index,
                            itemSizePx = itemSizePx,
                            gapPx = gapPx
                        )
                    }
                    ActionPanelSelectableItem(
                        actionPanelState = actionPanelState,
                        index = index,
                        action = action,
                        targetAnimOffset = offset,
                        panelOrigin = panelOrigin,
                        itemSizePx = itemSizePx,
                        vibrations = vibrations,
                        modifier = Modifier.size(itemSize),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        ActionPanelIcon(
                            action = action,
                            iconSize = itemSize * 0.58f,
                            bitmapIconSize = itemSize * 0.76f
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedVisibilityScope.PieActionPanel(
    actionPanelStyle: PieStyle,
    actionPanelState: ActionPanelState,
    modifier: Modifier = Modifier,
    vibrations: Vibrations? = null
) {
    val itemSize = actionPanelStyle.itemSize.toDp()
    val actionCount = actionPanelState.actions.size
    val itemSizePx = itemSize.toPx()
    var parentSize by remember { mutableStateOf(Size.Zero) }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { parentSize = it.size.toSize() }
                .matchParentSize()
        )

        val origin = actionPanelState.origin
        val position = actionPanelState.position
        val primaryColor = MaterialTheme.colorScheme.primary

        Canvas(Modifier.matchParentSize()) {
            if (parentSize.isEmpty() || !origin.isSpecified) return@Canvas

            val panelOrigin = actionPanelOrigin(parentSize, origin, position, itemSizePx)
            val maxOuterRadius = pieMaxOuterRadius(parentSize, panelOrigin, position, actionCount, itemSizePx)
            if (maxOuterRadius <= 0f) return@Canvas

            val bgColor = primaryColor.copy(alpha = 0.15f)
            val gapRatio = 0.15f
            val segCount = 8
            val minGapPx = itemSizePx * 0.2f
            val maxItemsPerLayer = arcLayerCapacity(itemSizePx * 2f, itemSizePx, minGapPx)
            val layerCount = ceil(actionCount / maxItemsPerLayer.toFloat()).toInt().coerceAtLeast(1)
            val itemsPerLayer = ceil(actionCount / layerCount.toFloat()).toInt().coerceAtLeast(1)
            var remaining = actionCount
            var layer = 0
            while (remaining > 0) {
                val countInLayer = min(itemsPerLayer, remaining).coerceAtLeast(1)
                val layerRadius = itemSizePx * (2.0f + layer * 1.25f)
                val maxRad = when (position) {
                    Position.Left -> parentSize.width - panelOrigin.x - itemSizePx
                    Position.Right -> panelOrigin.x - itemSizePx
                    Position.Bottom -> panelOrigin.y - itemSizePx
                }.coerceAtLeast(itemSizePx * 1.5f)
                val resolvedRadius = min(layerRadius, maxRad)
                val innerR = (resolvedRadius - itemSizePx * 0.5f).coerceAtLeast(1f)
                val outerR = min(resolvedRadius + itemSizePx * 0.5f, maxOuterRadius)

                val sectorSpan = 170f / countInLayer
                val fillSpan = sectorSpan * (1f - gapRatio)

                for (i in 0 until countInLayer) {
                    val centerAngle = -85f + (i + 0.5f) * sectorSpan
                    val fillStart = centerAngle - fillSpan / 2f
                    val fillEnd = centerAngle + fillSpan / 2f
                    if (fillEnd <= fillStart) continue

                    val startRad = Math.toRadians(fillStart.toDouble())
                    val endRad = Math.toRadians(fillEnd.toDouble())
                    val path = Path()
                    with(path) {
                        var angleRad = startRad
                        val c0 = cos(angleRad).toFloat()
                        val s0 = sin(angleRad).toFloat()
                        val innerStart = pixelPos(panelOrigin, c0, s0, innerR, position)
                        moveTo(innerStart.x, innerStart.y)

                        for (t in 0..segCount) {
                            angleRad = startRad + (endRad - startRad) * t / segCount
                            val cc = cos(angleRad).toFloat()
                            val ss = sin(angleRad).toFloat()
                            val p = pixelPos(panelOrigin, cc, ss, outerR, position)
                            lineTo(p.x, p.y)
                        }
                        for (t in segCount downTo 0) {
                            angleRad = startRad + (endRad - startRad) * t / segCount
                            val cc = cos(angleRad).toFloat()
                            val ss = sin(angleRad).toFloat()
                            val p = pixelPos(panelOrigin, cc, ss, innerR, position)
                            lineTo(p.x, p.y)
                        }
                        close()
                    }
                    drawPath(path, bgColor)
                }

                remaining -= countInLayer
                layer++
            }
        }

        Box(
            modifier = Modifier
                .run {
                    graphicsLayer {
                        if (parentSize.isEmpty()) return@graphicsLayer
                        val itemSizeHalf = itemSize.toPx() / 2f
                        val panelOrigin = actionPanelOrigin(parentSize, origin, position, itemSize.toPx())
                        val ox = panelOrigin.x
                        val oy = panelOrigin.y
                        translationX = ox - itemSizeHalf
                        translationY = oy - itemSizeHalf
                    }
                }
                .size(itemSize)
        ) {
            val transition = transition
            actionPanelState.actions.fastForEachIndexed { index, action ->
                key(index) {
                    val panelOrigin = actionPanelOrigin(parentSize, origin, position, itemSizePx)
                    val targetAnimOffset = remember(parentSize, position, origin, actionCount, index, itemSizePx) {
                        if (parentSize.isEmpty()) return@remember Offset.Zero
                        val minGapPx = itemSizePx * 0.2f
                        val maxItemsPerLayer = arcLayerCapacity(itemSizePx * 2f, itemSizePx, minGapPx)
                        val layerCount = ceil(actionCount / maxItemsPerLayer.toFloat()).toInt().coerceAtLeast(1)
                        val itemsPerLayer = ceil(actionCount / layerCount.toFloat()).toInt().coerceAtLeast(1)
                        val layer = index / itemsPerLayer
                        val indexInLayer = index % itemsPerLayer
                        val firstIndexInLayer = layer * itemsPerLayer
                        val countInLayer = min(itemsPerLayer, actionCount - firstIndexInLayer).coerceAtLeast(1)
                        val radius = itemSizePx * (2.0f + layer * 1.25f)
                        val maxRad = when (position) {
                            Position.Left -> parentSize.width - panelOrigin.x - itemSizePx
                            Position.Right -> panelOrigin.x - itemSizePx
                            Position.Bottom -> panelOrigin.y - itemSizePx
                        }.coerceAtLeast(itemSizePx * 1.5f)
                        val resolvedRadius = min(radius, maxRad)
                        val sectorCenter = -85f + 170f * (indexInLayer + 0.5f) / countInLayer
                        val rad = Math.toRadians(sectorCenter.toDouble())
                        val x = cos(rad).toFloat() * resolvedRadius
                        val y = sin(rad).toFloat() * resolvedRadius
                        val target = when (position) {
                            Position.Left -> Offset(panelOrigin.x + x, panelOrigin.y + y)
                            Position.Right -> Offset(panelOrigin.x - x, panelOrigin.y + y)
                            Position.Bottom -> Offset(panelOrigin.x + y, panelOrigin.y - x)
                        }.coerceInside(parentSize, itemSizePx)
                        target - panelOrigin
                    }
                    var isHovered by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        if (isHovered) 1.15f else 1f,
                        tween(AnimNormal.toInt()),
                        label = "actionScale"
                    )

                    LaunchedEffect(transition, actionPanelState, index, action, panelOrigin, targetAnimOffset) {
                        snapshotFlow { actionPanelState.finger }
                            .filter {
                                it.isSpecified &&
                                        !transition.isRunning &&
                                        transition.currentState == Visible
                            }
                            .collect { finger ->
                                if (pieHitContains(finger, panelOrigin, position, actionCount, index, itemSizePx, parentSize)) {
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
                            .matchParentSize(),
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

private fun pieHitContains(
    finger: Offset,
    panelOrigin: Offset,
    position: Position,
    actionCount: Int,
    index: Int,
    itemSizePx: Float,
    parentSize: Size
): Boolean {
    if (parentSize.isEmpty() || actionCount <= 0) return false
    if (!panelOrigin.isSpecified || panelOrigin.x.isNaN() || panelOrigin.y.isNaN()) return false
    val minGapPx = itemSizePx * 0.2f
    val maxItemsPerLayer = arcLayerCapacity(itemSizePx * 2f, itemSizePx, minGapPx)
    val layerCount = ceil(actionCount / maxItemsPerLayer.toFloat()).toInt().coerceAtLeast(1)
    val itemsPerLayer = ceil(actionCount / layerCount.toFloat()).toInt().coerceAtLeast(1)
    val layer = index / itemsPerLayer
    val indexInLayer = index % itemsPerLayer
    val firstIndexInLayer = layer * itemsPerLayer
    val countInLayer = min(itemsPerLayer, actionCount - firstIndexInLayer).coerceAtLeast(1)

    val layerRadius = itemSizePx * (2.0f + layer * 1.25f)
    val maxRad = when (position) {
        Position.Left -> parentSize.width - panelOrigin.x - itemSizePx
        Position.Right -> panelOrigin.x - itemSizePx
        Position.Bottom -> panelOrigin.y - itemSizePx
    }.coerceAtLeast(itemSizePx * 1.5f)
    val resolvedRadius = min(layerRadius, maxRad)
    val innerRadius = (resolvedRadius - itemSizePx * 0.5f).coerceAtLeast(1f)
    val outerRadius = resolvedRadius + itemSizePx * 0.5f

    val dx = finger.x - panelOrigin.x
    val dy = finger.y - panelOrigin.y
    val dist = sqrt(dx * dx + dy * dy)
    if (dist < innerRadius || dist > outerRadius) return false

    val canonAngle = when (position) {
        Position.Left -> Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        Position.Right -> Math.toDegrees(atan2(dy.toDouble(), (-dx).toDouble())).toFloat()
        Position.Bottom -> Math.toDegrees(atan2(dx.toDouble(), (-dy).toDouble())).toFloat()
    }
    val sectorWidth = 170f / countInLayer
    val sectorStart = -85f + indexInLayer * sectorWidth
    val sectorEnd = sectorStart + sectorWidth
    return canonAngle in sectorStart..sectorEnd
}

private fun pieMaxOuterRadius(
    parentSize: Size,
    panelOrigin: Offset,
    position: Position,
    actionCount: Int,
    itemSizePx: Float
): Float {
    if (actionCount <= 0) return 0f
    val minGapPx = itemSizePx * 0.2f
    val maxItemsPerLayer = arcLayerCapacity(itemSizePx * 2f, itemSizePx, minGapPx)
    val layerCount = ceil(actionCount / maxItemsPerLayer.toFloat()).toInt().coerceAtLeast(1)
    val lastLayer = layerCount - 1
    val radius = itemSizePx * (2.0f + lastLayer * 1.25f)
    val maxRad = when (position) {
        Position.Left -> parentSize.width - panelOrigin.x - itemSizePx
        Position.Right -> panelOrigin.x - itemSizePx
        Position.Bottom -> panelOrigin.y - itemSizePx
    }.coerceAtLeast(itemSizePx * 1.5f)
    val resolvedRadius = min(radius, maxRad)
    return resolvedRadius + itemSizePx * 0.5f
}

private fun pixelPos(panelOrigin: Offset, c: Float, s: Float, radius: Float, position: Position): Offset {
    return when (position) {
        Position.Left -> Offset(panelOrigin.x + c * radius, panelOrigin.y + s * radius)
        Position.Right -> Offset(panelOrigin.x - c * radius, panelOrigin.y + s * radius)
        Position.Bottom -> Offset(panelOrigin.x + s * radius, panelOrigin.y - c * radius)
    }
}

private fun actionPanelOrigin(
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

private fun arcActionOffset(
    parentSize: Size,
    origin: Offset,
    position: Position,
    actionCount: Int,
    index: Int,
    itemSizePx: Float
): Offset {
    if (parentSize.isEmpty() || actionCount <= 0) return Offset.Zero
    val minGapPx = itemSizePx * 0.2f
    val maxItemsPerLayer = arcLayerCapacity(
        radius = itemSizePx * 2.0f,
        itemSizePx = itemSizePx,
        minGapPx = minGapPx
    )
    val layerCount = ceil(actionCount / maxItemsPerLayer.toFloat()).toInt().coerceAtLeast(1)
    val itemsPerLayer = ceil(actionCount / layerCount.toFloat()).toInt().coerceAtLeast(1)
    val layer = index / itemsPerLayer
    val indexInLayer = index % itemsPerLayer
    val firstIndexInLayer = layer * itemsPerLayer
    val countInLayer = min(itemsPerLayer, actionCount - firstIndexInLayer).coerceAtLeast(1)
    val radius = itemSizePx * (2.0f + layer * 1.25f)
    val maxRadius = when (position) {
        Position.Left -> parentSize.width - origin.x - itemSizePx
        Position.Right -> origin.x - itemSizePx
        Position.Bottom -> origin.y - itemSizePx
    }.coerceAtLeast(itemSizePx * 1.5f)
    val resolvedRadius = min(radius, maxRadius)
    val sweepDegree = if (countInLayer == 1) 0f else min(170f, 35f * (countInLayer - 1))
    val angleDegree = if (countInLayer == 1) 0f else {
        -sweepDegree / 2f + sweepDegree * indexInLayer / (countInLayer - 1)
    }
    val radians = Math.toRadians(angleDegree.toDouble())
    val x = cos(radians).toFloat() * resolvedRadius
    val y = sin(radians).toFloat() * resolvedRadius
    val targetCenter = when (position) {
        Position.Left -> Offset(origin.x + x, origin.y + y)
        Position.Right -> Offset(origin.x - x, origin.y + y)
        Position.Bottom -> Offset(origin.x + y, origin.y - x)
    }.coerceInside(parentSize, itemSizePx)
    return targetCenter - origin
}

private fun arcLayerCapacity(radius: Float, itemSizePx: Float, minGapPx: Float): Int {
    val minDistance = itemSizePx + minGapPx
    val diameter = radius * 2f
    if (diameter <= minDistance) return 1
    val minAngle = Math.toDegrees(2.0 * asin((minDistance / diameter).coerceAtMost(1f).toDouble())).toFloat()
    return floor(170f / minAngle).toInt().coerceAtLeast(1) + 1
}

private fun gridActionOffset(
    parentSize: Size,
    origin: Offset,
    position: Position,
    actionCount: Int,
    index: Int,
    itemSizePx: Float,
    gapPx: Float
): Offset {
    if (parentSize.isEmpty() || actionCount <= 0) return Offset.Zero
    val padding = itemSizePx * 0.75f
    val originGap = itemSizePx * 1.35f
    val rect = when (position) {
        Position.Left -> PanelRect(origin.x + originGap, padding, parentSize.width - padding, parentSize.height - padding)
        Position.Right -> PanelRect(padding, padding, origin.x - originGap, parentSize.height - padding)
        Position.Bottom -> PanelRect(padding, padding, parentSize.width - padding, origin.y - originGap)
    }.validOrFallback(parentSize, padding)
    val cell = itemSizePx + gapPx
    val compactRect = rect.limitHeight(
        maxHeight = itemSizePx * 5f + gapPx * 4f,
        anchorY = origin.y,
        alignBottom = position == Position.Bottom
    )
    val availableWidth = (compactRect.right - compactRect.left).coerceAtLeast(itemSizePx)
    val availableHeight = (compactRect.bottom - compactRect.top).coerceAtLeast(itemSizePx)
    val maxColumns = floor((availableWidth + gapPx) / cell).toInt().coerceAtLeast(1).coerceAtMost(actionCount)
    val maxRows = floor((availableHeight + gapPx) / cell).toInt().coerceAtLeast(1)
    val minColumns = ceil(actionCount / maxRows.toFloat()).toInt().coerceAtLeast(1)
    val columns = minColumns.coerceIn(1, maxColumns)
    val rows = ceil(actionCount / columns.toFloat()).toInt().coerceAtLeast(1)
    val gridWidth = columns * itemSizePx + (columns - 1) * gapPx
    val gridHeight = rows * itemSizePx + (rows - 1) * gapPx
    val left = when (position) {
        Position.Left -> compactRect.left
        Position.Right -> compactRect.right - gridWidth
        Position.Bottom -> (origin.x - gridWidth / 2f).coerceSafely(compactRect.left, compactRect.right - gridWidth)
    }
    val top = when (position) {
        Position.Left, Position.Right -> (origin.y - gridHeight / 2f).coerceSafely(compactRect.top, compactRect.bottom - gridHeight)
        Position.Bottom -> compactRect.bottom - gridHeight
    }
    val col = index % columns
    val row = index / columns
    val targetCenter = Offset(
        x = left + col * cell + itemSizePx / 2f,
        y = top + row * cell + itemSizePx / 2f
    ).coerceInside(parentSize, itemSizePx)
    return targetCenter - origin
}

private data class PanelRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun validOrFallback(parentSize: Size, padding: Float): PanelRect {
        return if (right > left && bottom > top) this
        else PanelRect(padding, padding, parentSize.width - padding, parentSize.height - padding)
    }

    fun limitHeight(maxHeight: Float, anchorY: Float, alignBottom: Boolean): PanelRect {
        val currentHeight = bottom - top
        if (currentHeight <= maxHeight) return this
        if (alignBottom) {
            return copy(top = bottom - maxHeight)
        }
        val limitedTop = (anchorY - maxHeight / 2f).coerceIn(top, bottom - maxHeight)
        return copy(top = limitedTop, bottom = limitedTop + maxHeight)
    }
}

private fun Offset.coerceInside(parentSize: Size, itemSizePx: Float): Offset {
    val padding = itemSizePx / 2f
    return Offset(
        x = x.coerceIn(padding, parentSize.width - padding),
        y = y.coerceIn(padding, parentSize.height - padding)
    )
}

private fun Float.coerceSafely(minimumValue: Float, maximumValue: Float): Float {
    return if (minimumValue <= maximumValue) coerceIn(minimumValue, maximumValue) else minimumValue
}

private fun actionPanelHitContains(
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
private fun AnimatedVisibilityScope.ActionPanelSelectableItem(
    actionPanelState: ActionPanelState,
    index: Int,
    action: Action,
    targetAnimOffset: Offset,
    panelOrigin: Offset,
    itemSizePx: Float,
    vibrations: Vibrations?,
    modifier: Modifier,
    shape: androidx.compose.ui.graphics.Shape,
    content: @Composable () -> Unit
) {
    val transition = transition
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isHovered) 1.08f else 1f, tween(AnimNormal.toInt()), label = "actionScale")
    LaunchedEffect(transition, actionPanelState, index, action, panelOrigin, targetAnimOffset) {
        snapshotFlow { actionPanelState.finger }
            .filter { it.isSpecified && !transition.isRunning && transition.currentState == Visible }
            .collect { finger ->
                if (actionPanelHitContains(finger, panelOrigin, targetAnimOffset, itemSizePx)) {
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
private fun ActionPanelIcon(action: Action, iconSize: Dp, bitmapIconSize: Dp = iconSize) {
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
private fun actionPanelItemColor(action: Action): Color {
    val actionIcon = actionIcon(action = action)
    return when (action.value) {
        GlobalActions.EXTRA_LAUNCH_APP -> action.appInfo?.iconBgColor.toActionPanelColor()
        GlobalActions.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.iconBgColor.toActionPanelColor()

        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun Int?.toActionPanelColor(): Color {
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
