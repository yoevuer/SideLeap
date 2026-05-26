package hunoia.sideleap.ui.screen.settings.gesture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hunoia.sideleap.R
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxGridColumns
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxGridCornerRadius
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxGridRows
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxGridScrollHotZoneHeight
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxGridScrollSpeed
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxItemSize
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinGridColumns
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinGridCornerRadius
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinGridRows
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinGridScrollHotZoneHeight
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinGridScrollSpeed
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinItemSize
import hunoia.sideleap.settings.model.GridStyle
import hunoia.sideleap.ui.theme.RootPadding
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.ui.component.MyTextSlider
import hunoia.sideleap.ui.component.SectionCard
import kotlin.math.roundToInt

@Composable
fun GridStyleSettingsContent(
    gridStyle: GridStyle,
    onStyleChange: (GridStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    var localItemSize by remember(gridStyle.itemSize) { mutableStateOf(gridStyle.itemSize.toFloat()) }
    var localColumns by remember(gridStyle.columns) { mutableStateOf(gridStyle.columns.toFloat()) }
    var localRows by remember(gridStyle.rows) { mutableStateOf(gridStyle.rows.toFloat()) }
    var localScrollSpeed by remember(gridStyle.scrollSpeed) { mutableStateOf(gridStyle.scrollSpeed.toFloat()) }
    var localScrollHotZoneHeight by remember(gridStyle.scrollHotZoneHeight) { mutableStateOf(gridStyle.scrollHotZoneHeight.toFloat()) }
    var localCornerRadius by remember(gridStyle.cornerRadius) { mutableStateOf(gridStyle.cornerRadius.toFloat()) }

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(RootPadding)
            .padding(bottom = ScrollBottomPadding)
    ) {
        SectionCard(title = stringResource(id = R.string.icon)) {
            MyTextSlider(
                value = localItemSize,
                onValueChange = { localItemSize = it },
                onValueChangeFinished = { onStyleChange(gridStyle.copy(itemSize = localItemSize.toInt())) },
                text = stringResource(id = R.string.icon_size),
                valueDisplay = "${localItemSize.roundToInt()}px",
                valueRange = MinItemSize.toFloat()..MaxItemSize.toFloat()
            )
            MyTextSlider(
                value = localColumns,
                onValueChange = { localColumns = it },
                onValueChangeFinished = { onStyleChange(gridStyle.copy(columns = localColumns.toInt())) },
                text = stringResource(id = R.string.columns),
                valueDisplay = "${localColumns.roundToInt()}",
                valueRange = MinGridColumns.toFloat()..MaxGridColumns.toFloat()
            )
            MyTextSlider(
                value = localRows,
                onValueChange = { localRows = it },
                onValueChangeFinished = { onStyleChange(gridStyle.copy(rows = localRows.toInt())) },
                text = stringResource(id = R.string.rows),
                valueDisplay = "${localRows.roundToInt()}",
                valueRange = MinGridRows.toFloat()..MaxGridRows.toFloat()
            )
        }
        SectionCard(
            modifier = Modifier.padding(top = SectionPadding),
            title = stringResource(id = R.string.scrolling)
        ) {
            MyTextSlider(
                value = localScrollSpeed,
                onValueChange = { localScrollSpeed = it },
                onValueChangeFinished = { onStyleChange(gridStyle.copy(scrollSpeed = localScrollSpeed.toInt())) },
                text = stringResource(id = R.string.scroll_speed),
                valueDisplay = "${localScrollSpeed.roundToInt()}",
                valueRange = MinGridScrollSpeed.toFloat()..MaxGridScrollSpeed.toFloat()
            )
            MyTextSlider(
                value = localScrollHotZoneHeight,
                onValueChange = { localScrollHotZoneHeight = it },
                onValueChangeFinished = { onStyleChange(gridStyle.copy(scrollHotZoneHeight = localScrollHotZoneHeight.toInt())) },
                text = stringResource(id = R.string.scroll_hot_zone_height),
                valueDisplay = "${localScrollHotZoneHeight.roundToInt()}px",
                valueRange = MinGridScrollHotZoneHeight.toFloat()..MaxGridScrollHotZoneHeight.toFloat()
            )
        }
        SectionCard(
            modifier = Modifier.padding(top = SectionPadding),
            title = stringResource(id = R.string.background)
        ) {
            MyTextSlider(
                value = localCornerRadius,
                onValueChange = { localCornerRadius = it },
                onValueChangeFinished = { onStyleChange(gridStyle.copy(cornerRadius = localCornerRadius.toInt())) },
                text = stringResource(id = R.string.corner_radius),
                valueDisplay = "${localCornerRadius.roundToInt()}px",
                valueRange = MinGridCornerRadius.toFloat()..MaxGridCornerRadius.toFloat()
            )
        }
    }
}
