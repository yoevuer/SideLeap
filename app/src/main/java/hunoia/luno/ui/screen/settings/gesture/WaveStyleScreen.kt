package hunoia.luno.ui.screen.settings.gesture
import hunoia.luno.ui.theme.*

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
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
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxBezierLength
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxBezierStrokeWidth
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxBezierStrokeWidthValue
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxBezierWidth
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxIconScale
import hunoia.luno.config.defaults.SettingsUiDefaults.MinBezierLength
import hunoia.luno.config.defaults.SettingsUiDefaults.MinBezierStrokeWidth
import hunoia.luno.config.defaults.SettingsUiDefaults.MinBezierWidth
import hunoia.luno.config.defaults.SettingsUiDefaults.MinIconScale
import hunoia.luno.config.model.ColorSource
import hunoia.luno.config.model.ThemeColorKey
import hunoia.luno.ui.ext.resolveColor
import hunoia.luno.config.model.WaveStyle
import hunoia.luno.config.model.WaveStyle.Companion.ICON_TYPE_ANGLE
import hunoia.luno.config.model.WaveStyle.Companion.ICON_TYPE_ARROW
import hunoia.luno.config.model.WaveStyle.Companion.ICON_TYPE_ARROW_NEW
import hunoia.luno.config.model.WaveStyle.Companion.ICON_TYPE_TRIANGLE
import hunoia.luno.config.model.WaveStyle.Companion.SHAPE_LINE
import hunoia.luno.config.model.WaveStyle.Companion.SHAPE_WAVE
import hunoia.luno.ui.screen.settings.gesture.getWaveStyleIcon
import hunoia.luno.ui.screen.settings.gesture.WaveStyleVM.UiEvent
import hunoia.luno.ui.theme.MinInteractiveSize
import hunoia.luno.ui.theme.SectionPadding
import hunoia.luno.ui.theme.SubMinInteractiveSize
import hunoia.luno.ui.component.ColorPickerBottomSheet
import hunoia.luno.ui.component.ColorSelection
import hunoia.luno.ui.component.MyColorDisplay
import hunoia.luno.ui.component.MyColumn
import hunoia.luno.ui.component.MyExpandableColumn
import hunoia.luno.ui.component.ExpressiveRow
import hunoia.luno.ui.component.ExpressiveSection
import hunoia.luno.ui.component.ExpressiveSwitchItem
import hunoia.luno.ui.component.MyTextSlider
import kotlinx.coroutines.launch
import kotlin.math.roundToInt



@Composable
private fun resolvePreviewColor(source: ColorSource, themeKey: ThemeColorKey, customColor: Int): Color = when (source) {
    ColorSource.Custom -> Color(customColor)
    ColorSource.Theme -> themeKey.resolveColor()
}

@Composable
private fun ShapePreview(shapeType: Int, modifier: Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val path = Path()
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        when (shapeType) {
            SHAPE_WAVE -> {
                path.moveTo(2f, cy)
                path.cubicTo(w * 0.3f, 2f, w * 0.7f, h - 2f, w - 2f, cy)
            }
            SHAPE_LINE -> {
                path.moveTo(cx, 2f)
                path.lineTo(cx, h - 2f)
            }
        }
        drawPath(path, color = color, style = Stroke(Spacing2.toPx()))
    }
}

