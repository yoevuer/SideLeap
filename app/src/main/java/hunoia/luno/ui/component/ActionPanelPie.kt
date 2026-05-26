package hunoia.luno.ui.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState.Visible
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastForEachIndexed
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ktx.toPx
import hunoia.luno.action.Action
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.appInfo
import hunoia.luno.action.display.actionIcon
import hunoia.luno.action.shortcutInfo
import hunoia.luno.gesture.Position
import hunoia.luno.settings.model.PieStyle
import hunoia.luno.system.vibration.Vibrations
import hunoia.luno.system.vibration.tryVibrateForActionPanel
import kotlinx.coroutines.flow.filter
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
internal fun AnimatedVisibilityScope.PieActionPanel(
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
                .fillMaxSize()
        )

        val origin = actionPanelState.origin
        val position = actionPanelState.position
        val primaryColor = MaterialTheme.colorScheme.primary

        Canvas(Modifier.matchParentSize()) {
            if (parentSize.isEmpty() || !origin.isSpecified) return@Canvas

            val panelOrigin = actionPanelOrigin(parentSize, origin, position, itemSizePx)
            val maxOuterRadius = pieMaxOuterRadius(parentSize, panelOrigin, position, actionCount, itemSizePx, actionPanelStyle.arcLength)
            if (maxOuterRadius <= 0f) return@Canvas

            val bgColor = primaryColor.copy(alpha = 0.35f)
            val gapRatio = 0.15f
            val segCount = 8
            val minGapPx = itemSizePx * 0.2f
            val maxItemsPerLayer = arcLayerCapacity(itemSizePx * 2f, itemSizePx, minGapPx, actionPanelStyle.arcLength)
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

                val sectorSpan = actionPanelStyle.arcLength.toFloat() / countInLayer
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
                    val targetAnimOffset = remember(parentSize, position, origin, actionCount, index, itemSizePx, actionPanelStyle.arcLength) {
                        if (parentSize.isEmpty()) return@remember Offset.Zero
                        val minGapPx = itemSizePx * 0.2f
                        val maxItemsPerLayer = arcLayerCapacity(itemSizePx * 2f, itemSizePx, minGapPx, actionPanelStyle.arcLength)
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
                        val arcDegrees = actionPanelStyle.arcLength.toFloat()
                        val sectorCenter = -85f + arcDegrees * (indexInLayer + 0.5f) / countInLayer
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
                        spring(stiffness = Spring.StiffnessHigh),
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
                                if (pieHitContains(finger, panelOrigin, position, actionCount, index, itemSizePx, parentSize, actionPanelStyle.arcLength)) {
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
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (actionIcon is ImageVector) {
                            Image(
                                modifier = Modifier.size(itemSize * 0.65f),
                                imageVector = actionIcon,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                            )
                        } else {
                            AsyncImage(
                                modifier = Modifier
                                    .size(itemSize * 0.82f)
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
    parentSize: Size,
    arcLength: Int
): Boolean {
    if (parentSize.isEmpty() || actionCount <= 0) return false
    if (!panelOrigin.isSpecified || panelOrigin.x.isNaN() || panelOrigin.y.isNaN()) return false
    val minGapPx = itemSizePx * 0.2f
    val maxItemsPerLayer = arcLayerCapacity(itemSizePx * 2f, itemSizePx, minGapPx, arcLength)
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
    val arcDegrees = arcLength.toFloat()
    val sectorWidth = arcDegrees / countInLayer
    val sectorStart = -85f + indexInLayer * sectorWidth
    val sectorEnd = sectorStart + sectorWidth
    return canonAngle in sectorStart..sectorEnd
}

private fun pieMaxOuterRadius(
    parentSize: Size,
    panelOrigin: Offset,
    position: Position,
    actionCount: Int,
    itemSizePx: Float,
    arcLength: Int
): Float {
    if (actionCount <= 0) return 0f
    val minGapPx = itemSizePx * 0.2f
    val maxItemsPerLayer = arcLayerCapacity(itemSizePx * 2f, itemSizePx, minGapPx, arcLength)
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
