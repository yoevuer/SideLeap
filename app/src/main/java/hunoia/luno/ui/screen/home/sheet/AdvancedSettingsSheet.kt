package hunoia.luno.ui.screen.home.sheet

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import hunoia.luno.R
import hunoia.luno.ui.component.BottomSheetNestedContent
import hunoia.luno.ui.component.LabeledSwitch
import hunoia.luno.ui.component.MyColumn
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.component.OptimizedScrollState
import hunoia.luno.ui.component.SectionCard
import hunoia.luno.ui.screen.settings.AdvancedSettingsVM
import hunoia.luno.ui.theme.SectionPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsSheet(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return
    val vm: AdvancedSettingsVM = viewModel()
    val scrollState = rememberScrollState()
    OptimizedBottomSheet(
        onDismissRequest = onDismiss,
        scrollState = OptimizedScrollState.Scroll(scrollState)
    ) {
        BottomSheetNestedContent(scrollState = OptimizedScrollState.Scroll(scrollState)) {
            UDFComponent(component = vm.udfComponent, onEvent = {}) { uiState ->
                MyColumn {
                    SectionCard(title = stringResource(R.string.app_management)) {
                    }
                    SectionCard(
                        modifier = Modifier.padding(top = SectionPadding),
                        title = stringResource(R.string.gesture_behavior)
                    ) {
                        LabeledSwitch(
                            onCheckedChange = { vm.onFitSoftKeyboardChange(it) },
                            checked = uiState.fitSoftKeyboard,
                            text = stringResource(R.string.fit_soft_keyboard),
                            secondaryText = stringResource(R.string.fit_soft_keyboard_hint)
                        )
                        LabeledSwitch(
                            onCheckedChange = { vm.onPreciseSlideTypeChange(it) },
                            checked = uiState.isPreciseSlideTypeEnabled,
                            text = stringResource(R.string.precise_slide_type),
                            secondaryText = stringResource(R.string.precise_slide_type_hint)
                        )
                    }
                    SectionCard(
                        modifier = Modifier.padding(top = SectionPadding),
                        title = stringResource(R.string.hide_gesture_button)
                    ) {
                        LabeledSwitch(
                            onCheckedChange = { vm.onHideLandscapeChange(it) },
                            checked = uiState.hideLandscape,
                            text = stringResource(R.string.landscape)
                        )
                        LabeledSwitch(
                            onCheckedChange = { vm.onHideScreenLockChange(it) },
                            checked = uiState.hideScreenLock,
                            text = stringResource(R.string.lock_screen)
                        )
                        LabeledSwitch(
                            onCheckedChange = { vm.onHideHomeScreenChange(it) },
                            checked = uiState.hideHomeScreen,
                            text = stringResource(R.string.launcher)
                        )
                    }
                }
            }
        }
    }
}
