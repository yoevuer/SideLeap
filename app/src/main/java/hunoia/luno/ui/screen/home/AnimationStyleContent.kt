package hunoia.luno.ui.screen.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onSingleClick
import hunoia.luno.R
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxBezierLength
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxBezierStrokeWidth
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxBezierWidth
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxBubbleDiameter
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxBubbleOffset
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxCapsuleCornerRadius
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxCapsuleLength
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxCapsuleThickness
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxIconScale
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinBezierLength
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinBezierStrokeWidth
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinBezierWidth
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinBubbleDiameter
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinBubbleOffset
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinLineWidth
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxLineWidth
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinLineLength
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxLineLength
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinLineOffset
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxLineOffset
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinLineCornerRadius
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxLineCornerRadius
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinCapsuleCornerRadius
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinCapsuleLength
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinCapsuleThickness
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinIconScale
import hunoia.luno.settings.model.AnimationStyles
import hunoia.luno.settings.model.BubbleStyle
import hunoia.luno.settings.model.CapsuleStyle
import hunoia.luno.settings.model.ColorSource
import hunoia.luno.settings.model.LineStyle
import hunoia.luno.settings.model.ThemeColorKey
import hunoia.luno.settings.model.WaveStyle
import hunoia.luno.ui.screen.settings.gesture.getWaveStyleIcon
import hunoia.luno.ui.theme.MinInteractiveSize
import hunoia.luno.ui.theme.SectionPadding
import hunoia.luno.ui.theme.SubMinInteractiveSize
import hunoia.luno.ui.component.ColorPickerDialog
import hunoia.luno.ui.component.MyColorDisplay
import hunoia.luno.ui.component.MyColumn
import hunoia.luno.ui.component.MyExpandableColumn
import hunoia.luno.ui.component.SectionCard
import hunoia.luno.ui.component.TextActionButton
import hunoia.luno.ui.component.MyTextSlider
import hunoia.luno.ui.component.LabeledSwitch
import hunoia.luno.ui.component.ThemeColorPickerDialog
import kotlin.math.roundToInt

