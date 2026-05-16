package hunoia.sideleap.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.onSingleClick
import hunoia.sideleap.settings.api.SettingsUiDefaults
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVerticalWithSection
import hunoia.sideleap.ui.theme.DividerHeight
import hunoia.sideleap.ui.theme.IconTextPadding
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MainSecondaryTextPadding
import hunoia.sideleap.ui.theme.MarkColorSize
import hunoia.sideleap.ui.theme.MinItemHeight
import hunoia.sideleap.ui.theme.MinItemHeightNoSecondary

@Composable
fun TextActionButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondaryText: String = "",
    secondaryTextColor: Color = MaterialTheme.colorScheme.secondary,
    prefix: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = ContentPaddingHorizontal,
        vertical = ContentPaddingVerticalWithSection
    )
) {
    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .fillMaxWidth()
            .let {
                val minHeight = if (secondaryText.isEmpty()) {
                    MinItemHeightNoSecondary
                } else {
                    MinItemHeight
                }
                it.heightIn(min = minHeight)
            }
            .onSingleClick(enabled = enabled) {
                onClick()
            }
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IconTextPadding)
        ) {
            prefix?.invoke()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .width(IntrinsicSize.Max),
                verticalArrangement = Arrangement.spacedBy(MainSecondaryTextPadding)
            ) {
                Text(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                if (secondaryText.isNotEmpty()) {
                    Text(
                        modifier = Modifier.width(IntrinsicSize.Max),
                        text = secondaryText,
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (enabled) {
            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = text)
        }
    }
}

@Composable
fun LabeledSwitch(
    onCheckedChange: (Boolean) -> Unit,
    checked: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onTextClick: (() -> Unit)? = null,
    secondaryText: String = "",
    secondaryTextColor: Color = MaterialTheme.colorScheme.secondary,
    markColor: Color = Color.Unspecified,
    mainSecondaryTextPadding: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = ContentPaddingHorizontal,
        vertical = ContentPaddingVerticalWithSection
    )
) {
    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .fillMaxWidth()
            .let {
                val minHeight = if (secondaryText.isEmpty()) {
                    MinItemHeightNoSecondary
                } else {
                    MinItemHeight
                }
                it.heightIn(min = minHeight)
            }
            .onSingleClick(enabled = enabled) {
                if (onTextClick != null) {
                    onTextClick()
                } else {
                    onCheckedChange(!checked)
                }
            }
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        val mainSecondaryPadding = when (mainSecondaryTextPadding) {
            true -> MainSecondaryTextPadding
            else -> 0.dp
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(mainSecondaryPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(IconTextPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        modifier = Modifier.weight(1f),
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                if (markColor.isSpecified) {
                    Box(
                        modifier = Modifier
                            .size(MarkColorSize)
                            .background(color = markColor, shape = CircleShape)
                    )
                }
            }
            if (secondaryText.isNotEmpty()) {
                Text(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    text = secondaryText,
                    color = secondaryTextColor,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (onTextClick != null) {
            VerticalDivider(
                modifier = Modifier.height(DividerHeight),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentSize provides 0.dp
        ) {
            Switch(
                enabled = enabled,
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

private const val DISABLED_ALPHA = SettingsUiDefaults.DisabledAlpha