package hunoia.luno.ui.screen.home

import hunoia.luno.ui.component.MyTextSlider

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kotlin.math.roundToInt

@Composable
internal fun MiniWindowSettingsContent(uiState: UiState, vm: HomeVM) {
    Column {
        MyTextSlider(
            value = uiState.miniWindowHorizontalBias,
            onValueChange = { vm.onMiniWindowHorizontalBiasChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = "水平偏移",
            valueDisplay = "${(uiState.miniWindowHorizontalBias * 100).roundToInt()}%",
            valueRange = -1f..1f,
        )
        MyTextSlider(
            value = uiState.miniWindowVerticalBias,
            onValueChange = { vm.onMiniWindowVerticalBiasChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = "垂直偏移",
            valueDisplay = "${(uiState.miniWindowVerticalBias * 100).roundToInt()}%",
            valueRange = -1f..1f,
        )
        MyTextSlider(
            value = uiState.miniWindowWidthFraction,
            onValueChange = { vm.onMiniWindowWidthFractionChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = "宽度",
            valueDisplay = "${(uiState.miniWindowWidthFraction * 100).roundToInt()}%",
            valueRange = 0.2f..1.5f,
        )
        MyTextSlider(
            value = uiState.miniWindowHeightFraction,
            onValueChange = { vm.onMiniWindowHeightFractionChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = "高度",
            valueDisplay = "${(uiState.miniWindowHeightFraction * 100).roundToInt()}%",
            valueRange = 0.2f..1.5f,
        )
    }
}