@Composable
fun AnimationStyleContent(
    onDismiss: () -> Unit,
    vm: AnimationStyleVM = viewModel()
) {
    val scrollState = rememberScrollState()
    UDFComponent(component = vm.udfComponent, onEvent = { }) { uiState ->
        var showThemeColorPickerFor by remember { mutableStateOf<String?>(null) }
        var pendingColorTarget by remember { mutableStateOf<String?>(null) }
        val type = uiState.currentType

        if (uiState.colorPickerDialog.first) {
            ColorPickerDialog(
                onDismissRequest = { vm.colorPickerDialog.show(false) },
                onColorPicked = { color ->
                    val argb = color.toArgb()
                    val target = pendingColorTarget ?: return@ColorPickerDialog
                    dispatchColorCustom(vm, type, target, argb)
                    vm.colorPickerDialog.show(false)
                    pendingColorTarget = null
                },
                initialColor = Color(uiState.colorPickerDialog.second)
            )
        }

        showThemeColorPickerFor?.let { target ->
            ThemeColorPickerDialog(
                onDismissRequest = { showThemeColorPickerFor = null },
                onColorPicked = { key ->
                    dispatchColorThemeKey(vm, type, target, key)
                    showThemeColorPickerFor = null
                }
            )
        }

        val waveStyle = uiState.waveStyle
        val capsuleStyle = uiState.capsuleStyle
        val bubbleStyle = uiState.bubbleStyle
        val lineStyle = uiState.lineStyle

        var localWaveStrokeWidth by remember(waveStyle.strokeWidth) { mutableStateOf(waveStyle.strokeWidth.toFloat()) }
        var localWaveWidth by remember(waveStyle.width) { mutableStateOf(waveStyle.width.toFloat()) }
        var localCapsuleStrokeWidth by remember(capsuleStyle.strokeWidth) { mutableStateOf(capsuleStyle.strokeWidth.toFloat()) }
        var localCapsuleThickness by remember(capsuleStyle.thickness) { mutableStateOf(capsuleStyle.thickness.toFloat()) }
        var localCapsuleMaxLength by remember(capsuleStyle.maxLength) { mutableStateOf(capsuleStyle.maxLength.toFloat()) }
        var localCapsuleCornerRadius by remember(capsuleStyle.cornerRadius) { mutableStateOf(capsuleStyle.cornerRadius.toFloat()) }
        var localBubbleStrokeWidth by remember(bubbleStyle.strokeWidth) { mutableStateOf(bubbleStyle.strokeWidth.toFloat()) }
        var localBubbleDiameter by remember(bubbleStyle.diameter) { mutableStateOf(bubbleStyle.diameter.toFloat()) }
        var localBubbleMaxOffset by remember(bubbleStyle.maxOffset) { mutableStateOf(bubbleStyle.maxOffset.toFloat()) }
        var localLineStrokeWidth by remember(lineStyle.strokeWidth) { mutableStateOf(lineStyle.strokeWidth.toFloat()) }
        var localLineWidth by remember(lineStyle.width) { mutableStateOf(lineStyle.width.toFloat()) }
        var localLineMaxLength by remember(lineStyle.maxLength) { mutableStateOf(lineStyle.maxLength.toFloat()) }
        var localLineMaxOffset by remember(lineStyle.maxOffset) { mutableStateOf(lineStyle.maxOffset.toFloat()) }
        var localLineCornerRadius by remember(lineStyle.cornerRadius) { mutableStateOf(lineStyle.cornerRadius.toFloat()) }

        MyColumn(scrollState = scrollState) {
            SectionCard(title = stringResource(id = R.string.color_outline)) {
                TextActionButton(
                    onClick = {
                        val bgSource = when (type) {
                            AnimationStyles.TYPE_WAVE -> waveStyle.backgroundColorSource
                            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.backgroundColorSource
                            AnimationStyles.TYPE_LINE -> lineStyle.backgroundColorSource
                    AnimationStyles.TYPE_LINE -> lineStyle.backgroundColor
                    else -> bubbleStyle.backgroundColorSource
                        }
                        if (bgSource == ColorSource.Theme) {
                            showThemeColorPickerFor = "bg"
                        } else {
                            pendingColorTarget = "bg"
                            val color = when (type) {
                                AnimationStyles.TYPE_WAVE -> waveStyle.backgroundColor
                                AnimationStyles.TYPE_CAPSULE -> capsuleStyle.backgroundColor
                                AnimationStyles.TYPE_LINE -> lineStyle.backgroundColor
                    else -> bubbleStyle.backgroundColor
                            }
                            vm.colorPickerDialog.show(show = true, color = color, belongsTo = null, targetType = type)
                        }
                    },
                    text = stringResource(id = R.string.background_color),
                    prefix = {
                        MyColorDisplay(
                            color = resolvePreviewColor(
                                when (type) {
                                    AnimationStyles.TYPE_WAVE -> waveStyle.backgroundColorSource
                                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.backgroundColorSource
                                    AnimationStyles.TYPE_LINE -> lineStyle.backgroundColorSource
                                    else -> bubbleStyle.backgroundColorSource
                                },
                                when (type) {
                                    AnimationStyles.TYPE_WAVE -> waveStyle.backgroundColorThemeKey
                                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.backgroundColorThemeKey
                                    AnimationStyles.TYPE_LINE -> lineStyle.backgroundColorThemeKey
                    else -> bubbleStyle.backgroundColorThemeKey
                                },
                                when (type) {
                                    AnimationStyles.TYPE_WAVE -> waveStyle.backgroundColor
                                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.backgroundColor
                                    AnimationStyles.TYPE_LINE -> lineStyle.backgroundColor
                    else -> bubbleStyle.backgroundColor
                                }
                            )
                        )
                    },
                    trailing = {
                        Switch(
                            checked = when (type) {
                                AnimationStyles.TYPE_WAVE -> waveStyle.backgroundColorSource
                                AnimationStyles.TYPE_CAPSULE -> capsuleStyle.backgroundColorSource
                                AnimationStyles.TYPE_LINE -> lineStyle.backgroundColorSource
                    else -> bubbleStyle.backgroundColorSource
                            } == ColorSource.Theme,
                            onCheckedChange = { checked ->
                                dispatchColorSource(vm, type, "bg",
                                    if (checked) ColorSource.Theme else ColorSource.Custom)
                            }
                        )
                    }
                )
                TextActionButton(
                    onClick = {
                        val strokeSource = when (type) {
                            AnimationStyles.TYPE_WAVE -> waveStyle.strokeColorSource
                            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.strokeColorSource
                            AnimationStyles.TYPE_LINE -> lineStyle.strokeColorSource
                    else -> bubbleStyle.strokeColorSource
                        }
                        if (strokeSource == ColorSource.Theme) {
                            showThemeColorPickerFor = "stroke"
                        } else {
                            pendingColorTarget = "stroke"
                            val color = when (type) {
                                AnimationStyles.TYPE_WAVE -> waveStyle.strokeColor
                                AnimationStyles.TYPE_CAPSULE -> capsuleStyle.strokeColor
                                AnimationStyles.TYPE_LINE -> lineStyle.strokeColor
                    else -> bubbleStyle.strokeColor
                            }
                            vm.colorPickerDialog.show(show = true, color = color, belongsTo = null, targetType = type)
                        }
                    },
                    text = stringResource(id = R.string.stroke_color),
                    prefix = {
                        MyColorDisplay(
                            color = resolvePreviewColor(
                                when (type) {
                                    AnimationStyles.TYPE_WAVE -> waveStyle.strokeColorSource
                                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.strokeColorSource
                                    AnimationStyles.TYPE_LINE -> lineStyle.strokeColorSource
                    else -> bubbleStyle.strokeColorSource
                                },
                                when (type) {
                                    AnimationStyles.TYPE_WAVE -> waveStyle.strokeColorThemeKey
                                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.strokeColorThemeKey
                                    AnimationStyles.TYPE_LINE -> lineStyle.strokeColorThemeKey
                    else -> bubbleStyle.strokeColorThemeKey
                                },
                                when (type) {
                                    AnimationStyles.TYPE_WAVE -> waveStyle.strokeColor
                                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.strokeColor
                                    AnimationStyles.TYPE_LINE -> lineStyle.strokeColor
                    else -> bubbleStyle.strokeColor
                                }
                            )
                        )
                    },
                    trailing = {
                        Switch(
                            checked = when (type) {
                                AnimationStyles.TYPE_WAVE -> waveStyle.strokeColorSource
                                AnimationStyles.TYPE_CAPSULE -> capsuleStyle.strokeColorSource
                                AnimationStyles.TYPE_LINE -> lineStyle.strokeColorSource
                    else -> bubbleStyle.strokeColorSource
                            } == ColorSource.Theme,
                            onCheckedChange = { checked ->
                                dispatchColorSource(vm, type, "stroke",
                                    if (checked) ColorSource.Theme else ColorSource.Custom)
                            }
                        )
                    }
                )
                MyTextSlider(
                    value = when (type) {
                        AnimationStyles.TYPE_WAVE -> localWaveStrokeWidth
                        AnimationStyles.TYPE_CAPSULE -> localCapsuleStrokeWidth
                        AnimationStyles.TYPE_LINE -> localLineStrokeWidth
                        else -> localBubbleStrokeWidth
                    },
                    onValueChange = { v ->
                        when (type) {
                            AnimationStyles.TYPE_WAVE -> localWaveStrokeWidth = v
                            AnimationStyles.TYPE_CAPSULE -> localCapsuleStrokeWidth = v
                            AnimationStyles.TYPE_LINE -> localLineStrokeWidth = v
                            else -> localBubbleStrokeWidth = v
                        }
                    },
                    onValueChangeFinished = {
                        when (type) {
                            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(strokeWidth = localWaveStrokeWidth.toInt()) }; vm.saveWaveSettings() }
                            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(strokeWidth = localCapsuleStrokeWidth.toInt()) }; vm.saveCapsuleSettings() }
                            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(strokeWidth = localLineStrokeWidth.toInt()) }; vm.saveLineSettings() }
                            else -> { vm.onBubbleStyleChange { it.copy(strokeWidth = localBubbleStrokeWidth.toInt()) }; vm.saveBubbleSettings() }
                        }
                    },
                    text = stringResource(id = R.string.stroke_width),
                    valueDisplay = "${when (type) {
                        AnimationStyles.TYPE_WAVE -> localWaveStrokeWidth
                        AnimationStyles.TYPE_CAPSULE -> localCapsuleStrokeWidth
                        AnimationStyles.TYPE_LINE -> localLineStrokeWidth
                        else -> localBubbleStrokeWidth
                    }.roundToInt()}px",
                    valueRange = MinBezierStrokeWidth.toFloat()..MaxBezierStrokeWidth.toFloat()
                )
            }

            SectionCard(
                modifier = Modifier.padding(top = SectionPadding),
                title = stringResource(id = R.string.shape_style)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StyleChip(
                        modifier = Modifier.weight(1f),
                        selected = type == AnimationStyles.TYPE_WAVE,
                        icon = { ShapePreview(WaveStyle.SHAPE_WAVE, Modifier.size(24.dp),
                            if (type == AnimationStyles.TYPE_WAVE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) },
                        label = stringResource(R.string.shape_wave),
                        onClick = { vm.onTabSelected(AnimationStyles.TYPE_WAVE) }
                    )
                    StyleChip(
                        modifier = Modifier.weight(1f),
                        selected = type == AnimationStyles.TYPE_LINE,
                        icon = { LinePreview(Modifier.size(24.dp),
                            if (type == AnimationStyles.TYPE_LINE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) },
                        label = stringResource(R.string.shape_line),
                        onClick = { vm.onTabSelected(AnimationStyles.TYPE_LINE) }
                    )
                    StyleChip(
                        modifier = Modifier.weight(1f),
                        selected = type == AnimationStyles.TYPE_CAPSULE,
                        icon = { CapsulePreview(Modifier.size(24.dp),
                            if (type == AnimationStyles.TYPE_CAPSULE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) },
                        label = stringResource(R.string.animation_style_capsule),
                        onClick = { vm.onTabSelected(AnimationStyles.TYPE_CAPSULE) }
                    )
                    StyleChip(
                        modifier = Modifier.weight(1f),
                        selected = type == AnimationStyles.TYPE_BUBBLE,
                        icon = { BubblePreview(Modifier.size(24.dp),
                            if (type == AnimationStyles.TYPE_BUBBLE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) },
                        label = stringResource(R.string.animation_style_bubble),
                        onClick = { vm.onTabSelected(AnimationStyles.TYPE_BUBBLE) }
                    )
                }
            }

            SectionCard(
                modifier = Modifier.padding(top = SectionPadding),
                title = stringResource(id = R.string.shape_size)
            ) {
                when (type) {
                    AnimationStyles.TYPE_WAVE -> {
                        MyTextSlider(
                            value = localWaveWidth,
                            onValueChange = { localWaveWidth = it },
                            onValueChangeFinished = { vm.onWaveStyleChange { it.copy(width = localWaveWidth.toInt()) }; vm.saveWaveSettings() },
                            text = stringResource(id = R.string.width),
                            valueDisplay = "${localWaveWidth.roundToInt()}px",
                            valueRange = MinBezierWidth.toFloat()..MaxBezierWidth.toFloat()
                        )
                        MyTextSlider(
                            value = waveStyle.bezierLengthHalfRatio.toFloat(),
                            onValueChange = { ratio -> vm.onWaveStyleChange { w -> w.copy(bezierLengthHalfRatio = ratio) } },
                            onValueChangeFinished = { vm.saveWaveSettings() },
                            text = stringResource(id = R.string.length),
                            valueDisplay = String.format("%.1f", waveStyle.bezierLengthHalfRatio),
                            valueRange = MinBezierLength.toFloat()..MaxBezierLength.toFloat()
                        )
                        LabeledSwitch(
                            onCheckedChange = { checked -> vm.onWaveStyleChange { it.copy(safeBounds = checked) }; vm.saveWaveSettings() },
                            checked = waveStyle.safeBounds,
                            text = stringResource(id = R.string.reserved_bounds),
                            secondaryText = stringResource(id = R.string.reserved_bounds_hint)
                        )
                        LabeledSwitch(
                            onCheckedChange = { checked -> vm.onWaveStyleChange { it.copy(transformEnabled = checked) }; vm.saveWaveSettings() },
                            checked = waveStyle.transformEnabled,
                            text = stringResource(id = R.string.bezier_transform),
                            secondaryText = stringResource(id = R.string.bezier_transform_hint)
                        )
                    }
                    AnimationStyles.TYPE_CAPSULE -> {
                        MyTextSlider(
                            value = localCapsuleThickness,
                            onValueChange = { localCapsuleThickness = it },
                            onValueChangeFinished = { vm.onCapsuleStyleChange { it.copy(thickness = localCapsuleThickness.toInt()) }; vm.saveCapsuleSettings() },
                            text = stringResource(id = R.string.thickness),
                            valueDisplay = "${localCapsuleThickness.roundToInt()}px",
                            valueRange = MinCapsuleThickness.toFloat()..MaxCapsuleThickness.toFloat()
                        )
                        MyTextSlider(
                            value = localCapsuleMaxLength,
                            onValueChange = { localCapsuleMaxLength = it },
                            onValueChangeFinished = { vm.onCapsuleStyleChange { it.copy(maxLength = localCapsuleMaxLength.toInt()) }; vm.saveCapsuleSettings() },
                            text = stringResource(id = R.string.max_length),
                            valueDisplay = "${localCapsuleMaxLength.roundToInt()}px",
                            valueRange = MinCapsuleLength.toFloat()..MaxCapsuleLength.toFloat()
                        )
                        MyTextSlider(
                            value = localCapsuleCornerRadius,
                            onValueChange = { localCapsuleCornerRadius = it },
                            onValueChangeFinished = { vm.onCapsuleStyleChange { it.copy(cornerRadius = localCapsuleCornerRadius.toInt()) }; vm.saveCapsuleSettings() },
                            text = stringResource(id = R.string.corner_radius),
                            valueDisplay = "${localCapsuleCornerRadius.roundToInt()}px",
                            valueRange = MinCapsuleCornerRadius.toFloat()..MaxCapsuleCornerRadius.toFloat()
                        )
                    }
                    AnimationStyles.TYPE_LINE -> {
                        MyTextSlider(
                            value = localLineWidth,
                            onValueChange = { localLineWidth = it },
                            onValueChangeFinished = { vm.onLineStyleChange { it.copy(width = localLineWidth.toInt()) }; vm.saveLineSettings() },
                            text = stringResource(id = R.string.width),
                            valueDisplay = "${localLineWidth.roundToInt()}px",
                            valueRange = MinLineWidth.toFloat()..MaxLineWidth.toFloat()
                        )
                        MyTextSlider(
                            value = localLineMaxLength,
                            onValueChange = { localLineMaxLength = it },
                            onValueChangeFinished = { vm.onLineStyleChange { it.copy(maxLength = localLineMaxLength.toInt()) }; vm.saveLineSettings() },
                            text = stringResource(id = R.string.max_length),
                            valueDisplay = "${localLineMaxLength.roundToInt()}px",
                            valueRange = MinLineLength.toFloat()..MaxLineLength.toFloat()
                        )
                        MyTextSlider(
                            value = localLineMaxOffset,
                            onValueChange = { localLineMaxOffset = it },
                            onValueChangeFinished = { vm.onLineStyleChange { it.copy(maxOffset = localLineMaxOffset.toInt()) }; vm.saveLineSettings() },
                            text = stringResource(id = R.string.max_offset),
                            valueDisplay = "${localLineMaxOffset.roundToInt()}px",
                            valueRange = MinLineOffset.toFloat()..MaxLineOffset.toFloat()
                        )
                        MyTextSlider(
                            value = localLineCornerRadius,
                            onValueChange = { localLineCornerRadius = it },
                            onValueChangeFinished = { vm.onLineStyleChange { it.copy(cornerRadius = localLineCornerRadius.toInt()) }; vm.saveLineSettings() },
                            text = stringResource(id = R.string.corner_radius),
                            valueDisplay = "${localLineCornerRadius.roundToInt()}px",
                            valueRange = MinLineCornerRadius.toFloat()..MaxLineCornerRadius.toFloat()
                        )
                    }
                    else -> {
                        MyTextSlider(
                            value = localBubbleDiameter,
                            onValueChange = { localBubbleDiameter = it },
                            onValueChangeFinished = { vm.onBubbleStyleChange { it.copy(diameter = localBubbleDiameter.toInt()) }; vm.saveBubbleSettings() },
                            text = stringResource(id = R.string.diameter),
                            valueDisplay = "${localBubbleDiameter.roundToInt()}px",
                            valueRange = MinBubbleDiameter.toFloat()..MaxBubbleDiameter.toFloat()
                        )
                        MyTextSlider(
                            value = localBubbleMaxOffset,
                            onValueChange = { localBubbleMaxOffset = it },
                            onValueChangeFinished = { vm.onBubbleStyleChange { it.copy(maxOffset = localBubbleMaxOffset.toInt()) }; vm.saveBubbleSettings() },
                            text = stringResource(id = R.string.max_offset),
                            valueDisplay = "${localBubbleMaxOffset.roundToInt()}px",
                            valueRange = MinBubbleOffset.toFloat()..MaxBubbleOffset.toFloat()
                        )
                    }
                }
            }

            IconSection(
                iconColor = when (type) {
                    AnimationStyles.TYPE_WAVE -> waveStyle.iconColor
                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconColor
                    AnimationStyles.TYPE_LINE -> lineStyle.iconColor
                    else -> bubbleStyle.iconColor
                },
                iconColorSource = when (type) {
                    AnimationStyles.TYPE_WAVE -> waveStyle.iconColorSource
                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconColorSource
                    AnimationStyles.TYPE_LINE -> lineStyle.iconColorSource
                    else -> bubbleStyle.iconColorSource
                },
                iconColorThemeKey = when (type) {
                    AnimationStyles.TYPE_WAVE -> waveStyle.iconColorThemeKey
                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconColorThemeKey
                    AnimationStyles.TYPE_LINE -> lineStyle.iconColorThemeKey
                    else -> bubbleStyle.iconColorThemeKey
                },
                iconScale = when (type) {
                    AnimationStyles.TYPE_WAVE -> waveStyle.iconScale
                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconScale
                    AnimationStyles.TYPE_LINE -> lineStyle.iconScale
                    else -> bubbleStyle.iconScale
                },
                iconType = when (type) {
                    AnimationStyles.TYPE_WAVE -> waveStyle.iconType
                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconType
                    AnimationStyles.TYPE_LINE -> lineStyle.iconType
                    else -> bubbleStyle.iconType
                },
                isCustomIconExpanded = when (type) {
                    AnimationStyles.TYPE_WAVE -> uiState.waveCustomIconExpanded
                    AnimationStyles.TYPE_CAPSULE -> uiState.capsuleCustomIconExpanded
                    else -> uiState.bubbleCustomIconExpanded
                },
                onColorClick = {
                    val iconSource = when (type) {
                        AnimationStyles.TYPE_WAVE -> waveStyle.iconColorSource
                        AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconColorSource
                        AnimationStyles.TYPE_LINE -> lineStyle.iconColorSource
                    else -> bubbleStyle.iconColorSource
                    }
                    if (iconSource == ColorSource.Theme) {
                        showThemeColorPickerFor = "icon"
                    } else {
                        pendingColorTarget = "icon"
                        val color = when (type) {
                            AnimationStyles.TYPE_WAVE -> waveStyle.iconColor
                            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconColor
                            AnimationStyles.TYPE_LINE -> lineStyle.iconColor
                    else -> bubbleStyle.iconColor
                        }
                        vm.colorPickerDialog.show(show = true, color = color, belongsTo = null, targetType = type)
                    }
                },
                onColorSourceChange = { checked ->
                    val source = if (checked) ColorSource.Theme else ColorSource.Custom
                    dispatchColorSource(vm, type, "icon", source)
                },
                onIconScaleChange = { scale ->
                    when (type) {
                        AnimationStyles.TYPE_WAVE -> vm.onWaveStyleChange { it.copy(iconScale = scale) }
                        AnimationStyles.TYPE_CAPSULE -> vm.onCapsuleStyleChange { it.copy(iconScale = scale) }
                        AnimationStyles.TYPE_LINE -> vm.onLineStyleChange { it.copy(iconScale = scale) }
                        else -> vm.onBubbleStyleChange { it.copy(iconScale = scale) }
                    }
                },
                onIconScaleChangeFinished = {
                    when (type) {
                        AnimationStyles.TYPE_WAVE -> vm.saveWaveSettings()
                        AnimationStyles.TYPE_CAPSULE -> vm.saveCapsuleSettings()
                        AnimationStyles.TYPE_LINE -> vm.saveLineSettings()
                        else -> vm.saveBubbleSettings()
                    }
                },
                onIconTypeChange = { iconType ->
                    when (type) {
                        AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(iconType = iconType) }; vm.saveWaveSettings() }
                        AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(iconType = iconType) }; vm.saveCapsuleSettings() }
                        AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(iconType = iconType) }; vm.saveLineSettings() }
                        else -> { vm.onBubbleStyleChange { it.copy(iconType = iconType) }; vm.saveBubbleSettings() }
                    }
                },
                onCustomIconExpandedChange = { expanded ->
                    when (type) {
                        AnimationStyles.TYPE_WAVE -> vm.onWaveCustomIconExpandedChange(expanded)
                        AnimationStyles.TYPE_CAPSULE -> vm.onCapsuleCustomIconExpandedChange(expanded)
                        AnimationStyles.TYPE_LINE -> vm.onLineCustomIconExpandedChange(expanded)
                        else -> vm.onBubbleCustomIconExpandedChange(expanded)
                    }
                }
            )
        }
    }
}

