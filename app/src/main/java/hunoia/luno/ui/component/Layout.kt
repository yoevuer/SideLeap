package hunoia.luno.ui.component
import hunoia.luno.ui.theme.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.onClick
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVerticalWithSection
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.MinItemHeightNoSecondary
import hunoia.luno.ui.theme.RootPadding
import hunoia.luno.ui.theme.ScrollBottomPadding
import hunoia.luno.ui.theme.SectionTitlePadding

@Composable
fun MyColumn(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .padding(RootPadding)
            .padding(bottom = ScrollBottomPadding),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String = "",
    content: @Composable ColumnScope.() -> Unit
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
                maxLines = 1
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = Spacing2
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun MyExpandableColumn(
    onExpandedChange: (Boolean) -> Unit,
    title: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = backgroundColor
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MinItemHeightNoSecondary)
                    .onClick {
                        onExpandedChange(!expanded)
                    }
                    .padding(
                        horizontal = ContentPaddingHorizontal,
                        vertical = ContentPaddingVerticalWithSection
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ItemPadding)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )

                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 0f else -90f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "ArrowDropDownRotation"
                )
                Icon(
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotation
                    },
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = title
                )
            }

            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    content()
                }
            }
        }
    }
}