@Composable
fun WaveStyleContent(
    onDismiss: () -> Unit,
    vm: WaveStyleVM = viewModel()
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    UDFComponent(
        component = vm.udfComponent,
        onEvent = { event ->
            when (event) {
                UiEvent.ScrollToBottom -> {
                    coroutineScope.launch {
                        scrollState.animateScrollBy(
                            value = 1000f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    }
                }
            }
        }
    ) { uiState ->
        var colorPickerTarget by remember { mutableStateOf<String?>(null) }
        colorPickerTarget?.let { target ->
            val style = uiState.animationStyle
            val initial = when (target) {
                "bg" -> resolvePreviewColor(style.backgroundColorSource, style.backgroundColorThemeKey, style.backgroundColor)
                "stroke" -> resolvePreviewColor(style.strokeColorSource, style.strokeColorThemeKey, style.strokeColor)
                else -> resolvePreviewColor(style.iconColorSource, style.iconColorThemeKey, style.iconColor)
            }
            ColorPickerBottomSheet(
                onDismissRequest = { colorPickerTarget = null },
                onColorSelected = { selection ->
                    when (selection) {
                        is ColorSelection.Custom -> {
                            when (target) {
                                "bg" -> { vm.colorPickerDialog.show(show = true, color = style.backgroundColor, belongsTo = style::backgroundColor) }
                                "stroke" -> { vm.colorPickerDialog.show(show = true, color = style.strokeColor, belongsTo = style::strokeColor) }
                                else -> { vm.colorPickerDialog.show(show = true, color = style.iconColor, belongsTo = style::iconColor) }
                            }
                            vm.colorPickerDialog.onColorChange(selection.color.toArgb())
                            vm.colorPickerDialog.confirm()
                        }
                        is ColorSelection.Theme -> {
                            when (target) {
                                "bg" -> vm.onBackgroundColorThemeKeyChange(selection.key)
                                "stroke" -> vm.onStrokeColorThemeKeyChange(selection.key)
                                else -> vm.onIconColorThemeKeyChange(selection.key)
                            }
                        }
                    }
                    colorPickerTarget = null
                },
                initialColor = initial,
            )
        }

        MyColumn(scrollState = scrollState) {
                    val style = uiState.animationStyle

                    var localStrokeWidth by remember(style.strokeWidth) { mutableStateOf(style.strokeWidth.toFloat()) }
                    var localWidth by remember(style.width) { mutableStateOf(style.width.toFloat()) }

                    ExpressiveSection(title = stringResource(id = R.string.color_outline)) {
                    ExpressiveRow(
                        onClick = { colorPickerTarget = "bg" },
                        text = stringResource(id = R.string.background_color),
                        icon = {
                            MyColorDisplay(color = resolvePreviewColor(style.backgroundColorSource, style.backgroundColorThemeKey, style.backgroundColor))
                        },
                        trailing = {
                            Switch(
                                checked = style.backgroundColorSource == ColorSource.Theme,
                                onCheckedChange = { vm.onBackgroundColorSourceChange(if (it) ColorSource.Theme else ColorSource.Custom) }
                            )
                        }
                    )
                    ExpressiveRow(
                        onClick = { colorPickerTarget = "stroke" },
                        text = stringResource(id = R.string.stroke_color),
                        icon = {
                            MyColorDisplay(color = resolvePreviewColor(style.strokeColorSource, style.strokeColorThemeKey, style.strokeColor))
                        },
                        trailing = {
                            Switch(
                                checked = style.strokeColorSource == ColorSource.Theme,
                                onCheckedChange = { vm.onStrokeColorSourceChange(if (it) ColorSource.Theme else ColorSource.Custom) }
                            )
                        }
                    )
                    MyTextSlider(
                        value = localStrokeWidth,
                        onValueChange = { localStrokeWidth = it },
                        onValueChangeFinished = {
                            vm.onStrokeWidthChange(localStrokeWidth)
                            vm.saveSettings()
                        },
                        text = stringResource(id = R.string.stroke_width),
                        valueDisplay = "${localStrokeWidth.roundToInt()}px",
                        valueRange = MinBezierStrokeWidth.toFloat()..MaxBezierStrokeWidth.toFloat()
                    )
                }

                ExpressiveSection(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.shape_style)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing16, vertical = Spacing4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            SHAPE_WAVE to stringResource(id = R.string.shape_wave),
                            SHAPE_LINE to stringResource(id = R.string.shape_line),
                        ).fastForEach { (type, label) ->
                            val selected = uiState.animationStyle.shapeType == type
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clipToBackground(
                                        color = when (selected) {
                                            true -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = RoundedCornerShape(Spacing12)
                                    )
                                    .onSingleClick { vm.onShapeTypeChange(type) }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(Spacing32)
                                        .clip(RoundedCornerShape(Spacing6))
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainerHigh
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ShapePreview(
                                        shapeType = type,
                                        modifier = Modifier.size(Spacing24),
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(Spacing4))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                ExpressiveSection(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.shape_size)
                ) {
                    MyTextSlider(
                        value = localWidth,
                        onValueChange = { localWidth = it },
                        onValueChangeFinished = {
                            vm.onWidthChange(localWidth)
                            vm.saveSettings()
                        },
                        text = stringResource(id = R.string.width),
                        valueDisplay = "${localWidth.roundToInt()}px",
                        valueRange = MinBezierWidth.toFloat()..MaxBezierWidth.toFloat()
                    )
                    MyTextSlider(
                        value = uiState.animationStyle.bezierLengthHalfRatio,
                        onValueChange = { vm.onLengthHalfRatioChange(it) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.length),
                        valueDisplay = String.format("%.1f", uiState.animationStyle.bezierLengthHalfRatio),
                        valueRange = MinBezierLength..MaxBezierLength
                    )
                    ExpressiveSwitchItem(
                        onCheckedChange = { vm.onSafeBoundsChange(it) },
                        checked = uiState.animationStyle.safeBounds,
                        title = stringResource(id = R.string.reserved_bounds),
                        subtitle = stringResource(id = R.string.reserved_bounds_hint)
                    )
                    ExpressiveSwitchItem(
                        onCheckedChange = { vm.onTransformEnabledChange(it) },
                        checked = uiState.animationStyle.transformEnabled,
                        title = stringResource(id = R.string.bezier_transform),
                        subtitle = stringResource(id = R.string.bezier_transform_hint)
                    )
                }

                ExpressiveSection(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.icon)
                ) {
                    ExpressiveRow(
                        onClick = { colorPickerTarget = "icon" },
                        text = stringResource(id = R.string.tint),
                        icon = {
                            MyColorDisplay(color = resolvePreviewColor(style.iconColorSource, style.iconColorThemeKey, style.iconColor))
                        },
                        trailing = {
                            Switch(
                                checked = style.iconColorSource == ColorSource.Theme,
                                onCheckedChange = { vm.onIconColorSourceChange(if (it) ColorSource.Theme else ColorSource.Custom) }
                            )
                        }
                    )

                    MyTextSlider(
                        value = uiState.animationStyle.iconScale,
                        onValueChange = { vm.onIconScaleChange(it) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.icon_scale),
                        valueDisplay = "${(uiState.animationStyle.iconScale * 100).roundToInt()}%",
                        valueRange = MinIconScale..MaxIconScale
                    )

                    MyExpandableColumn(
                        onExpandedChange = { vm.onCustomIconExpandedChange(it) },
                        title = stringResource(id = R.string.custom_icon),
                        expanded = uiState.isCustomIconExpanded,
                        shape = RectangleShape
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(MinInteractiveSize),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(
                                ICON_TYPE_ARROW,
                                ICON_TYPE_TRIANGLE,
                                ICON_TYPE_ANGLE,
                                ICON_TYPE_ARROW_NEW
                            ).fastForEach { iconType ->
                                val selected = uiState.animationStyle.iconType == iconType
                                Image(
                                    modifier = Modifier
                                        .size(SubMinInteractiveSize)
                                        .clipToBackground(
                                            color = when (selected) {
                                                true -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            },
                                            shape = CircleShape
                                        )
                                        .onSingleClick {
                                            vm.onIconTypeChange(iconType)
                                        },
                                    painter = getWaveStyleIcon(iconType),
                                    contentDescription = null,
                                    contentScale = ContentScale.Inside,
                                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