private fun dispatchColorThemeKey(vm: AnimationStyleVM, type: Int, target: String, key: ThemeColorKey) {
    when (target) {
        "bg" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(backgroundColorThemeKey = key) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(backgroundColorThemeKey = key) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(backgroundColorThemeKey = key) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(backgroundColorThemeKey = key) }; vm.saveBubbleSettings() }
        }
        "stroke" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(strokeColorThemeKey = key) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(strokeColorThemeKey = key) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(strokeColorThemeKey = key) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(strokeColorThemeKey = key) }; vm.saveBubbleSettings() }
        }
        "icon" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(iconColorThemeKey = key) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(iconColorThemeKey = key) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(iconColorThemeKey = key) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(iconColorThemeKey = key) }; vm.saveBubbleSettings() }
        }
    }
}

private fun dispatchColorCustom(vm: AnimationStyleVM, type: Int, target: String, argb: Int) {
    when (target) {
        "bg" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(backgroundColor = argb) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(backgroundColor = argb) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(backgroundColor = argb) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(backgroundColor = argb) }; vm.saveBubbleSettings() }
        }
        "stroke" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(strokeColor = argb) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(strokeColor = argb) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(strokeColor = argb) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(strokeColor = argb) }; vm.saveBubbleSettings() }
        }
        "icon" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(iconColor = argb) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(iconColor = argb) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(iconColor = argb) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(iconColor = argb) }; vm.saveBubbleSettings() }
        }
    }
}

