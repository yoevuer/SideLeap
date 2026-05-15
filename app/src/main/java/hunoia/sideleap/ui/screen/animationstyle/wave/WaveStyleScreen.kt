package hunoia.sideleap.ui.screen.animationstyle.wave

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import hunoia.sideleap.settings.SettingsUiDefaults.MaxBezierLength
import hunoia.sideleap.settings.SettingsUiDefaults.MaxBezierStrokeWidth
import hunoia.sideleap.settings.SettingsUiDefaults.MaxBezierWidth
import hunoia.sideleap.settings.SettingsUiDefaults.MaxIconScale
import hunoia.sideleap.settings.SettingsUiDefaults.MinBezierLength
import hunoia.sideleap.settings.SettingsUiDefaults.MinBezierStrokeWidth
import hunoia.sideleap.settings.SettingsUiDefaults.MinBezierWidth
import hunoia.sideleap.settings.SettingsUiDefaults.MinIconScale
import hunoia.sideleap.settings.model.WaveStyle.Companion.ICON_TYPE_ANGLE
import hunoia.sideleap.settings.model.WaveStyle.Companion.ICON_TYPE_ARROW
import hunoia.sideleap.settings.model.WaveStyle.Companion.ICON_TYPE_ARROW_NEW
import hunoia.sideleap.settings.model.WaveStyle.Companion.ICON_TYPE_TRIANGLE
import hunoia.sideleap.ui.screen.animationstyle.wave.getWaveStyleIcon
import hunoia.sideleap.ui.screen.animationstyle.wave.WaveStyleVM.UiEvent
import hunoia.sideleap.ui.theme.MinInteractiveSize
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.ui.theme.SubMinInteractiveSize
import hunoia.sideleap.ui.widget.ColorPickerDialog
import hunoia.sideleap.ui.widget.MyColorDisplay
import hunoia.sideleap.ui.widget.MyColumn
import hunoia.sideleap.ui.widget.MyExpandableColumn
import hunoia.sideleap.ui.widget.SectionCard
import hunoia.sideleap.ui.widget.TextActionButton
import hunoia.sideleap.ui.widget.MyTextSlider
import hunoia.sideleap.ui.widget.LabeledSwitch
import hunoia.sideleap.ui.widget.TopBar
import kotlinx.coroutines.launch

/**
 * @author DS-Z
 * @since 2025/11/4
 */

@Composable
fun WaveStyleScreen(
    onBack: () -> Unit,
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

        Column {
            TopBar(
                onBack = onBack,
                title = stringResource(id = R.string.animation_style)
            )
            MyColumn(scrollState = scrollState) {
                SectionCard(title = stringResource(id = R.string.color_outline)) {
                    TextActionButton(
                        onClick = {
                            vm.colorPickerDialog.show(
                                show = true,
                                color = uiState.animationStyle.backgroundColor,
                                belongsTo = uiState.animationStyle::backgroundColor
                            )
                        },
                        text = stringResource(id = R.string.background_color),
                        prefix = {
                            MyColorDisplay(color = Color(uiState.animationStyle.backgroundColor))
                        }
                    )
                    TextActionButton(
                        onClick = {
                            vm.colorPickerDialog.show(
                                show = true,
                                color = uiState.animationStyle.strokeColor,
                                belongsTo = uiState.animationStyle::strokeColor
                            )
                        },
                        text = stringResource(id = R.string.stroke_color),
                        prefix = {
                            MyColorDisplay(color = Color(uiState.animationStyle.strokeColor))
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
//                    LabeledSwitch(
//                        onCheckedChange = { vm.onStickySlideChange(it) },
//                        checked = uiState.animationStyle.stickySlideEnabled,
//                        text = stringResource(id = R.string.sticky_slide),
//                        secondaryText = stringResource(id = R.string.sticky_slide_tips)
//                    )
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
                            vm.colorPickerDialog.show(
                                show = true,
                                color = uiState.animationStyle.iconColor,
                                belongsTo = uiState.animationStyle::iconColor
                            )
                        },
                        text = stringResource(id = R.string.tint),
                        prefix = {
                            MyColorDisplay(color = Color(uiState.animationStyle.iconColor))
                        }
                    )
                    MyTextSlider(
                        value = uiState.animationStyle.iconScale,
                        onValueChange = { vm.onIconScaleChange(it) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.scaling),
                        sliderValueHint = stringResource(id = R.string.small) to stringResource(id = R.string.large),
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
                                    colorFilter = ColorFilter.tint(color = Color.White)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
