package hunoia.luno.ui.screen.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import hunoia.luno.ui.screen.home.sheet.AdvancedSettingsSheet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.onSingleClick
import hunoia.luno.R
import hunoia.luno.settings.defaults.SettingsUiDefaults.GestureButtonColorAlpha
import hunoia.luno.gesture.GestureButton
import hunoia.luno.gesture.bounds
import hunoia.luno.ui.gesture.actionTextCompose
import hunoia.luno.ui.gesture.buttonTextCompose
import hunoia.luno.system.intent.gotoAccessibilitySettings
import hunoia.luno.system.intent.gotoIgnoreBatteryOptimizations
import hunoia.luno.ui.screen.home.HomeVM.UiEvent
import hunoia.luno.ui.screen.freeze.FrozenAppManageContent
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVerticalWithSection
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.MinItemHeightNoSecondary
import hunoia.luno.ui.theme.RootPadding
import hunoia.luno.ui.theme.SectionPadding
import hunoia.luno.ui.theme.SectionPaddingNoTitle
import hunoia.luno.ui.theme.TopBarPaddingExtra
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.component.OptimizedScrollState
import hunoia.luno.ui.component.MyAlertDialog
import hunoia.luno.ui.component.MyColumn
import hunoia.luno.ui.component.MyExpandableColumn
import hunoia.luno.ui.component.SectionCard
import hunoia.luno.ui.component.TextActionButton
import hunoia.luno.ui.component.LabeledSwitch
import hunoia.luno.ui.component.MyTextSlider
import hunoia.luno.ui.component.TopBar
import hunoia.luno.settings.model.DayNightMode
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.system.intent.KeepAliveHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import hunoia.luno.settings.defaults.SettingsUiDefaults.getDayNightModeText
import hunoia.luno.gesture.SubGestureDirection
import hunoia.luno.settings.model.GestureSettings.VirtualMouseTrailStyle
import hunoia.luno.ui.screen.freeze.AppBlacklistContent
import hunoia.luno.ui.screen.home.AnimationStyleContent
import hunoia.luno.ui.screen.home.sheet.AnimationStyleSheet
import hunoia.luno.ui.screen.home.sheet.DisplaySettingsSheet
import hunoia.luno.ui.screen.home.sheet.FrozenAppManageSheet
import hunoia.luno.ui.screen.home.sheet.AdvancedSettingsSheet
import hunoia.luno.ui.screen.home.sheet.MiniWindowSettingsSheet
import hunoia.luno.ui.screen.home.sheet.VirtualMouseSettingsSheet

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/22
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavToGestureButtonSettings: (GestureButton) -> Unit,
    onNavToSubGestureEditor: (String) -> Unit,
    vm: HomeVM = viewModel()
) {
        val scrollState = rememberScrollState()
        var showVirtualMouseSettings by remember { mutableStateOf(false) }
        var showFrozenManage by remember { mutableStateOf(false) }
        var showDisplaySettings by remember { mutableStateOf(false) }
        var showAnimationStyle by remember { mutableStateOf(false) }
        var showMiniWindowSettings by remember { mutableStateOf(false) }
        var showAdvancedSettings by remember { mutableStateOf(false) }
        var showAppBlacklist by remember { mutableStateOf(false) }
    val context = LocalContext.current
    UDFComponent(
        component = vm.udfComponent,
        onEvent = { event ->
            when (event) {
                is UiEvent.ScrollToBottom -> {
                    scrollState.animateScrollTo(
                        value = scrollState.maxValue,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                }
                is UiEvent.ScrollToEvent -> {
                    scrollState.animateScrollTo(
                        value = event.offsetY,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                }

            }
        }
    ) { uiState ->
        if (uiState.showResetWarningDialog) {
            MyAlertDialog(
                onDismissRequest = { vm.showResetWarningDialog(false) },
                onConfirmClick = { vm.reset() },
                title = stringResource(id = R.string.reset_default_settings_warning),
                text = stringResource(id = R.string.reset_default_settings_warning_desc)
            )
        }

        val createFileLauncher = rememberLauncherForActivityResult(
            contract = CreateDocument("*/*")
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            vm.backup(context, uri)
        }
        val getFileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            vm.restore(context, uri)
        }
        if (uiState.showBackupRestoreDialog) {
            BackupRestoreDialog(
                onDismissRequest = {
                    vm.showBackupRestoreDialog(false)
                },
                onBackupRequest = {
                    vm.showBackupRestoreDialog(false)
                    val appName = context.getString(context.applicationInfo.labelRes)
                    val timestamp = System.currentTimeMillis()
                    val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(timestamp))
                    val fileName = "${appName}_$date.zip"
                    createFileLauncher.launch(fileName)
                },
                onRestoreRequest = {
                    vm.showBackupRestoreDialog(false)
                    getFileLauncher.launch("*/*")
                }
            )
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(key1 = lifecycleOwner) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                vm.updatePermissionState()
            }
        }

            Box {
                VirtualMouseSettingsSheet(
                    show = showVirtualMouseSettings,
                    onDismiss = { showVirtualMouseSettings = false },
                    virtualMouse = uiState.virtualMouse,
                    vm = vm
                )
                DisplaySettingsSheet(
                    show = showDisplaySettings,
                    onDismiss = { showDisplaySettings = false },
                    uiState = uiState,
                    vm = vm,
                    onShowAnimationStyle = { showAnimationStyle = true },
                    onShowMiniWindowSettings = { showMiniWindowSettings = true }
                )
                AnimationStyleSheet(
                    show = showAnimationStyle,
                    onDismiss = { showAnimationStyle = false }
                )
                FrozenAppManageSheet(
                    show = showFrozenManage,
                    onDismiss = { showFrozenManage = false }
                )
                MiniWindowSettingsSheet(
                    show = showMiniWindowSettings,
                    onDismiss = { showMiniWindowSettings = false },
                    uiState = uiState,
                    vm = vm
                )
                AdvancedSettingsSheet(
                    show = showAdvancedSettings,
                    onDismiss = { showAdvancedSettings = false }
                )
                if (showAppBlacklist) {
                    OptimizedBottomSheet(
                        onDismissRequest = { showAppBlacklist = false }
                    ) {
                        AppBlacklistContent(onDismiss = { showAppBlacklist = false })
                    }
                }
                Column {
                TopBar(
                    onBack = { },
                    title = stringResource(id = R.string.home_title),
                    showBackIcon = false,
                    actions = {
                        IconButton(onClick = { vm.showMoreMenu(true) }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(id = R.string.more)
                            )
                        }

                        DropdownMenu(
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium,
                            offset = DpOffset(-(TopBarPaddingExtra / 2), 0.dp),
                            expanded = uiState.showMoreMenu,
                            onDismissRequest = { vm.showMoreMenu(false) }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    vm.showMoreMenu(false)
                                    vm.showResetWarningDialog(true)
                                },
                                text = {
                                    Text(text = stringResource(id = R.string.reset_default_settings_warning),)
                                }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    vm.showMoreMenu(false) {
                                        vm.showBackupRestoreDialog(true)
                                    }
                                },
                                text = {
                                    Text(text = stringResource(id = R.string.backup_restore))
                                }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    vm.showMoreMenu(false)
                                    context.gotoIgnoreBatteryOptimizations()
                                },
                                text = {
                                    Text(text = stringResource(id = R.string.ignoring_battery_optimizations))
                                }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    vm.showMoreMenu(false)
                                    KeepAliveHelper.gotoSettings(context)
                                },
                                text = {
                                    Text(text = stringResource(id = R.string.launch_self_permission))
                                }
                            )
                        }
                    }
                )

                MyColumn(scrollState = scrollState) {
                    SectionCard(
                        title = stringResource(id = R.string.global_settings)
                    ) {
                        LabeledSwitch(
                            onCheckedChange = {
                                if (it && !uiState.isAccessibilityEnabled) {
                                    context.gotoAccessibilitySettings()
                                } else {
                                    vm.onAppGestureEnabledChange(it)
                                }
                            },
                            checked = uiState.isGestureEnabled,
                            text = stringResource(id = R.string.gesture_switch),
                            secondaryText = stringResource(id = R.string.gesture_switch_hint)
                        )
                        TextActionButton(
                            onClick = { showAdvancedSettings = true },
                            text = stringResource(id = R.string.advanced_settings),
                            secondaryText = stringResource(id = R.string.advanced_settings_hint)
                        )
                        TextActionButton(
                            onClick = { showDisplaySettings = true },
                            text = stringResource(id = R.string.display),
                            secondaryText = stringResource(id = R.string.display_hint)
                        )
                        TextActionButton(
                            onClick = { showAppBlacklist = true },
                            text = stringResource(id = R.string.exclude_app),
                            secondaryText = stringResource(id = R.string.exclude_app_hint)
                        )
                    }

                    SectionCard(
                        modifier = Modifier.padding(top = SectionPadding),
                        title = stringResource(id = R.string.action_settings)
                    ) {
                        TextActionButton(
                            onClick = { showVirtualMouseSettings = true },
                            text = stringResource(id = R.string.virtual_mouse),
                            secondaryText = stringResource(id = R.string.virtual_mouse_hint)
                        )
                        TextActionButton(
                            onClick = { showFrozenManage = true },
                            text = stringResource(id = R.string.frozen_app_manage),
                            secondaryText = stringResource(id = R.string.frozen_app_manage_hint)
                        )
                    }

                    val density = LocalDensity.current
                    var gestureButtonListOffset by remember { mutableIntStateOf(Int.MAX_VALUE) }
                    SectionCard(
                        modifier = Modifier
                            .onGloballyPositioned {
                                density.run {
                                    val position = it.positionInParent()
                                    gestureButtonListOffset =
                                        (position.y + RootPadding.toPx()).toInt()
                                }
                            }
                            .padding(top = SectionPadding),
                        title = stringResource(id = R.string.gesture_button_list)
                    ) {
                        MyExpandableColumn(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.Transparent,
                            shape = RectangleShape,
                            title = stringResource(id = R.string.bottom_gesture_button_list),
                            expanded = uiState.isBottomGestureButtonListExpanded,
                            onExpandedChange = { expanded ->
                                if (expanded) {
                                    vm.expandBottomGestureButtonList(true, gestureButtonListOffset)
                                } else {
                                    vm.expandBottomGestureButtonList(false)
                                }
                            }
                        ) {
                            uiState.bottomGestureButtons.fastForEach { button ->
                                key(button) {
                                    LabeledSwitch(
                                        onTextClick = { onNavToGestureButtonSettings(button) },
                                        onCheckedChange = { vm.onBottomGestureButtonEnabledChange(button, it) },
                                        checked = button.enabled,
                                        text = button.buttonTextCompose(),
                                        secondaryText = run {
                                            val expected = button.actionTextCompose()
                                            if (expected.isNotEmpty()) {
                                                return@run expected
                                            }
                                            stringResource(id = R.string.action_none)
                                        },
                                        secondaryTextColor = MaterialTheme.colorScheme.primary,
                                        markColor = when (button.isDefault) {
                                            true -> MaterialTheme.colorScheme.primary.copy(alpha = GestureButtonColorAlpha)
                                            else -> Color(button.color).copy(alpha = GestureButtonColorAlpha)
                                        }
                                    )
                                }
                            }
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = MinItemHeightNoSecondary)
                                    .onSingleClick {
                                        vm.addBottomGestureButton()
                                    }
                                    .wrapContentSize(),
                                text = stringResource(id = R.string.add_gesture_button),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        MyExpandableColumn(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.Transparent,
                            shape = RectangleShape,
                            title = stringResource(id = R.string.side_gesture_button_list),
                            expanded = uiState.isSideGestureButtonListExpanded,
                            onExpandedChange = { expanded ->
                                if (expanded) {
                                    vm.expandSideGestureButtonList(true, gestureButtonListOffset)
                                } else {
                                    vm.expandSideGestureButtonList(false)
                                }
                            }
                        ) {
                            uiState.sideGestureButtons.fastForEach { button ->
                                key(button) {
                                    LabeledSwitch(
                                        onTextClick = { onNavToGestureButtonSettings(button) },
                                        onCheckedChange = { vm.onSideGestureButtonEnabledChange(button, it) },
                                        checked = button.enabled,
                                        text = button.buttonTextCompose(),
                                        secondaryText = run {
                                            val expected = button.actionTextCompose()
                                            if (expected.isNotEmpty()) {
                                                return@run expected
                                            }
                                            stringResource(id = R.string.action_none)
                                        },
                                        secondaryTextColor = MaterialTheme.colorScheme.primary,
                                        markColor = when (button.isDefault) {
                                            true -> MaterialTheme.colorScheme.primary.copy(alpha = GestureButtonColorAlpha)
                                            else -> Color(button.color).copy(alpha = GestureButtonColorAlpha)
                                        }
                                    )
                                }
                            }
                            androidx.compose.material3.TextButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = MinItemHeightNoSecondary),
                                onClick = { vm.addSideGestureButton() }
                            ) {
                                Text(
                                    text = stringResource(id = R.string.add_gesture_button),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        MyExpandableColumn(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.Transparent,
                            shape = RectangleShape,
                            title = stringResource(id = R.string.sub_gesture_list),
                            expanded = uiState.isSubGestureListExpanded,
                            onExpandedChange = { expanded ->
                                if (expanded) {
                                    vm.expandSubGestureList(true, gestureButtonListOffset)
                                } else {
                                    vm.expandSubGestureList(false)
                                }
                            }
                        ) {
                            uiState.subGestures.fastForEach { gesture ->
                                key(gesture.id) {
                                    LabeledSwitch(
                                        onTextClick = { onNavToSubGestureEditor(gesture.id) },
                                        onCheckedChange = { vm.onSubGestureEnabledChange(gesture, it) },
                                        checked = gesture.enabled,
                                        text = gesture.name,
                                        markColor = Color(gesture.color).copy(alpha = GestureButtonColorAlpha)
                                    )
                                }
                            }
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = MinItemHeightNoSecondary)
                                    .onSingleClick {
                                    val id = java.util.UUID.randomUUID().toString()
                                    vm.addSubGesture(id)
                                }
                                    .wrapContentSize(),
                                text = stringResource(id = R.string.add_sub_gesture),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.isSideGestureButtonListExpanded || uiState.isBottomGestureButtonListExpanded,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
            ) {
                val colorScheme = MaterialTheme.colorScheme
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val buttons = if (uiState.isSideGestureButtonListExpanded) {
                                uiState.sideGestureButtons
                            } else {
                                uiState.bottomGestureButtons
                            }
                            buttons.fastForEach { button ->
                                if (!button.enabled) {
                                    return@fastForEach
                                }
                                val bounds = button.bounds()
                                drawRoundRect(
                                    color = when (button.isDefault) {
                                        true -> colorScheme.primary.copy(alpha = GestureButtonColorAlpha)
                                        else -> Color(button.color).copy(alpha = GestureButtonColorAlpha)
                                    },
                                    topLeft = bounds.topLeft,
                                    size = bounds.size,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                                drawRoundRect(
                                    color = colorScheme.outlineVariant,
                                    topLeft = bounds.topLeft,
                                    size = bounds.size,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                )
                            }
                        }
                )
            }
        }
    }
}

