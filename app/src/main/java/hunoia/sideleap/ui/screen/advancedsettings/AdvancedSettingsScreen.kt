package hunoia.sideleap.ui.screen.advancedsettings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import hunoia.sideleap.R
import hunoia.sideleap.settings.api.SettingsUiDefaults.getDayNightModeText
import hunoia.sideleap.ui.theme.EdgeMenuPadding
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.settings.model.DayNightMode
import hunoia.sideleap.ui.screen.animationstyle.wave.WaveStyleContent
import hunoia.sideleap.ui.screen.appblacklist.AppBlacklistContent
import hunoia.sideleap.ui.screen.quickapplaunchermanage.QuickAppLauncherManageContent
import hunoia.sideleap.ui.widget.MyColumn
import hunoia.sideleap.ui.widget.MyAlertDialog
import hunoia.sideleap.ui.widget.SectionCard
import hunoia.sideleap.ui.widget.TextActionButton
import hunoia.sideleap.ui.widget.LabeledSwitch
import hunoia.sideleap.ui.widget.TopBar

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
    var showQuickAppHidden by remember { mutableStateOf(false) }
    var showAnimationStyle by remember { mutableStateOf(false) }
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
                        onClick = { showQuickAppHidden = true },
                        text = stringResource(id = R.string.manage_hidden_apps)
                    )
                    TextActionButton(
                        onClick = { confirmClear = true },
                        text = stringResource(id = R.string.clear_quick_app_stats)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onShowSystemAppsChange(it) },
                        checked = uiState.showSystemApps,
                        text = stringResource(id = R.string.show_system_apps)
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
                }
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
                    LabeledSwitch(
                        onCheckedChange = { vm.onHideTemporaryChange(it) },
                        checked = uiState.hideTemporary,
                        text = stringResource(id = R.string.click_to_hide_button_temporary)
                    )
                }
                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.display)
                ) {
                    LabeledSwitch(
                        onTextClick = { showAnimationStyle = true },
                        onCheckedChange = { vm.onShowAnimation(it) },
                        checked = uiState.showAnimation,
                        text = stringResource(id = R.string.animation_style)
                    )
                    if (uiState.showDynamicColorOption) {
                        LabeledSwitch(
                            onCheckedChange = { vm.onDynamicColorChange(it) },
                            checked = uiState.dynamicColor,
                            text = stringResource(id = R.string.dynamic_color),
                            secondaryText = stringResource(id = R.string.dynamic_color_hint)
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        TextActionButton(
                            onClick = { vm.showDayNightModeDropdownMenu(true) },
                            text = stringResource(id = R.string.day_night_mode),
                            secondaryText = getDayNightModeText(uiState.dayNightMode),
                            secondaryTextColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Box(Modifier.size(1.dp)) {
                            DropdownMenu(
                                containerColor = MaterialTheme.colorScheme.surface,
                                offset = DpOffset(x = -EdgeMenuPadding, y = 0.dp),
                                shape = MaterialTheme.shapes.medium,
                                expanded = uiState.showDayNightModeDropdownMenu,
                                onDismissRequest = { vm.showDayNightModeDropdownMenu(false) }
                            ) {
                                listOf(
                                    DayNightMode.Auto to getDayNightModeText(DayNightMode.Auto),
                                    DayNightMode.Day to getDayNightModeText(DayNightMode.Day),
                                    DayNightMode.Night to getDayNightModeText(DayNightMode.Night),
                                ).fastForEach { (effectValue, text) ->
                                    key(effectValue) {
                                        DropdownMenuItem(
                                            onClick = {
                                                vm.onDayNightModeChange(effectValue)
                                                vm.showDayNightModeDropdownMenu(false)
                                            },
                                            text = { Text(text = text) }
                                        )
                                    }
                                }
                            }
                        }
                    }
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
                AppBlacklistContent(onDismiss = { showAppBlacklist = false })
            }
        }
        if (showQuickAppHidden) {
            ModalBottomSheet(
                onDismissRequest = { showQuickAppHidden = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                QuickAppLauncherManageContent(onDismiss = { showQuickAppHidden = false })
            }
        }
        if (showAnimationStyle) {
            ModalBottomSheet(
                onDismissRequest = { showAnimationStyle = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                WaveStyleContent(onDismiss = { showAnimationStyle = false })
            }
        }
    }
}
