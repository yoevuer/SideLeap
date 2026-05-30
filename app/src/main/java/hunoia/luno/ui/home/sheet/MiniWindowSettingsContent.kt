package hunoia.luno.ui.home

import hunoia.luno.ui.component.input.MyTextSlider

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import hunoia.luno.R
import kotlin.math.roundToInt

@Composable
internal fun MiniWindowSettingsContent(uiState: UiState, vm: HomeVM) {
    Column {
        MyTextSlider(
            value = uiState.miniWindowHorizontalBias,
            onValueChange = { vm.onMiniWindowHorizontalBiasChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = stringResource(R.string.horizontal_offset),
            valueDisplay = "${(uiState.miniWindowHorizontalBias * 100).roundToInt()}%",
            valueRange = -1f..1f,
        )
        MyTextSlider(
            value = uiState.miniWindowVerticalBias,
            onValueChange = { vm.onMiniWindowVerticalBiasChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = stringResource(R.string.vertical_offset),
            valueDisplay = "${(uiState.miniWindowVerticalBias * 100).roundToInt()}%",
            valueRange = -1f..1f,
        )
        MyTextSlider(
            value = uiState.miniWindowWidthFraction,
            onValueChange = { vm.onMiniWindowWidthFractionChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = stringResource(R.string.width),
            valueDisplay = "${(uiState.miniWindowWidthFraction * 100).roundToInt()}%",
            valueRange = 0.2f..1.5f,
        )
        MyTextSlider(
            value = uiState.miniWindowHeightFraction,
            onValueChange = { vm.onMiniWindowHeightFractionChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = stringResource(R.string.height),
            valueDisplay = "${(uiState.miniWindowHeightFraction * 100).roundToInt()}%",
            valueRange = 0.2f..1.5f,
        )
    }
}