private fun dispatchColorSource(vm: AnimationStyleVM, type: Int, target: String, source: ColorSource) {
    when (target) {
        "bg" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(backgroundColorSource = source) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(backgroundColorSource = source) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(backgroundColorSource = source) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(backgroundColorSource = source) }; vm.saveBubbleSettings() }
        }
        "stroke" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(strokeColorSource = source) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(strokeColorSource = source) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(strokeColorSource = source) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(strokeColorSource = source) }; vm.saveBubbleSettings() }
        }
        "icon" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(iconColorSource = source) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(iconColorSource = source) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(iconColorSource = source) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(iconColorSource = source) }; vm.saveBubbleSettings() }
        }
    }
}

@Composable
private fun resolvePreviewColor(source: ColorSource, themeKey: ThemeColorKey, customColor: Int): Color = when (source) {
    ColorSource.Custom -> Color(customColor)
    ColorSource.Theme -> when (themeKey) {
        ThemeColorKey.Primary -> MaterialTheme.colorScheme.primary
        ThemeColorKey.PrimaryContainer -> MaterialTheme.colorScheme.primaryContainer
        ThemeColorKey.Secondary -> MaterialTheme.colorScheme.secondary
        ThemeColorKey.SecondaryContainer -> MaterialTheme.colorScheme.secondaryContainer
        ThemeColorKey.Tertiary -> MaterialTheme.colorScheme.tertiary
        ThemeColorKey.TertiaryContainer -> MaterialTheme.colorScheme.tertiaryContainer
        ThemeColorKey.Surface -> MaterialTheme.colorScheme.surface
        ThemeColorKey.SurfaceVariant -> MaterialTheme.colorScheme.surfaceVariant
        ThemeColorKey.OnSurface -> MaterialTheme.colorScheme.onSurface
        ThemeColorKey.OnSurfaceVariant -> MaterialTheme.colorScheme.onSurfaceVariant
        ThemeColorKey.Outline -> MaterialTheme.colorScheme.outline
        ThemeColorKey.OutlineVariant -> MaterialTheme.colorScheme.outlineVariant
        ThemeColorKey.SurfaceContainerLow -> MaterialTheme.colorScheme.surfaceContainerLow
        ThemeColorKey.SurfaceContainer -> MaterialTheme.colorScheme.surfaceContainer
        ThemeColorKey.SurfaceContainerHigh -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
}

@Composable
private fun IconSection(
    iconColor: Int,
    iconColorSource: ColorSource,
    iconColorThemeKey: ThemeColorKey,
    iconScale: Float,
    iconType: Int,
    isCustomIconExpanded: Boolean,
    onColorClick: () -> Unit,
    onColorSourceChange: (Boolean) -> Unit,
    onIconScaleChange: (Float) -> Unit,
    onIconScaleChangeFinished: () -> Unit,
    onIconTypeChange: (Int) -> Unit,
    onCustomIconExpandedChange: (Boolean) -> Unit
) {
    SectionCard(
        modifier = Modifier.padding(top = SectionPadding),
        title = stringResource(id = R.string.icon)
    ) {
        TextActionButton(
            onClick = onColorClick,
            text = stringResource(id = R.string.tint),
            prefix = { MyColorDisplay(color = resolvePreviewColor(iconColorSource, iconColorThemeKey, iconColor)) },
            trailing = {
                Switch(checked = iconColorSource == ColorSource.Theme,
                    onCheckedChange = onColorSourceChange)
            }
        )

        MyTextSlider(
            value = iconScale,
            onValueChange = onIconScaleChange,
            onValueChangeFinished = onIconScaleChangeFinished,
            text = stringResource(id = R.string.icon_scale),
            valueDisplay = "${(iconScale * 100).roundToInt()}%",
            valueRange = MinIconScale..MaxIconScale
        )

        MyExpandableColumn(
            onExpandedChange = onCustomIconExpandedChange,
            title = stringResource(id = R.string.custom_icon),
            expanded = isCustomIconExpanded,
            shape = RectangleShape
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(MinInteractiveSize),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    WaveStyle.ICON_TYPE_ARROW,
                    WaveStyle.ICON_TYPE_TRIANGLE,
                    WaveStyle.ICON_TYPE_ANGLE,
                    WaveStyle.ICON_TYPE_ARROW_NEW
                ).fastForEach { iconTypeValue ->
                    val selected = iconType == iconTypeValue
                    Image(
                        modifier = Modifier.size(SubMinInteractiveSize)
                            .clipToBackground(
                                color = when (selected) { true -> MaterialTheme.colorScheme.primary else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) },
                                shape = CircleShape
                            )
                            .onSingleClick { onIconTypeChange(iconTypeValue) },
                        painter = getWaveStyleIcon(iconTypeValue),
                        contentDescription = null,
                        contentScale = ContentScale.Inside,
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }
    }
}

@Composable
private fun StyleChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clipToBackground(
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .onSingleClick { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(MaterialTheme.shapes.small)
                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

@Composable
private fun ShapePreview(shapeType: Int, modifier: Modifier, color: Color) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), MaterialTheme.shapes.small)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
        val path = Path()
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        when (shapeType) {
            WaveStyle.SHAPE_WAVE -> {
                path.moveTo(2f, cy)
                path.cubicTo(w * 0.3f, 2f, w * 0.7f, h - 2f, w - 2f, cy)
            }
            WaveStyle.SHAPE_LINE -> {
                path.moveTo(cx, 2f)
                path.lineTo(cx, h - 2f)
            }
        }
            drawPath(path, color = color, style = Stroke(2.5.dp.toPx()))
        }
    }
}

@Composable
private fun LinePreview(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), MaterialTheme.shapes.small)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val lineH = h * 0.75f
        val top = (h - lineH) / 2f
        val strokeWidth = w * 0.18f
        drawLine(
            color = color,
            start = Offset(cx, top),
            end = Offset(cx, top + lineH),
            strokeWidth = strokeWidth.coerceAtLeast(1f),
            cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun CapsulePreview(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), MaterialTheme.shapes.small)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
        val w = size.width
        val h = size.height
        val rectW = w * 0.8f
        val rectH = h * 0.55f
        val corner = rectH / 2f
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset((w - rectW) / 2f, (h - rectH) / 2f),
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(2.5.dp.toPx())
            )
        }
    }
}

@Composable
private fun BubblePreview(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), MaterialTheme.shapes.small)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = minOf(size.width, size.height) * 0.42f
            drawCircle(color = color, radius = r, center = Offset(cx, cy), style = Stroke(2.5.dp.toPx()))
        }
    }
}
