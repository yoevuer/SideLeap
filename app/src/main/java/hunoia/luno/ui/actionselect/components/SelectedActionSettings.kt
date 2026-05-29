package hunoia.luno.ui.actionselect

import hunoia.luno.ui.theme.*

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import hunoia.luno.R
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.SubGesture
import kotlin.math.roundToInt

@Composable
fun SelectedActionSettings(
    selectedItems: List<Any>,
    longPressTargetIndex: Int?,
    subGestures: List<SubGesture>,
    itemLabel: (Any) -> String,
    onSetLongPress: (Int) -> Unit,
    onClearLongPress: (Int) -> Unit,
    onCancelLongPress: () -> Unit,
    onMoveSelected: (Int, Int) -> Unit,
    onRemoveItem: (Any) -> Unit,
    onClearAll: () -> Unit,
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    var itemHeight by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPaddingHorizontal * 2, vertical = Spacing4),
        verticalArrangement = Arrangement.spacedBy(Spacing6)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.selected_count_no_limit, selectedItems.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            if (longPressTargetIndex != null) {
                TextButton(onClick = onCancelLongPress) {
                    Text(stringResource(R.string.cancel))
                }
            }
            TextButton(onClick = onClearAll) {
                Text(stringResource(R.string.clear_all))
            }
        }
        selectedItems.forEachIndexed { index, item ->
            val action = item as? Action
            val longPressAction = action?.longPressAction
            val shortPressText = itemLabel(item)
            val longPressText = if (longPressAction != null) {
                LocalContext.current.actionTextWithSubGesture(longPressAction, subGestures, emptyIfNone = false)
            } else {
                stringResource(R.string.long_press_action_fallback)
            }
            val isDragging = draggedIndex == index
            Surface(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        if (itemHeight == 0f) itemHeight = coordinates.size.height.toFloat()
                    }
                    .graphicsLayer {
                        if (isDragging) {
                            translationY = dragOffset
                        }
                    }
                    .pointerInput(selectedItems.size) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedIndex = index
                                dragOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                if (draggedIndex == index) {
                                    change.consume()
                                    dragOffset += dragAmount.y
                                }
                            },
                            onDragEnd = {
                                draggedIndex?.let { from ->
                                    val spacingPx = with(density) { Spacing6.toPx() }
                                    val step = (itemHeight + spacingPx).coerceAtLeast(1f)
                                    val delta = (dragOffset / step).roundToInt()
                                    val to = (from + delta).coerceIn(0, selectedItems.lastIndex)
                                    if (to != from) onMoveSelected(from, to)
                                }
                                draggedIndex = null
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggedIndex = null
                                dragOffset = 0f
                            }
                        )
                    },
                shape = MaterialTheme.shapes.medium,
                color = if (longPressTargetIndex == index) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing10, vertical = Spacing6),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing4)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        modifier = Modifier.size(Spacing20),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${index + 1}. ${shortPressText} / ${longPressText}",
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(velocity = 50.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (longPressAction != null) {
                        TextButton(
                            onClick = { onClearLongPress(index) },
                            contentPadding = PaddingValues(horizontal = Spacing6, vertical = 0.dp)
                        ) {
                            Text(stringResource(R.string.clear_long_press_action))
                        }
                    } else {
                        TextButton(
                            onClick = { onSetLongPress(index) },
                            contentPadding = PaddingValues(horizontal = Spacing6, vertical = 0.dp)
                        ) {
                            Text(stringResource(R.string.set_long_press_action))
                        }
                    }
                    IconButton(
                        onClick = { onRemoveItem(item) },
                        modifier = Modifier.size(Spacing32)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            if (longPressTargetIndex == index) {
                Text(
                    text = stringResource(R.string.choose_long_press_action_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 34.dp)
                )
            }
        }
    }
}
