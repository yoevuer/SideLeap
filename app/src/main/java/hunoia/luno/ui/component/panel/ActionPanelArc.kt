package hunoia.luno.ui.component.panel

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastForEachIndexed
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ktx.toPx
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.ArcStyle
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.gesture.GestureFacade
import kotlinx.coroutines.flow.filter
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun AnimatedVisibilityScope.ArcActionPanel(
    actionPanelStyle: ArcStyle,
    actionPanelState: ActionPanelState,
    modifier: Modifier = Modifier,
    gestureSettings: GestureSettings? = null
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
                .fillMaxSize()
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
                    val targetAnimOffset = remember(parentSize, actionPanelState.position, actionPanelState.actions.size, index, actionPanelStyle.arcLength) {
                        arcActionOffset(
                            parentSize = parentSize,
                            origin = actionPanelOrigin(parentSize, actionPanelState.origin, actionPanelState.position, itemSizePx),
                            position = actionPanelState.position,
                            actionCount = actionCount,
                            index = index,
                            itemSizePx = itemSizePx,
                            arcLength = actionPanelStyle.arcLength
                        )
                    }
                    val panelOrigin = actionPanelOrigin(parentSize, actionPanelState.origin, actionPanelState.position, itemSizePx)
                    ActionPanelSelectableItem(
                        actionPanelState = actionPanelState,
                        index = index,
                        action = action,
                        targetAnimOffset = targetAnimOffset,
                        panelOrigin = panelOrigin,
                        itemSizePx = itemSizePx,
                        gestureSettings = gestureSettings,
                        modifier = Modifier
                            .run animateEnterExit@{
                                val stiffness = Spring.StiffnessMedium
                                animateEnterExit(
                                    enter = scaleIn(spring(stiffness = stiffness)) +
                                            slideIn(animationSpec = spring(stiffness = stiffness)) {
                                                IntOffset(-targetAnimOffset.x.toInt(), -targetAnimOffset.y.toInt())
                                            },
                                    exit = scaleOut(spring(stiffness = stiffness)) +
                                            slideOut(animationSpec = spring(stiffness = stiffness)) {
                                                IntOffset(-targetAnimOffset.x.toInt(), -targetAnimOffset.y.toInt())
                                            }
                                )
                            }
                            .fillMaxSize(),
                        shape = CircleShape
                    ) {
                        ActionPanelIcon(action = action, iconSize = itemSize * 0.65f, bitmapIconSize = itemSize * 0.82f)
                    }
                }
            }
        }
    }
}

private fun arcActionOffset(
    parentSize: Size,
    origin: Offset,
    position: Position,
    actionCount: Int,
    index: Int,
    itemSizePx: Float,
    arcLength: Int
): Offset {
    if (parentSize.isEmpty() || actionCount <= 0) return Offset.Zero
    val minGapPx = itemSizePx * 0.2f
    val maxItemsPerLayer = arcLayerCapacity(
        radius = itemSizePx * 2.0f,
        itemSizePx = itemSizePx,
        minGapPx = minGapPx,
        arcLength = arcLength
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
    val arcDegrees = arcLength.toFloat()
    val sweepDegree = if (countInLayer == 1) 0f else min(arcDegrees, 35f * (countInLayer - 1))
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
