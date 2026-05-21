package hunoia.sideleap.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.onSingleClick
import hunoia.sideleap.R
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVerticalWithSection
import hunoia.sideleap.ui.theme.EdgeMenuPadding
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinItemHeightNoSecondary
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.ui.screen.freeze.AppBlacklistContent
import hunoia.sideleap.ui.component.BottomSheetNestedContent
import hunoia.sideleap.ui.component.MyColumn
import hunoia.sideleap.ui.component.MyAlertDialog
import hunoia.sideleap.ui.component.SectionCard
import hunoia.sideleap.ui.component.TextActionButton
import hunoia.sideleap.ui.component.LabeledSwitch
import hunoia.sideleap.ui.component.MyTextSlider
import hunoia.sideleap.ui.component.TopBar
import kotlin.math.roundToInt

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/23
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(
    onBack: () -> Unit,
    vm: AdvancedSettingsVM = viewModel()
) {
    var showAppBlacklist by remember { mutableStateOf(false) }
    UDFComponent(component = vm.udfComponent, onEvent = {}) { uiState ->
        var confirmClear by remember { mutableStateOf(false) }
        Column {
            TopBar(
                onBack = onBack,
                title = stringResource(id = R.string.advanced_settings)
            )
            MyColumn {
                SectionCard(
                    title = stringResource(id = R.string.app_management)
                ) {
                    TextActionButton(
                        onClick = { showAppBlacklist = true },
                        text = stringResource(id = R.string.exclude_app),
                        secondaryText = stringResource(id = R.string.exclude_app_hint)
                    )
                    TextActionButton(
                        onClick = { confirmClear = true },
                        text = stringResource(id = R.string.clear_quick_app_stats)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onExcludeFromRecentsChange(it) },
                        checked = uiState.excludeFromRecents,
                        text = stringResource(id = R.string.exclude_from_recents),
                        secondaryText = stringResource(id = R.string.exclude_from_recents_hint)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onActionPanelAppLongPressLaunchPopupChanged(it) },
                        checked = uiState.actionPanelAppLongPressLaunchPopup,
                        text = stringResource(id = R.string.action_panel_launch_app),
                        secondaryText = stringResource(id = R.string.action_panel_launch_app_hint)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onQuickLauncherAppLongPressLaunchPopupChanged(it) },
                        checked = uiState.quickLauncherAppLongPressLaunchPopup,
                        text = stringResource(id = R.string.quick_launcher_launch_app),
                        secondaryText = stringResource(id = R.string.quick_launcher_launch_app_hint)
                    )
                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.gesture_behavior)
                ) {
                    LabeledSwitch(
                        onCheckedChange = { vm.onFitSoftKeyboardChange(it) },
                        checked = uiState.fitSoftKeyboard,
                        text = stringResource(id = R.string.fit_soft_keyboard),
                        secondaryText = stringResource(id = R.string.fit_soft_keyboard_hint)
                    )
                }
                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.hide_gesture_button)
                ) {
                    LabeledSwitch(
                        onCheckedChange = { vm.onHideLandscapeChange(it) },
                        checked = uiState.hideLandscape,
                        text = stringResource(id = R.string.landscape)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onHideScreenLockChange(it) },
                        checked = uiState.hideScreenLock,
                        text = stringResource(id = R.string.lock_screen)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onHideHomeScreenChange(it) },
                        checked = uiState.hideHomeScreen,
                        text = stringResource(id = R.string.launcher)
                    )
                }
            }
        }
        if (confirmClear) {
            MyAlertDialog(
                onDismissRequest = { confirmClear = false },
                onConfirmClick = {
                    confirmClear = false
                    vm.clearQuickAppLauncherStatsConfirmed()
                },
                title = null,
                text = stringResource(id = R.string.clear_quick_app_stats_confirm),
                onCancelClick = { confirmClear = false }
            )
        }

        if (showAppBlacklist) {
            ModalBottomSheet(
                onDismissRequest = { showAppBlacklist = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                BottomSheetNestedContent {
                    AppBlacklistContent(onDismiss = { showAppBlacklist = false })
                }
            }
        }
    }
}
}
