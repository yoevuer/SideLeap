package hunoia.sideleap.ui.screen.home

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.onSingleClick
import hunoia.sideleap.R
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.GestureButtonColorAlpha
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.gesture.bounds
import hunoia.sideleap.ui.gesture.actionTextCompose
import hunoia.sideleap.ui.gesture.buttonTextCompose
import hunoia.sideleap.system.intent.gotoAccessibilitySettings
import hunoia.sideleap.system.intent.gotoIgnoreBatteryOptimizations
import hunoia.sideleap.ui.screen.home.HomeVM.UiEvent
import hunoia.sideleap.ui.screen.freeze.FrozenAppManageContent
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVerticalWithSection
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinItemHeightNoSecondary
import hunoia.sideleap.ui.theme.RootPadding
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.ui.theme.SectionPaddingNoTitle
import hunoia.sideleap.ui.theme.TopBarPaddingExtra
import hunoia.sideleap.ui.component.BottomSheetNestedContent
import hunoia.sideleap.ui.component.MyAlertDialog
import hunoia.sideleap.ui.component.MyColumn
import hunoia.sideleap.ui.component.MyExpandableColumn
import hunoia.sideleap.ui.component.SectionCard
import hunoia.sideleap.ui.component.TextActionButton
import hunoia.sideleap.ui.component.LabeledSwitch
import hunoia.sideleap.ui.component.MyTextSlider
import hunoia.sideleap.ui.component.TopBar
import hunoia.sideleap.settings.model.DayNightMode
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.system.intent.KeepAliveHelper
import com.blankj.utilcode.util.TimeUtils
import kotlin.math.roundToInt
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.getDayNightModeText
import hunoia.sideleap.gesture.SubGestureDirection
import hunoia.sideleap.settings.model.GestureSettings.VirtualMouseTrailStyle
import hunoia.sideleap.ui.screen.settings.gesture.WaveStyleContent

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/22
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavToUnlock: () -> Unit,
    onNavToAdvancedSettings: () -> Unit,
    onNavToGestureSettings: () -> Unit,
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
                    val date = TimeUtils.millis2String(timestamp, "yyyyMMdd_HHmmss")
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
            if (showVirtualMouseSettings) {
                ModalBottomSheet(
                    onDismissRequest = { showVirtualMouseSettings = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    BottomSheetNestedContent {
                        MyColumn(scrollState = rememberScrollState()) {
                            VirtualMouseSettingsContent(
                                virtualMouse = uiState.virtualMouse,
                                vm = vm,
                            )
                        }
                    }
                }
            }
            if (showDisplaySettings) {
                ModalBottomSheet(
                    onDismissRequest = { showDisplaySettings = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    BottomSheetNestedContent {
                        MyColumn(scrollState = rememberScrollState()) {
                            DisplaySettingsContent(
                                uiState = uiState,
                                vm = vm,
                                showAnimationStyle = { showAnimationStyle = true },
                                showMiniWindowSettings = { showMiniWindowSettings = true }
                            )
                        }
                    }
                }
            }
            if (showAnimationStyle) {
                ModalBottomSheet(
                    onDismissRequest = { showAnimationStyle = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    BottomSheetNestedContent {
                        WaveStyleContent(onDismiss = { showAnimationStyle = false })
                    }
                }
            }
            if (showFrozenManage) {
                ModalBottomSheet(
                    onDismissRequest = { showFrozenManage = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    BottomSheetNestedContent {
                        FrozenAppManageContent(onDismiss = { showFrozenManage = false })
                    }
                }
            }
            if (showMiniWindowSettings) {
                ModalBottomSheet(
                    onDismissRequest = { showMiniWindowSettings = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    BottomSheetNestedContent {
                        MyColumn(scrollState = rememberScrollState()) {
                            MiniWindowSettingsContent(uiState = uiState, vm = vm)
                        }
                    }
                }
            }
            Column {
                TopBar(
                    onBack = { },
                    title = stringResource(id = R.string.home_title),
                    titleStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.W900),
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
                            onClick = onNavToAdvancedSettings,
                            text = stringResource(id = R.string.advanced_settings),
                            secondaryText = stringResource(id = R.string.advanced_settings_hint)
                        )
                        TextActionButton(
                            onClick = onNavToGestureSettings,
                            text = stringResource(id = R.string.gesture_settings),
                            secondaryText = stringResource(id = R.string.gesture_settings_hint)
                        )
                        TextActionButton(
                            onClick = { showDisplaySettings = true },
                            text = stringResource(id = R.string.display),
                            secondaryText = stringResource(id = R.string.display_hint)
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
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = MinItemHeightNoSecondary)
                                    .onSingleClick {
                                        vm.addSideGestureButton()
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
                                drawRect(
                                    color = when (button.isDefault) {
                                        true -> colorScheme.primary.copy(alpha = GestureButtonColorAlpha)
                                        else -> Color(button.color).copy(alpha = GestureButtonColorAlpha)
                                    },
                                    topLeft = bounds.topLeft,
                                    size = bounds.size
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
private fun VirtualMouseSettingsContent(
    virtualMouse: GestureSettings.VirtualMouse,
    vm: HomeVM,
) {
    var showTrailStyleDropdown by remember { mutableStateOf(false) }
    Column {
        LabeledSwitch(
            onCheckedChange = { vm.onVirtualMouseContinuousModeChange(it) },
            checked = virtualMouse.continuousMode,
            text = stringResource(id = R.string.virtual_mouse_continuous_mode),
            secondaryText = stringResource(id = R.string.virtual_mouse_continuous_mode_hint)
        )
        MyTextSlider(
            value = virtualMouse.continuousModeTimeoutMs / 1000f,
            onValueChange = { vm.onVirtualMouseContinuousModeTimeoutChange((it * 1000).toLong()) },
            text = "连续模式超时",
            valueDisplay = "${virtualMouse.continuousModeTimeoutMs / 1000}秒",
            valueRange = 1f..10f,
        )
        MyTextSlider(
            value = virtualMouse.sensitivityX,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(sensitivityX = it)) },
            onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = "X 灵敏度",
            valueDisplay = String.format("%.1fx", virtualMouse.sensitivityX),
            valueRange = 0.5f..4f,
        )
        MyTextSlider(
            value = virtualMouse.sensitivityY,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(sensitivityY = it)) },
            onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = "Y 灵敏度",
            valueDisplay = String.format("%.1fx", virtualMouse.sensitivityY),
            valueRange = 0.5f..4f,
        )
        MyTextSlider(
            value = virtualMouse.acceleration,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(acceleration = it)) },
            onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = "加速曲线",
            valueDisplay = String.format("%.1f", virtualMouse.acceleration),
            valueRange = 0f..2f,
        )
        MyTextSlider(
            value = virtualMouse.cursorSizeDp.toFloat(),
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(cursorSizeDp = it.toInt())) },
            onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = "光标大小",
            valueDisplay = "${virtualMouse.cursorSizeDp}dp",
            valueRange = 12f..64f,
        )
        MyTextSlider(
            value = virtualMouse.cursorAlpha,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(cursorAlpha = it)) },
            onValueChangeFinished = { vm.saveVirtualMouseSettings() },
            text = "光标透明度",
            valueDisplay = "${(virtualMouse.cursorAlpha * 100).toInt()}%",
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
                text = "拖尾强度",
                valueDisplay = String.format("%.1f", virtualMouse.trailStrength),
                valueRange = 0.5f..2f,
            )
            MyTextSlider(
                value = virtualMouse.trailAlpha,
                onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(trailAlpha = it)) },
                onValueChangeFinished = { vm.saveVirtualMouseSettings() },
                text = "拖尾透明度",
                valueDisplay = "${(virtualMouse.trailAlpha * 100).toInt()}%",
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
            MyTextSlider(
                value = virtualMouse.longPressDelayMs.toFloat(),
                onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(longPressDelayMs = it.toLong())) },
                onValueChangeFinished = { vm.saveVirtualMouseSettings() },
                text = "长按延迟",
                valueDisplay = "${virtualMouse.longPressDelayMs}ms",
                valueRange = 400f..2000f,
            )
            MyTextSlider(
                value = virtualMouse.longPressMoveToleranceDp.toFloat(),
                onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(longPressMoveToleranceDp = it.toInt())) },
                onValueChangeFinished = { vm.saveVirtualMouseSettings() },
                text = "停留容差",
                valueDisplay = "${virtualMouse.longPressMoveToleranceDp}dp",
                valueRange = 2f..16f,
            )
        }
    }
}

private fun virtualMouseTrailStyleText(style: VirtualMouseTrailStyle): String {
    return when (style) {
        VirtualMouseTrailStyle.None -> "关闭"
        VirtualMouseTrailStyle.Dots -> "残影点"
        VirtualMouseTrailStyle.LightBand -> "光带"
    }
}

@Composable
private fun DisplaySettingsContent(
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
private fun MiniWindowSettingsContent(uiState: HomeVM.UiState, vm: HomeVM) {
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
                SectionCard {
                    TextActionButton(
                        onClick = onBackupRequest,
                        text = stringResource(id = R.string.backup),
                        secondaryText = stringResource(id = R.string.backup_hint)
                    )
                }
                SectionCard(modifier = Modifier.padding(top = SectionPaddingNoTitle)) {
                    TextActionButton(
                        onClick = onRestoreRequest,
                        text = stringResource(id = R.string.restore),
                        secondaryText = stringResource(id = R.string.restore_hint)
                    )
                }
            }
        }
    }
}
