package hunoia.sideleap.ui.screen.advancedsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import hunoia.sideleap.settings.api.SettingsUiDefaults.getDayNightModeText
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVerticalWithSection
import hunoia.sideleap.ui.theme.EdgeMenuPadding
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinItemHeightNoSecondary
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.settings.model.DayNightMode
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

@Composable
fun AdvancedSettingsScreen(
    onBack: () -> Unit,
    onNavToAppBlacklist: () -> Unit,
    onNavToQuickAppHidden: () -> Unit = {},
    onNavToAnimationStyle: () -> Unit,
    vm: AdvancedSettingsVM = viewModel()
) {
    UDFComponent(component = vm.udfComponent, onEvent = {}) { uiState ->
        var confirmClear by remember { mutableStateOf(false) }
        Column {
            TopBar(
                onBack = onBack,
                title = stringResource(id = R.string.advanced_settings)
            )
            MyColumn {
                SectionCard {
                    TextActionButton(
                        onClick = onNavToAppBlacklist,
                        text = stringResource(id = R.string.exclude_app),
                        secondaryText = stringResource(id = R.string.exclude_app_hint)
                    )
                }
                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.gesture_button_extension)
                ) {
                    LabeledSwitch(
                        onTextClick = onNavToAnimationStyle,
                        onCheckedChange = { vm.onShowAnimation(it) },
                        checked = uiState.showAnimation,
                        text = stringResource(id = R.string.animation_style)
                    )
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
                    title = stringResource(id = R.string.app_settings)
                ) {
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
                    TextActionButton(onClick = onNavToQuickAppHidden, text = stringResource(id = R.string.manage_hidden_apps))
                    TextActionButton(onClick = { confirmClear = true }, text = stringResource(id = R.string.clear_quick_app_stats))
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
                    if (uiState.showDynamicColorOption) {
                        LabeledSwitch(
                            onCheckedChange = { vm.onDynamicColorChange(it) },
                            checked = uiState.dynamicColor,
                            text = stringResource(id = R.string.dynamic_color),
                            secondaryText = stringResource(id = R.string.dynamic_color_hint)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = MinItemHeightNoSecondary)
                            .onSingleClick {
                                vm.showDayNightModeDropdownMenu(true)
                            }
                            .padding(
                                horizontal = ContentPaddingHorizontal,
                                vertical = ContentPaddingVerticalWithSection
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ItemPadding)
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(id = R.string.day_night_mode),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Box {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = getDayNightModeText(uiState.dayNightMode),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
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
                                            text = {
                                                Text(text = text)
                                            }
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
    }
}
