package hunoia.luno.ui.screen.settings.gesture
import hunoia.luno.ui.theme.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.aaron.compose.ktx.clipToBorder
import com.aaron.compose.ktx.onSingleClick
import hunoia.luno.R
import hunoia.luno.config.model.ActionPanelStyles
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVerticalWithSection
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.MinItemHeightNoSecondary
import hunoia.luno.ui.component.MyColumn
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ActionPanelStyleSelectContent(
    currentStyle: ActionPanelStyles,
    onStyleSelected: (ActionPanelStyles) -> Unit,
    onConfigRequest: (ActionPanelStyles) -> Unit
) {
    val items = listOf(
        Triple(ActionPanelStyles.TYPE_ARC, R.string.action_panel_style_arc, R.string.action_panel_style_arc_hint),
        Triple(ActionPanelStyles.TYPE_GRID, R.string.action_panel_style_grid, R.string.action_panel_style_grid_hint),
        Triple(ActionPanelStyles.TYPE_PIE, R.string.action_panel_style_pie, R.string.action_panel_style_pie_hint)
    )

    Column {
        MyColumn(verticalArrangement = Arrangement.spacedBy(Spacing12)) {
            items.fastForEachIndexed { _, (type, nameRes, descRes) ->
                val selected = type == currentStyle.type
                ActionPanelStyleCard(
                    type = type,
                    nameRes = nameRes,
                    descRes = descRes,
                    selected = selected,
                    hasSettings = true,
                    onClick = {
                        val style = when (type) {
                            ActionPanelStyles.TYPE_GRID -> ActionPanelStyles.grid()
                            ActionPanelStyles.TYPE_PIE -> ActionPanelStyles.pie()
                            else -> ActionPanelStyles.arc()
                        }
                        onStyleSelected(style)
                    },
                    onSettingsClick = {
                        val style = when (type) {
                            ActionPanelStyles.TYPE_GRID -> ActionPanelStyles.grid()
                            ActionPanelStyles.TYPE_PIE -> ActionPanelStyles.pie()
                            else -> ActionPanelStyles.arc()
                        }
                        onConfigRequest(style)
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionPanelStyleCard(
    type: Int,
    nameRes: Int,
    descRes: Int,
    selected: Boolean,
    hasSettings: Boolean,
    onClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.large
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBorder(width = 1.5.dp, color = borderColor, shape = shape)
            .onSingleClick { onClick() },
        shape = shape,
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinItemHeightNoSecondary + 28.dp)
                .padding(
                    horizontal = ContentPaddingHorizontal,
                    vertical = ContentPaddingVerticalWithSection
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItemPadding)
        ) {
            Box(modifier = Modifier.size(80.dp)) {
                when (type) {
                    ActionPanelStyles.TYPE_GRID -> GridStylePreview(modifier = Modifier.fillMaxSize())
                    ActionPanelStyles.TYPE_PIE -> PieStylePreview(modifier = Modifier.fillMaxSize())
                    else -> ArcStylePreview(modifier = Modifier.fillMaxSize())
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    modifier = Modifier.padding(top = Spacing2),
                    text = stringResource(id = descRes),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selected && hasSettings) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clipToBorder(
                            width = Spacing1,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .onSingleClick { onSettingsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(Spacing20),
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
internal fun PreviewStage(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing4)
                .clipToBorder(
                    width = Spacing1,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.small
                )
                .clipToBounds(),
            content = content
        )
    }
}

@Composable
internal fun GridStylePreview(modifier: Modifier = Modifier) {
    PreviewStage(modifier = modifier) {
        val colorScheme = MaterialTheme.colorScheme
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 58.dp, height = 44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colorScheme.primary.copy(alpha = 0.16f))
                .padding(horizontal = 8.dp, vertical = Spacing6)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing4)
            ) {
                repeat(2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing4)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ArcStylePreview(modifier: Modifier = Modifier) {
    PreviewStage(modifier = modifier) {
        val colorScheme = MaterialTheme.colorScheme
        Canvas(modifier = Modifier.fillMaxSize()) {
            val anchor = Offset(x = -size.width * 0.08f, y = size.height * 0.5f)
            val itemRadius = Spacing4.toPx()
            val innerArcRadius = size.minDimension * 0.37f
            val outerArcRadius = size.minDimension * 0.56f

            fun pointOnArc(radius: Float, angleDegree: Float): Offset {
                val rad = Math.toRadians(angleDegree.toDouble())
                return Offset(
                    x = anchor.x + cos(rad).toFloat() * radius,
                    y = anchor.y + sin(rad).toFloat() * radius
                )
            }

            listOf(-30f, 0f, 30f).forEach { angle ->
                drawCircle(colorScheme.primary, itemRadius, pointOnArc(innerArcRadius, angle))
            }
            listOf(-30f, -10f, 10f, 30f).forEach { angle ->
                drawCircle(colorScheme.primary, itemRadius, pointOnArc(outerArcRadius, angle))
            }
        }
    }
}

@Composable
internal fun PieStylePreview(modifier: Modifier = Modifier) {
    PreviewStage(modifier = modifier) {
        val colorScheme = MaterialTheme.colorScheme
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension * 0.45f
            val itemRadius = Spacing4.toPx()

            listOf(0f, 60f, 120f, 180f, 240f, 300f).forEach { angleDeg ->
                val rad = Math.toRadians(angleDeg.toDouble())
                val px = cx + cos(rad).toFloat() * radius * 0.6f
                val py = cy + sin(rad).toFloat() * radius * 0.6f
                drawCircle(colorScheme.primary, itemRadius, Offset(px, py))
            }
            drawCircle(
                color = colorScheme.primary,
                radius = itemRadius * 2.5f,
                center = Offset(cx, cy)
            )
        }
    }
}
