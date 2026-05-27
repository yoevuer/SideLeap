package hunoia.luno.ui.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastForEachIndexed
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ktx.toPx
import hunoia.luno.action.Action
import hunoia.luno.gesture.Position
import hunoia.luno.settings.model.GridStyle
import hunoia.luno.settings.model.GestureSettings
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

@Composable
internal fun AnimatedVisibilityScope.GridActionPanel(
    actionPanelStyle: GridStyle,
    actionPanelState: ActionPanelState,
    modifier: Modifier = Modifier,
    gestureSettings: GestureSettings? = null
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
                            gestureSettings = gestureSettings,
                        modifier = Modifier.size(itemSize),
                        shape = RoundedCornerShape(actionPanelStyle.cornerRadius.toDp())
                    ) {
                        ActionPanelIcon(
                            action = action,
                            iconSize = itemSize * 0.65f,
                            bitmapIconSize = itemSize * 0.82f
                        )
                    }
                }
            }
        }
    }
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
