package hunoia.luno.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.clipToBackground
import hunoia.luno.R
import hunoia.luno.config.model.SnapBackDefaults
import hunoia.luno.config.model.SnapBackType
import hunoia.luno.ui.component.MyTextSlider
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.SectionTitlePadding
import hunoia.luno.ui.theme.Spacing12
import hunoia.luno.ui.theme.Spacing8


@Composable
fun SnapBackSection(
    currentType: SnapBackType,
    springStiffness: Float,
    springDamping: Float,
    easeOutDurationMs: Int,
    elasticCoefficient: Float,
    flingDecay: Float,
    onTypeChange: (SnapBackType) -> Unit,
    onSpringStiffnessChange: (Float) -> Unit,
    onSpringDampingChange: (Float) -> Unit,
    onEaseOutDurationChange: (Int) -> Unit,
    onElasticCoefficientChange: (Float) -> Unit,
    onFlingDecayChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .padding(bottom = SectionTitlePadding)
                .padding(horizontal = ContentPaddingHorizontal),
            text = stringResource(id = R.string.snap_back_style),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(Spacing8),
                verticalArrangement = Arrangement.spacedBy(Spacing8),
            ) {
                StyleChipRow(
                    selected = currentType,
                    onSelected = onTypeChange
                )
                Spacer(Modifier.height(8.dp))
                when (currentType) {
                    SnapBackType.SPRING -> {
                        MyTextSlider(
                            value = springStiffness,
                            onValueChange = onSpringStiffnessChange,
                            text = stringResource(id = R.string.snap_back_speed),
                            valueDisplay = String.format("%.0f%%", springStiffness * 100),
                            valueRange = 0f..1f
                        )
                        MyTextSlider(
                            value = springDamping,
                            onValueChange = onSpringDampingChange,
                            text = stringResource(id = R.string.snap_back_bounce),
                            valueDisplay = String.format("%.0f%%", springDamping * 100),
                            valueRange = 0f..1f
                        )
                    }
                    SnapBackType.EASE_OUT -> {
                        MyTextSlider(
                            value = easeOutDurationMs.toFloat(),
                            onValueChange = { onEaseOutDurationChange(it.toInt()) },
                            text = stringResource(id = R.string.snap_back_duration),
                            valueDisplay = "${easeOutDurationMs}ms",
                            valueRange = 100f..1000f
                        )
                    }
                    SnapBackType.SNAP -> {
                        Text(
                            text = stringResource(id = R.string.snap_back_snap_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    SnapBackType.ELASTIC -> {
                        MyTextSlider(
                            value = elasticCoefficient,
                            onValueChange = onElasticCoefficientChange,
                            text = stringResource(id = R.string.snap_back_elastic),
                            valueDisplay = String.format("%.0f%%", elasticCoefficient * 100),
                            valueRange = 0f..1f
                        )
                    }
                    SnapBackType.FLING -> {
                        MyTextSlider(
                            value = flingDecay,
                            onValueChange = onFlingDecayChange,
                            text = stringResource(id = R.string.snap_back_fling_decay),
                            valueDisplay = String.format("%.0f%%", flingDecay * 100),
                            valueRange = 0f..1f
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StyleChipRow(
    selected: SnapBackType,
    onSelected: (SnapBackType) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SnapBackChip(
            selected = selected == SnapBackType.SPRING,
            label = stringResource(id = R.string.snap_back_spring),
            onClick = { onSelected(SnapBackType.SPRING) }
        )
        SnapBackChip(
            selected = selected == SnapBackType.EASE_OUT,
            label = stringResource(id = R.string.snap_back_ease_out),
            onClick = { onSelected(SnapBackType.EASE_OUT) }
        )
        SnapBackChip(
            selected = selected == SnapBackType.SNAP,
            label = stringResource(id = R.string.snap_back_snap),
            onClick = { onSelected(SnapBackType.SNAP) }
        )
        SnapBackChip(
            selected = selected == SnapBackType.ELASTIC,
            label = stringResource(id = R.string.snap_back_elastic_chip),
            onClick = { onSelected(SnapBackType.ELASTIC) }
        )
        SnapBackChip(
            selected = selected == SnapBackType.FLING,
            label = stringResource(id = R.string.snap_back_fling),
            onClick = { onSelected(SnapBackType.FLING) }
        )
    }
}

@Composable
private fun SnapBackChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clipToBackground(
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