private val VirtualMouseTrailStyleOptions = listOf(
    VirtualMouseTrailStyle.None,
    VirtualMouseTrailStyle.Dots,
    VirtualMouseTrailStyle.LightBand,
)

@Composable
internal fun VirtualMouseSettingsContent(
    virtualMouse: GestureSettings.VirtualMouse,
    vm: HomeVM,
    scrollState: androidx.compose.foundation.ScrollState? = null,
) {
    var showTrailStyleDropdown by remember { mutableStateOf(false) }
    MyColumn(scrollState = scrollState ?: rememberScrollState()) {
        LabeledSwitch(
            onCheckedChange = { vm.onVirtualMouseContinuousModeChange(it) },
            checked = virtualMouse.continuousMode,
            text = stringResource(id = R.string.virtual_mouse_continuous_mode),
            secondaryText = stringResource(id = R.string.virtual_mouse_continuous_mode_hint)
        )
        val currentVirtualMouse by rememberUpdatedState(virtualMouse)
        MyTextSlider(
            value = virtualMouse.continuousModeTimeoutMs / 1000f,
            onValueChange = { vm.onVirtualMouseContinuousModeTimeoutChange((it * 1000).toLong()) },
            onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = stringResource(id = R.string.virtual_mouse_continuous_timeout_plain),
            valueDisplay = stringResource(id = R.string.virtual_mouse_continuous_timeout, virtualMouse.continuousModeTimeoutMs / 1000),
            valueRange = 1f..10f,
        )
        MyTextSlider(
            value = virtualMouse.sensitivityX,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(sensitivityX = it)) },
            onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = stringResource(id = R.string.virtual_mouse_sensitivity_x_plain),
            valueDisplay = stringResource(id = R.string.virtual_mouse_sensitivity_x, virtualMouse.sensitivityX),
            valueRange = 0.5f..4f,
        )
        MyTextSlider(
            value = virtualMouse.sensitivityY,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(sensitivityY = it)) },
            onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = stringResource(id = R.string.virtual_mouse_sensitivity_y_plain),
            valueDisplay = stringResource(id = R.string.virtual_mouse_sensitivity_y, virtualMouse.sensitivityY),
            valueRange = 0.5f..4f,
        )
        MyTextSlider(
            value = virtualMouse.acceleration,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(acceleration = it)) },
            onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = stringResource(id = R.string.virtual_mouse_acceleration_plain),
            valueDisplay = stringResource(id = R.string.virtual_mouse_acceleration, virtualMouse.acceleration),
            valueRange = 0f..2f,
        )
        var localCursorSize by remember(virtualMouse.cursorSizeDp) { mutableStateOf(virtualMouse.cursorSizeDp.toFloat()) }
        MyTextSlider(
            value = localCursorSize,
            onValueChange = { localCursorSize = it },
            onValueChangeFinished = {
                vm.onVirtualMouseChange(currentVirtualMouse.copy(cursorSizeDp = localCursorSize.toInt()))
                vm.saveVirtualMouseSettings()
            },
            text = stringResource(id = R.string.virtual_mouse_cursor_size_plain),
            valueDisplay = stringResource(id = R.string.virtual_mouse_cursor_size, localCursorSize.toInt()),
            valueRange = 12f..64f,
        )
        MyTextSlider(
            value = virtualMouse.cursorAlpha,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(cursorAlpha = it)) },
            onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = stringResource(id = R.string.virtual_mouse_cursor_alpha_plain),
            valueDisplay = stringResource(id = R.string.virtual_mouse_cursor_alpha, (virtualMouse.cursorAlpha * 100).toInt()),
            valueRange = 0.2f..1f,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinItemHeightNoSecondary)
                .onSingleClick { showTrailStyleDropdown = true }
                .padding(horizontal = ContentPaddingHorizontal, vertical = ContentPaddingVerticalWithSection),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItemPadding)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.virtual_mouse_trail),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = virtualMouseTrailStyleText(virtualMouse.trailStyle),
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
                    shape = MaterialTheme.shapes.medium,
                    expanded = showTrailStyleDropdown,
                    onDismissRequest = { showTrailStyleDropdown = false }
                ) {
                    VirtualMouseTrailStyleOptions.fastForEach { style ->
                        DropdownMenuItem(
                            onClick = {
                                vm.onVirtualMouseTrailStyleChange(style)
                                showTrailStyleDropdown = false
                            },
                            text = { Text(virtualMouseTrailStyleText(style)) }
                        )
                    }
                }
            }
        }
        if (virtualMouse.trailStyle != VirtualMouseTrailStyle.None) {
            MyTextSlider(
                value = virtualMouse.trailStrength,
                onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(trailStrength = it)) },
                onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = stringResource(id = R.string.virtual_mouse_trail_strength_plain),
            valueDisplay = stringResource(id = R.string.virtual_mouse_trail_strength, virtualMouse.trailStrength),
                valueRange = 0.5f..2f,
            )
            MyTextSlider(
                value = virtualMouse.trailAlpha,
                onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(trailAlpha = it)) },
                onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = stringResource(id = R.string.virtual_mouse_trail_alpha_plain),
            valueDisplay = stringResource(id = R.string.virtual_mouse_trail_alpha, (virtualMouse.trailAlpha * 100).toInt()),
                valueRange = 0.2f..1f,
            )
        }
        LabeledSwitch(
            onCheckedChange = { vm.onVirtualMouseClickAnimationChange(it) },
            checked = virtualMouse.clickAnimationEnabled,
            text = stringResource(id = R.string.virtual_mouse_click_animation)
        )
        LabeledSwitch(
            onCheckedChange = { vm.onVirtualMouseLongPressEnabledChange(it) },
            checked = virtualMouse.longPressEnabled,
            text = stringResource(id = R.string.virtual_mouse_long_press),
            secondaryText = stringResource(id = R.string.virtual_mouse_long_press_hint)
        )
        if (virtualMouse.longPressEnabled) {
            var localLongPressDelay by remember(virtualMouse.longPressDelayMs) { mutableStateOf(virtualMouse.longPressDelayMs.toFloat()) }
            MyTextSlider(
                value = localLongPressDelay,
                onValueChange = { localLongPressDelay = it },
                onValueChangeFinished = {
                    vm.onVirtualMouseChange(currentVirtualMouse.copy(longPressDelayMs = localLongPressDelay.toLong()))
                    vm.saveVirtualMouseSettings()
                },
            text = stringResource(id = R.string.virtual_mouse_long_press_delay_plain),
            valueDisplay = stringResource(id = R.string.virtual_mouse_long_press_delay, localLongPressDelay.toLong()),
                valueRange = 400f..2000f,
            )
            var localTolerance by remember(virtualMouse.longPressMoveToleranceDp) { mutableStateOf(virtualMouse.longPressMoveToleranceDp.toFloat()) }
            MyTextSlider(
                value = localTolerance,
                onValueChange = { localTolerance = it },
                onValueChangeFinished = {
                    vm.onVirtualMouseChange(currentVirtualMouse.copy(longPressMoveToleranceDp = localTolerance.toInt()))
                    vm.saveVirtualMouseSettings()
                },
            text = stringResource(id = R.string.virtual_mouse_long_press_tolerance_plain),
            valueDisplay = stringResource(id = R.string.virtual_mouse_long_press_tolerance, localTolerance.toInt()),
                valueRange = 2f..16f,
            )
        }
    }
}

