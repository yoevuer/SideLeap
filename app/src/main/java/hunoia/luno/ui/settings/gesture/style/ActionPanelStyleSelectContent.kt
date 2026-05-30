package hunoia.luno.ui.settings.gesture.style
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
    val type = ActionPanelStyles.TYPE_ARC
    Column {
        MyColumn(verticalArrangement = Arrangement.spacedBy(Spacing12)) {
            ActionPanelStyleCard(
                type = type,
                nameRes = R.string.action_panel_style_arc,
                descRes = R.string.action_panel_style_arc_hint,
                selected = true,
                hasSettings = true,
                onClick = { onStyleSelected(ActionPanelStyles.arc()) },
                onSettingsClick = { onConfigRequest(ActionPanelStyles.arc()) }
            )
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
                ArcStylePreview(modifier = Modifier.fillMaxSize())
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
