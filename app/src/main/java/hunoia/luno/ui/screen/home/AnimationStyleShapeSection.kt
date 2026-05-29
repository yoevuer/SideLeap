package hunoia.luno.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hunoia.luno.R
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxBezierLength
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxBezierWidth
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxBubbleDiameter
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxBubbleOffset
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxCapsuleCornerRadius
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxCapsuleLength
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxCapsuleThickness
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxLineCornerRadius
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxLineLength
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxLineOffset
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxLineWidth
import hunoia.luno.config.defaults.SettingsUiDefaults.MinBezierLength
import hunoia.luno.config.defaults.SettingsUiDefaults.MinBezierWidth
import hunoia.luno.config.defaults.SettingsUiDefaults.MinBubbleDiameter
import hunoia.luno.config.defaults.SettingsUiDefaults.MinBubbleOffset
import hunoia.luno.config.defaults.SettingsUiDefaults.MinCapsuleCornerRadius
import hunoia.luno.config.defaults.SettingsUiDefaults.MinCapsuleLength
import hunoia.luno.config.defaults.SettingsUiDefaults.MinCapsuleThickness
import hunoia.luno.config.defaults.SettingsUiDefaults.MinLineCornerRadius
import hunoia.luno.config.defaults.SettingsUiDefaults.MinLineLength
import hunoia.luno.config.defaults.SettingsUiDefaults.MinLineOffset
import hunoia.luno.config.defaults.SettingsUiDefaults.MinLineWidth
import hunoia.luno.config.model.AnimationStyles
import hunoia.luno.config.model.WaveStyle
import hunoia.luno.ui.component.ExpressiveSection
import hunoia.luno.ui.component.ExpressiveSwitchItem
import hunoia.luno.ui.component.MyTextSlider
import hunoia.luno.ui.theme.SectionPadding
import hunoia.luno.ui.theme.Spacing16
import hunoia.luno.ui.theme.Spacing24
import kotlin.math.roundToInt