@Composable
internal fun virtualMouseTrailStyleText(style: VirtualMouseTrailStyle): String {
    return when (style) {
        VirtualMouseTrailStyle.None -> stringResource(id = R.string.virtual_mouse_trail_style_close)
        VirtualMouseTrailStyle.Dots -> stringResource(id = R.string.virtual_mouse_trail_style_dot)
        VirtualMouseTrailStyle.LightBand -> stringResource(id = R.string.virtual_mouse_trail_style_band)
    }
}

@Composable
internal fun DisplaySettingsContent(
    uiState: HomeVM.UiState,
    vm: HomeVM,
    showAnimationStyle: () -> Unit,
    showMiniWindowSettings: () -> Unit,
) {
    Column {
        LabeledSwitch(
            onTextClick = showAnimationStyle,
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
        TextActionButton(
            onClick = { showMiniWindowSettings() },
            text = stringResource(id = R.string.mini_window_position),
            secondaryText = stringResource(id = R.string.mini_window_position_hint)
        )
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
                    offset = DpOffset(x = 0.dp, y = 0.dp),
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

@Composable
internal fun MiniWindowSettingsContent(uiState: HomeVM.UiState, vm: HomeVM) {
    Column {
        MyTextSlider(
            value = uiState.miniWindowHorizontalBias,
            onValueChange = { vm.onMiniWindowHorizontalBiasChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = stringResource(id = R.string.mini_window_horizontal_position),
            valueDisplay = "${(uiState.miniWindowHorizontalBias * 100).roundToInt()}%",
        )
        MyTextSlider(
            value = uiState.miniWindowVerticalBias,
            onValueChange = { vm.onMiniWindowVerticalBiasChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = stringResource(id = R.string.mini_window_vertical_position),
            valueDisplay = "${(uiState.miniWindowVerticalBias * 100).roundToInt()}%",
        )
        MyTextSlider(
            value = uiState.miniWindowVerticalEdgeMarginFraction,
            onValueChange = { vm.onMiniWindowVerticalEdgeMarginChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = stringResource(id = R.string.mini_window_vertical_edge_margin),
            valueDisplay = "${(uiState.miniWindowVerticalEdgeMarginFraction * 100).roundToInt()}%",
            valueRange = 0f..0.2f,
        )
        MyTextSlider(
            value = uiState.miniWindowVerticalOffsetFraction,
            onValueChange = { vm.onMiniWindowVerticalOffsetChange(it) },
            onValueChangeFinished = { vm.saveDisplaySettings() },
            text = stringResource(id = R.string.mini_window_vertical_offset),
            valueDisplay = "${(uiState.miniWindowVerticalOffsetFraction * 100).roundToInt()}%",
            valueRange = -0.3f..0.3f,
        )
    }
}

@Composable
private fun BackupRestoreDialog(
    onDismissRequest: () -> Unit,
    onBackupRequest: () -> Unit,
    onRestoreRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AlertDialogDefaults.shape,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                TextActionButton(
                    onClick = onBackupRequest,
                    text = stringResource(id = R.string.backup),
                    secondaryText = stringResource(id = R.string.backup_hint)
                )
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                TextActionButton(
                    onClick = onRestoreRequest,
                    text = stringResource(id = R.string.restore),
                    secondaryText = stringResource(id = R.string.restore_hint)
                )
            }
        }
    }
}
