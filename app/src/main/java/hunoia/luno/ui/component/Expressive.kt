package hunoia.luno.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.onSingleClick
import hunoia.luno.settings.defaults.SettingsUiDefaults
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVerticalWithSection
import hunoia.luno.ui.theme.DividerHeight
import hunoia.luno.ui.theme.IconTextPadding
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.MainSecondaryTextPadding
import hunoia.luno.ui.theme.MarkColorSize
import hunoia.luno.ui.theme.MinItemHeight
import hunoia.luno.ui.theme.MinItemHeightNoSecondary
import hunoia.luno.ui.theme.Spacing12
import hunoia.luno.ui.theme.Spacing14
import hunoia.luno.ui.theme.Spacing16
import hunoia.luno.ui.theme.Spacing4
import hunoia.luno.ui.theme.Spacing48
import hunoia.luno.ui.theme.Spacing8
import hunoia.luno.ui.theme.SectionTitlePadding

@Composable
fun ExpressiveCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primaryContainer,
    onAccent: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconSize: Dp = Spacing48,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(Spacing16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing12),
            ) {
                Surface(
                    modifier = Modifier.size(iconSize),
                    shape = MaterialTheme.shapes.large,
                    color = accent,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = onAccent)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing4))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                trailing?.invoke()
            }
            Spacer(Modifier.height(Spacing16))
            content()
        }
    }
}

@Composable
fun ExpressiveRow(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondaryText: String = "",
    secondaryTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    icon: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .fillMaxWidth(),
        onClick = { if (enabled) onClick() },
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    val minHeight = if (secondaryText.isEmpty()) {
                        MinItemHeightNoSecondary
                    } else {
                        MinItemHeight
                    }
                    it.heightIn(min = minHeight)
                }
                .padding(
                    horizontal = ContentPaddingHorizontal,
                    vertical = ContentPaddingVerticalWithSection,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItemPadding),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(IconTextPadding),
            ) {
                icon?.invoke()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .width(IntrinsicSize.Max),
                    verticalArrangement = Arrangement.spacedBy(MainSecondaryTextPadding),
                ) {
                    Text(
                        modifier = Modifier.width(IntrinsicSize.Max),
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    if (secondaryText.isNotEmpty()) {
                        Text(
                            modifier = Modifier.width(IntrinsicSize.Max),
                            text = secondaryText,
                            color = secondaryTextColor,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (trailing != null) {
                trailing()
            } else if (enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = text,
                )
            }
        }
    }
}

@Composable
fun ExpressiveSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subtitle: String = "",
    secondaryTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    markColor: Color = Color.Unspecified,
    onMarkColorClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    mainSecondaryTextPadding: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .fillMaxWidth(),
        onClick = {
            if (onClick != null) {
                onClick()
            } else {
                onCheckedChange(!checked)
            }
        },
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    val minHeight = if (subtitle.isEmpty()) {
                        MinItemHeightNoSecondary
                    } else {
                        MinItemHeight
                    }
                    it.heightIn(min = minHeight)
                }
                .padding(
                    horizontal = ContentPaddingHorizontal,
                    vertical = ContentPaddingVerticalWithSection,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItemPadding),
        ) {
            val paddingVV = when (mainSecondaryTextPadding) {
                true -> MainSecondaryTextPadding
                else -> 0.dp
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Max),
                verticalArrangement = Arrangement.spacedBy(paddingVV),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(IconTextPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icon != null) {
                        icon()
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    if (markColor.isSpecified) {
                        Box(
                            modifier = Modifier
                                .size(MarkColorSize)
                                .background(color = markColor, shape = CircleShape)
                                .then(
                                    if (onMarkColorClick != null) {
                                        Modifier.clickable(
                                            indication = null,
                                            interactionSource = null,
                                            onClick = onMarkColorClick,
                                        )
                                    } else Modifier
                                ),
                        )
                    }
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        modifier = Modifier.width(IntrinsicSize.Max),
                        text = subtitle,
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (onClick != null) {
                VerticalDivider(
                    modifier = Modifier.height(DividerHeight),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Switch(
                    enabled = enabled,
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                )
            }
        }
    }
}

@Composable
fun ExpressiveSection(
    modifier: Modifier = Modifier,
    title: String = "",
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (title.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .padding(bottom = SectionTitlePadding)
                    .padding(horizontal = ContentPaddingHorizontal),
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(Spacing8),
                verticalArrangement = Arrangement.spacedBy(Spacing8),
            ) {
                content()
            }
        }
    }
}

private const val DISABLED_ALPHA = SettingsUiDefaults.DisabledAlpha
