package hunoia.luno.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.fastForEach
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onSingleClick
import hunoia.luno.R
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxIconScale
import hunoia.luno.config.defaults.SettingsUiDefaults.MinIconScale
import hunoia.luno.config.model.ColorSource
import hunoia.luno.config.model.ThemeColorKey
import hunoia.luno.config.model.WaveStyle
import hunoia.luno.ui.component.ExpressiveRow
import hunoia.luno.ui.component.ExpressiveSection
import hunoia.luno.ui.component.MyColorDisplay
import hunoia.luno.ui.component.MyExpandableColumn
import hunoia.luno.ui.component.MyTextSlider
import hunoia.luno.ui.screen.settings.gesture.getWaveStyleIcon
import hunoia.luno.ui.theme.MinInteractiveSize
import hunoia.luno.ui.theme.SectionPadding
import hunoia.luno.ui.theme.SubMinInteractiveSize
import kotlin.math.roundToInt

@Composable
fun IconSection(
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
    ExpressiveSection(
        modifier = Modifier.padding(top = SectionPadding),
        title = stringResource(id = R.string.icon)
    ) {
        ExpressiveRow(
            onClick = onColorClick,
            text = stringResource(id = R.string.tint),
            icon = { MyColorDisplay(color = resolvePreviewColor(iconColorSource, iconColorThemeKey, iconColor)) },
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
