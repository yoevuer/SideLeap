package hunoia.luno.ui.screen.settings.gesture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import hunoia.luno.R
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxGridColumns
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxGridCornerRadius
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxGridRows
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxItemSize
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinGridColumns
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinGridCornerRadius
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinGridRows
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinItemSize
import hunoia.luno.settings.model.GridStyle
import hunoia.luno.ui.theme.RootPadding
import hunoia.luno.ui.theme.ScrollBottomPadding
import hunoia.luno.ui.theme.SectionPadding
import hunoia.luno.ui.component.ExpressiveSection
import hunoia.luno.ui.component.MyTextSlider
import hunoia.luno.ui.screen.settings.gesture.GridStylePreview
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
    var localCornerRadius by remember(gridStyle.cornerRadius) { mutableStateOf(gridStyle.cornerRadius.toFloat()) }

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(RootPadding)
            .padding(bottom = ScrollBottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GridStylePreview(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(bottom = SectionPadding)
        )
        ExpressiveSection(title = stringResource(id = R.string.icon)) {
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
        ExpressiveSection(
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
