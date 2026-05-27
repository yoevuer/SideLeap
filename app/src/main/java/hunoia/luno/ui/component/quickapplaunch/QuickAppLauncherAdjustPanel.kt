package hunoia.luno.ui.component.quickapplaunch
import hunoia.luno.ui.theme.*

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hunoia.luno.R
import hunoia.luno.settings.model.QuickAppLauncherSettings
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.ui.component.MyTextSlider
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun QuickAppLauncherAdjustPanel(
    onSettingsChanged: (QuickAppLauncherSettings) -> Unit,
) {
    val settings by SettingsProvider.quickAppLauncherSettings.collectAsState(initial = QuickAppLauncherSettings())
    val coroutineScope = rememberCoroutineScope()
    fun updateLayout(next: QuickAppLauncherSettings) {
        onSettingsChanged(next)
        coroutineScope.launch { SettingsProvider.updateQuickAppLauncherLayout(next) }
    }

    val screenH = LocalConfiguration.current.screenHeightDp
    val maxPanel = 80.dp * (settings.candidateRows + 2.25f) + Spacing20 + Spacing10 * 2
    val heightMax = (maxPanel / screenH.dp).coerceIn(0.35f, 0.85f)

    Column(modifier = Modifier.fillMaxWidth()) {
        MyTextSlider(
            value = settings.contentHeightFraction,
            onValueChange = { updateLayout(settings.copy(contentHeightFraction = it)) },
            text = stringResource(R.string.quick_app_launcher_content_height),
            valueDisplay = "${(settings.contentHeightFraction * 100).roundToInt()}%",
            valueRange = 0.35f..heightMax,
        )
        MyTextSlider(
            value = settings.panelWidthFraction,
            onValueChange = { updateLayout(settings.copy(panelWidthFraction = it)) },
            text = stringResource(R.string.quick_app_launcher_panel_width),
            valueDisplay = "${(settings.panelWidthFraction * 100).roundToInt()}%",
            valueRange = 0.65f..1.0f,
        )
        MyTextSlider(
            value = settings.panelHeightFraction,
            onValueChange = { updateLayout(settings.copy(panelHeightFraction = it)) },
            text = stringResource(R.string.quick_app_launcher_panel_height),
            valueDisplay = "${(settings.panelHeightFraction * 100).roundToInt()}%",
            valueRange = 0.05f..0.9f,
        )
        MyTextSlider(
            value = settings.panelHorizontalBias,
            onValueChange = { updateLayout(settings.copy(panelHorizontalBias = it)) },
            text = stringResource(R.string.quick_app_launcher_horizontal_bias),
            valueDisplay = "${(settings.panelHorizontalBias * 100).roundToInt()}%",
            valueRange = 0.0f..1.0f,
        )
        MyTextSlider(
            value = settings.candidateRows.toFloat(),
            onValueChange = { updateLayout(settings.copy(candidateRows = it.roundToInt().coerceIn(1, 3))) },
            text = stringResource(R.string.quick_app_launcher_candidate_rows),
            valueDisplay = settings.candidateRows.toString(),
            valueRange = 1f..3f,
        )
        MyTextSlider(
            value = settings.gridColumns.toFloat(),
            onValueChange = { updateLayout(settings.copy(gridColumns = it.roundToInt().coerceIn(3, 5))) },
            text = stringResource(R.string.quick_app_launcher_grid_columns),
            valueDisplay = settings.gridColumns.toString(),
            valueRange = 3f..5f,
        )
    }
}