@Composable
fun ShapeStyleSection(
    type: Int,
    vm: AnimationStyleVM,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing16, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StyleChip(
            modifier = Modifier.weight(1f),
            selected = type == AnimationStyles.TYPE_WAVE,
            icon = { ShapePreview(WaveStyle.SHAPE_WAVE, Modifier.size(Spacing24),
                if (type == AnimationStyles.TYPE_WAVE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) },
            label = stringResource(R.string.shape_wave),
            onClick = { vm.onTabSelected(AnimationStyles.TYPE_WAVE) }
        )
        StyleChip(
            modifier = Modifier.weight(1f),
            selected = type == AnimationStyles.TYPE_LINE,
            icon = { LinePreview(Modifier.size(Spacing24),
                if (type == AnimationStyles.TYPE_LINE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) },
            label = stringResource(R.string.shape_line),
            onClick = { vm.onTabSelected(AnimationStyles.TYPE_LINE) }
        )
        StyleChip(
            modifier = Modifier.weight(1f),
            selected = type == AnimationStyles.TYPE_CAPSULE,
            icon = { CapsulePreview(Modifier.size(Spacing24),
                if (type == AnimationStyles.TYPE_CAPSULE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) },
            label = stringResource(R.string.animation_style_capsule),
            onClick = { vm.onTabSelected(AnimationStyles.TYPE_CAPSULE) }
        )
        StyleChip(
            modifier = Modifier.weight(1f),
            selected = type == AnimationStyles.TYPE_BUBBLE,
            icon = { BubblePreview(Modifier.size(Spacing24),
                if (type == AnimationStyles.TYPE_BUBBLE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) },
            label = stringResource(R.string.animation_style_bubble),
            onClick = { vm.onTabSelected(AnimationStyles.TYPE_BUBBLE) }
        )
    }
}

@Composable
fun ShapeSizeSection(
    type: Int,
    vm: AnimationStyleVM,
    waveStyle: WaveStyle,
    localWaveWidth: Float,
    onWaveWidthChange: (Float) -> Unit,
    onWaveWidthFinished: () -> Unit,
    localCapsuleThickness: Float,
    onCapsuleThicknessChange: (Float) -> Unit,
    onCapsuleThicknessFinished: () -> Unit,
    localCapsuleMaxLength: Float,
    onCapsuleMaxLengthChange: (Float) -> Unit,
    onCapsuleMaxLengthFinished: () -> Unit,
    localCapsuleCornerRadius: Float,
    onCapsuleCornerRadiusChange: (Float) -> Unit,
    onCapsuleCornerRadiusFinished: () -> Unit,
    localLineWidth: Float,
    onLineWidthChange: (Float) -> Unit,
    onLineWidthFinished: () -> Unit,
    localLineMaxLength: Float,
    onLineMaxLengthChange: (Float) -> Unit,
    onLineMaxLengthFinished: () -> Unit,
    localLineMaxOffset: Float,
    onLineMaxOffsetChange: (Float) -> Unit,
    onLineMaxOffsetFinished: () -> Unit,
    localLineCornerRadius: Float,
    onLineCornerRadiusChange: (Float) -> Unit,
    onLineCornerRadiusFinished: () -> Unit,
    localBubbleDiameter: Float,
    onBubbleDiameterChange: (Float) -> Unit,
    onBubbleDiameterFinished: () -> Unit,
    localBubbleMaxOffset: Float,
    onBubbleMaxOffsetChange: (Float) -> Unit,
    onBubbleMaxOffsetFinished: () -> Unit,
) {
    ExpressiveSection(
        modifier = Modifier.padding(top = SectionPadding),
        title = stringResource(id = R.string.shape_size)
    ) {
        when (type) {
            AnimationStyles.TYPE_WAVE -> {
                MyTextSlider(
                    value = localWaveWidth,
                    onValueChange = onWaveWidthChange,
                    onValueChangeFinished = onWaveWidthFinished,
                    text = stringResource(id = R.string.width),
                    valueDisplay = "${localWaveWidth.roundToInt()}px",
                    valueRange = MinBezierWidth.toFloat()..MaxBezierWidth.toFloat()
                )
                MyTextSlider(
                    value = waveStyle.bezierLengthHalfRatio,
                    onValueChange = { ratio -> vm.onWaveStyleChange { w -> w.copy(bezierLengthHalfRatio = ratio) } },
                    onValueChangeFinished = { vm.saveWaveSettings() },
                    text = stringResource(id = R.string.length),
                    valueDisplay = String.format("%.1f", waveStyle.bezierLengthHalfRatio),
                    valueRange = MinBezierLength..MaxBezierLength
                )
                ExpressiveSwitchItem(
                    onCheckedChange = { checked -> vm.onWaveStyleChange { it.copy(safeBounds = checked) }; vm.saveWaveSettings() },
                    checked = waveStyle.safeBounds,
                    title = stringResource(id = R.string.reserved_bounds),
                    subtitle = stringResource(id = R.string.reserved_bounds_hint)
                )
                ExpressiveSwitchItem(
                    onCheckedChange = { checked -> vm.onWaveStyleChange { it.copy(transformEnabled = checked) }; vm.saveWaveSettings() },
                    checked = waveStyle.transformEnabled,
                    title = stringResource(id = R.string.bezier_transform),
                    subtitle = stringResource(id = R.string.bezier_transform_hint)
                )
            }
            AnimationStyles.TYPE_CAPSULE -> {
                MyTextSlider(
                    value = localCapsuleThickness,
                    onValueChange = onCapsuleThicknessChange,
                    onValueChangeFinished = onCapsuleThicknessFinished,
                    text = stringResource(id = R.string.thickness),
                    valueDisplay = "${localCapsuleThickness.roundToInt()}px",
                    valueRange = MinCapsuleThickness.toFloat()..MaxCapsuleThickness.toFloat()
                )
                MyTextSlider(
                    value = localCapsuleMaxLength,
                    onValueChange = onCapsuleMaxLengthChange,
                    onValueChangeFinished = onCapsuleMaxLengthFinished,
                    text = stringResource(id = R.string.max_length),
                    valueDisplay = "${localCapsuleMaxLength.roundToInt()}px",
                    valueRange = MinCapsuleLength.toFloat()..MaxCapsuleLength.toFloat()
                )
                MyTextSlider(
                    value = localCapsuleCornerRadius,
                    onValueChange = onCapsuleCornerRadiusChange,
                    onValueChangeFinished = onCapsuleCornerRadiusFinished,
                    text = stringResource(id = R.string.corner_radius),
                    valueDisplay = "${localCapsuleCornerRadius.roundToInt()}px",
                    valueRange = MinCapsuleCornerRadius.toFloat()..MaxCapsuleCornerRadius.toFloat()
                )
            }
            AnimationStyles.TYPE_LINE -> {
                MyTextSlider(
                    value = localLineWidth,
                    onValueChange = onLineWidthChange,
                    onValueChangeFinished = onLineWidthFinished,
                    text = stringResource(id = R.string.width),
                    valueDisplay = "${localLineWidth.roundToInt()}px",
                    valueRange = MinLineWidth.toFloat()..MaxLineWidth.toFloat()
                )
                MyTextSlider(
                    value = localLineMaxLength,
                    onValueChange = onLineMaxLengthChange,
                    onValueChangeFinished = onLineMaxLengthFinished,
                    text = stringResource(id = R.string.max_length),
                    valueDisplay = "${localLineMaxLength.roundToInt()}px",
                    valueRange = MinLineLength.toFloat()..MaxLineLength.toFloat()
                )
                MyTextSlider(
                    value = localLineMaxOffset,
                    onValueChange = onLineMaxOffsetChange,
                    onValueChangeFinished = onLineMaxOffsetFinished,
                    text = stringResource(id = R.string.max_offset),
                    valueDisplay = "${localLineMaxOffset.roundToInt()}px",
                    valueRange = MinLineOffset.toFloat()..MaxLineOffset.toFloat()
                )
                MyTextSlider(
                    value = localLineCornerRadius,
                    onValueChange = onLineCornerRadiusChange,
                    onValueChangeFinished = onLineCornerRadiusFinished,
                    text = stringResource(id = R.string.corner_radius),
                    valueDisplay = "${localLineCornerRadius.roundToInt()}px",
                    valueRange = MinLineCornerRadius.toFloat()..MaxLineCornerRadius.toFloat()
                )
            }
            else -> {
                MyTextSlider(
                    value = localBubbleDiameter,
                    onValueChange = onBubbleDiameterChange,
                    onValueChangeFinished = onBubbleDiameterFinished,
                    text = stringResource(id = R.string.diameter),
                    valueDisplay = "${localBubbleDiameter.roundToInt()}px",
                    valueRange = MinBubbleDiameter.toFloat()..MaxBubbleDiameter.toFloat()
                )
                MyTextSlider(
                    value = localBubbleMaxOffset,
                    onValueChange = onBubbleMaxOffsetChange,
                    onValueChangeFinished = onBubbleMaxOffsetFinished,
                    text = stringResource(id = R.string.max_offset),
                    valueDisplay = "${localBubbleMaxOffset.roundToInt()}px",
                    valueRange = MinBubbleOffset.toFloat()..MaxBubbleOffset.toFloat()
                )
            }
        }
    }
}
