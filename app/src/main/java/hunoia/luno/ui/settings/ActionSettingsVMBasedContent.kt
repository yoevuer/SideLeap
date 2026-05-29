package hunoia.luno.ui.settings

import hunoia.luno.ui.theme.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.LoadingComponent
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.onSingleClick
import hunoia.luno.R
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxGotoBottomStrength
import hunoia.luno.config.defaults.SettingsUiDefaults.MinGotoBottomStrength
import hunoia.luno.ui.component.ExpressiveSwitchItem
import hunoia.luno.ui.component.input.MyTextSlider

@Composable
fun PreviousAppSettingsContent(vm: ActionSettingsVM = viewModel()) {
    UDFComponent(
        component = vm.udfComponent,
        onEvent = {}
    ) { uiState ->
        LoadingComponent(
            modifier = Modifier.fillMaxWidth(),
            component = vm.loadingComponent
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ItemPadding)
            ) {
                var inputPkgName by remember {
                    mutableStateOf(TextFieldValue())
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = inputPkgName,
                    onValueChange = { inputPkgName = it },
                    singleLine = true,
                    label = {
                        Text(
                            text = stringResource(R.string.exclude_app),
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    placeholder = {
                        Text(
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                            text = stringResource(R.string.typing_package_name_and_click_done),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            vm.onPreviousAppOperation(inputPkgName.text, true)
                            inputPkgName = TextFieldValue()
                        }
                    )
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(
                        items = uiState.actionSettings.previousApp.packageNames,
                        key = { it }
                    ) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ItemPadding)
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Image(
                                modifier = Modifier
                                    .size(MinInteractiveSize)
                                    .clip(CircleShape)
                                    .onSingleClick {
                                        vm.onPreviousAppOperation(item, false)
                                    },
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                contentScale = ContentScale.Inside,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GotoBottomSettingsContent(vm: ActionSettingsVM = viewModel()) {
    UDFComponent(
        component = vm.udfComponent,
        onEvent = {}
    ) { uiState ->
        LoadingComponent(
            modifier = Modifier.fillMaxWidth(),
            component = vm.loadingComponent
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ItemPadding)
            ) {
                var localStrength by remember(uiState.actionSettings.gotoBottom.strength) { mutableStateOf(uiState.actionSettings.gotoBottom.strength.toFloat()) }
                MyTextSlider(
                    value = localStrength,
                    onValueChange = { localStrength = it },
                    onValueChangeFinished = {
                        vm.onGotoBottomStrengthChange(localStrength)
                        vm.saveSettings()
                    },
                    text = stringResource(id = R.string.strength),
                    valueDisplay = String.format("%.1f", localStrength),
                    valueRange = MinGotoBottomStrength..MaxGotoBottomStrength
                )
            }
        }
    }
}

@Composable
fun HideGestureButtonSettingsContent(vm: ActionSettingsVM = viewModel()) {
    UDFComponent(
        component = vm.udfComponent,
        onEvent = {}
    ) { uiState ->
        LoadingComponent(
            modifier = Modifier.fillMaxWidth(),
            component = vm.loadingComponent
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ItemPadding)
            ) {
                val delayMs = uiState.actionSettings.hideGestureButton.delayMs
                Text(
                    text = stringResource(id = R.string.current_value_ms, delayMs),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                var localHideDelay by remember(delayMs) { mutableStateOf(delayMs.toFloat()) }
                MyTextSlider(
                    value = localHideDelay,
                    onValueChange = { localHideDelay = it },
                    onValueChangeFinished = {
                        vm.onHideGestureButtonDelayChange(localHideDelay)
                        vm.saveSettings()
                    },
                    text = stringResource(id = R.string.hide_gesture_button_delay_ms),
                    valueDisplay = "${localHideDelay.toLong()}ms",
                    valueRange = HideButtonDelayRange
                )
            }
        }
    }
}

@Composable
fun VolumeScrubSettingsContent(vm: ActionSettingsVM = viewModel()) {
    UDFComponent(
        component = vm.udfComponent,
        onEvent = {}
    ) { uiState ->
        LoadingComponent(
            modifier = Modifier.fillMaxWidth(),
            component = vm.loadingComponent
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ItemPadding)
            ) {
                ExpressiveSwitchItem(
                    onCheckedChange = { vm.onVolumeScrubHorizontalEnabledChange(it) },
                    checked = uiState.actionSettings.volumeScrub.horizontalEnabled,
                    title = stringResource(id = R.string.horizontal_volume_scrub),
                    subtitle = stringResource(id = R.string.horizontal_volume_scrub_hint)
                )
                val stepDp = uiState.actionSettings.volumeScrub.stepThresholdDp
                var localStepDp by remember(stepDp) { mutableStateOf(stepDp.toFloat()) }
                MyTextSlider(
                    value = localStepDp,
                    onValueChange = { localStepDp = it },
                    onValueChangeFinished = {
                        vm.onVolumeScrubStepThresholdChange(localStepDp)
                        vm.saveSettings()
                    },
                    text = stringResource(id = R.string.volume_scrub_sensitivity),
                    valueDisplay = "${localStepDp.toInt()}dp",
                    valueRange = VolumeScrubSensitivityRange
                )
            }
        }
    }
}
