package hunoia.sideleap.ui.screen.settings.gesture

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onSingleClick
import hunoia.sideleap.R
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxBezierLength
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxBezierStrokeWidth
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxBezierWidth
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxIconScale
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinBezierLength
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinBezierStrokeWidth
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinBezierWidth
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinIconScale
import hunoia.sideleap.settings.model.ColorSource
import hunoia.sideleap.settings.model.ThemeColorKey
import hunoia.sideleap.settings.model.WaveStyle.Companion.ICON_TYPE_ANGLE
import hunoia.sideleap.settings.model.WaveStyle.Companion.ICON_TYPE_ARROW
import hunoia.sideleap.settings.model.WaveStyle.Companion.ICON_TYPE_ARROW_NEW
import hunoia.sideleap.settings.model.WaveStyle.Companion.ICON_TYPE_TRIANGLE
import hunoia.sideleap.ui.screen.settings.gesture.getWaveStyleIcon
import hunoia.sideleap.ui.screen.settings.gesture.WaveStyleVM.UiEvent
import hunoia.sideleap.ui.theme.MinInteractiveSize
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.ui.theme.SubMinInteractiveSize
import hunoia.sideleap.ui.component.ColorPickerDialog
import hunoia.sideleap.ui.component.MyColorDisplay
import hunoia.sideleap.ui.component.MyColumn
import hunoia.sideleap.ui.component.MyExpandableColumn
import hunoia.sideleap.ui.component.SectionCard
import hunoia.sideleap.ui.component.TextActionButton
import hunoia.sideleap.ui.component.MyTextSlider
import hunoia.sideleap.ui.component.LabeledSwitch
import hunoia.sideleap.ui.component.ThemeColorPickerDialog
import kotlinx.coroutines.launch

/**
 * @author DS-Z
 * @since 2025/11/4
 */

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
        if (uiState.colorPickerDialog.first) {
            ColorPickerDialog(
                onDismissRequest = {
                    vm.colorPickerDialog.show(false)
                },
                onColorPicked = { color ->
                    vm.colorPickerDialog.onColorChange(color.toArgb())
                    vm.colorPickerDialog.confirm()
                },
                initialColor = Color(uiState.colorPickerDialog.second)
            )
        }

        var showThemeColorPickerFor by remember { mutableStateOf<String?>(null) }
        showThemeColorPickerFor?.let { target ->
            ThemeColorPickerDialog(
                onDismissRequest = { showThemeColorPickerFor = null },
                onColorPicked = { key ->
                    when (target) {
                        "bg" -> vm.onBackgroundColorThemeKeyChange(key)
                        "stroke" -> vm.onStrokeColorThemeKeyChange(key)
                        "icon" -> vm.onIconColorThemeKeyChange(key)
                    }
                    showThemeColorPickerFor = null
                }
            )
        }

        MyColumn(scrollState = scrollState) {
            val style = uiState.animationStyle

            SectionCard(title = stringResource(id = R.string.color_outline)) {
                    TextActionButton(
                        onClick = {
                            if (style.backgroundColorSource == ColorSource.Theme) {
                                showThemeColorPickerFor = "bg"
                            } else {
                                vm.colorPickerDialog.show(
                                    show = true,
                                    color = style.backgroundColor,
                                    belongsTo = style::backgroundColor
                                )
                            }
                        },
                        text = stringResource(id = R.string.background_color),
                        prefix = {
                            MyColorDisplay(color = resolvePreviewColor(style.backgroundColorSource, style.backgroundColorThemeKey, style.backgroundColor))
                        },
                        trailing = {
                            Switch(
                                checked = style.backgroundColorSource == ColorSource.Theme,
                                onCheckedChange = { vm.onBackgroundColorSourceChange(if (it) ColorSource.Theme else ColorSource.Custom) }
                            )
                        }
                    )
                    TextActionButton(
                        onClick = {
                            if (style.strokeColorSource == ColorSource.Theme) {
                                showThemeColorPickerFor = "stroke"
                            } else {
                                vm.colorPickerDialog.show(
                                    show = true,
                                    color = style.strokeColor,
                                    belongsTo = style::strokeColor
                                )
                            }
                        },
                        text = stringResource(id = R.string.stroke_color),
                        prefix = {
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
                        value = uiState.animationStyle.strokeWidth.toFloat(),
                        onValueChange = { vm.onStrokeWidthChange(it) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.stroke_width),
                        sliderValueHint = stringResource(id = R.string.small) to stringResource(id = R.string.large),
                        valueRange = MinBezierStrokeWidth.toFloat()..MaxBezierStrokeWidth.toFloat()
                    )
                }

                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.shape_size)
                ) {
                    MyTextSlider(
                        value = uiState.animationStyle.width.toFloat(),
                        onValueChange = { vm.onWidthChange(it) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.width),
                        sliderValueHint = stringResource(id = R.string.small) to stringResource(id = R.string.large),
                        valueRange = MinBezierWidth.toFloat()..MaxBezierWidth.toFloat()
                    )
                    MyTextSlider(
                        value = uiState.animationStyle.bezierLengthHalfRatio.toFloat(),
                        onValueChange = { vm.onLengthHalfRatioChange(it) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.length),
                        sliderValueHint = stringResource(id = R.string.short1) to stringResource(id = R.string.long1),
                        valueRange = MinBezierLength.toFloat()..MaxBezierLength.toFloat()
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onSafeBoundsChange(it) },
                        checked = uiState.animationStyle.safeBounds,
                        text = stringResource(id = R.string.reserved_bounds),
                        secondaryText = stringResource(id = R.string.reserved_bounds_hint)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onTransformEnabledChange(it) },
                        checked = uiState.animationStyle.transformEnabled,
                        text = stringResource(id = R.string.bezier_transform),
                        secondaryText = stringResource(id = R.string.bezier_transform_hint)
                    )
                }

                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.icon)
                ) {
                    TextActionButton(
                        onClick = {
                            if (style.iconColorSource == ColorSource.Theme) {
                                showThemeColorPickerFor = "icon"
                            } else {
                                vm.colorPickerDialog.show(
                                    show = true,
                                    color = style.iconColor,
                                    belongsTo = style::iconColor
                                )
                            }
                        },
                        text = stringResource(id = R.string.tint),
                        prefix = {
                            MyColorDisplay(color = resolvePreviewColor(style.iconColorSource, style.iconColorThemeKey, style.iconColor))
                        },
                        trailing = {
                            Switch(
                                checked = style.iconColorSource == ColorSource.Theme,
                                onCheckedChange = { vm.onIconColorSourceChange(if (it) ColorSource.Theme else ColorSource.Custom) }
                            )
                        }
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
                                    colorFilter = ColorFilter.tint(color = Color.White)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
