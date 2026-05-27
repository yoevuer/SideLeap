package hunoia.luno.ui.screen.home
import hunoia.luno.ui.theme.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import hunoia.luno.ui.component.ExpandChip
import hunoia.luno.ui.component.SectionCard
import hunoia.luno.ui.component.TextActionButton
import hunoia.luno.ui.component.LabeledSwitch
import hunoia.luno.ui.component.MyTextSlider
import hunoia.luno.ui.component.TopBar
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.system.intent.KeepAliveHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import hunoia.luno.gesture.SubGestureDirection
import hunoia.luno.settings.model.GestureSettings.PointerTrailStyle
import hunoia.luno.ui.screen.freeze.AppBlacklistContent
import hunoia.luno.ui.screen.home.sheet.AnimationStyleSheet
import hunoia.luno.ui.screen.home.sheet.FrozenAppManageSheet
import hunoia.luno.ui.screen.home.sheet.MiniWindowSettingsSheet
import hunoia.luno.ui.screen.home.sheet.PointerSettingsSheet

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
        var showPointerSettings by remember { mutableStateOf(false) }
        var showFrozenManage by remember { mutableStateOf(false) }
        var showAnimationStyle by remember { mutableStateOf(false) }
        var showMiniWindowSettings by remember { mutableStateOf(false) }
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
                PointerSettingsSheet(
                    show = showPointerSettings,
                    onDismiss = { showPointerSettings = false },
                    pointer = uiState.pointer,
                    vm = vm
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
                        LabeledSwitch(
                            onTextClick = { showAnimationStyle = true },
                            onCheckedChange = { vm.onShowAnimation(it) },
                            checked = uiState.showAnimation,
                            text = stringResource(id = R.string.animation_style),
                            secondaryText = stringResource(id = R.string.animation_style_hint)
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
                            onClick = { showPointerSettings = true },
                            text = stringResource(id = R.string.pointer),
                            secondaryText = stringResource(id = R.string.pointer_hint)
                        )
                        TextActionButton(
                            onClick = { showFrozenManage = true },
                            text = stringResource(id = R.string.frozen_app_manage),
                            secondaryText = stringResource(id = R.string.frozen_app_manage_hint)
                        )
                        TextActionButton(
                            onClick = { showMiniWindowSettings = true },
                            text = stringResource(id = R.string.mini_window_position),
                            secondaryText = stringResource(id = R.string.mini_window_position_hint)
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
                        Row(
                            modifier = Modifier.padding(
                                horizontal = ContentPaddingHorizontal,
                                vertical = ContentPaddingVerticalWithSection
                            ),
                            horizontalArrangement = Arrangement.spacedBy(Spacing6)
                        ) {
                            ExpandChip(
                                modifier = Modifier.weight(1f),
                                selected = uiState.isBottomGestureButtonListExpanded,
                                label = stringResource(id = R.string.bottom_gesture_button_list_short),
                                onClick = {
                                    if (uiState.isBottomGestureButtonListExpanded) {
                                        vm.expandBottomGestureButtonList(false)
                                    } else {
                                        vm.expandBottomGestureButtonList(true, gestureButtonListOffset)
                                    }
                                }
                            )
                            ExpandChip(
                                modifier = Modifier.weight(1f),
                                selected = uiState.isSideGestureButtonListExpanded,
                                label = stringResource(id = R.string.side_gesture_button_list_short),
                                onClick = {
                                    if (uiState.isSideGestureButtonListExpanded) {
                                        vm.expandSideGestureButtonList(false)
                                    } else {
                                        vm.expandSideGestureButtonList(true, gestureButtonListOffset)
                                    }
                                }
                            )
                            ExpandChip(
                                modifier = Modifier.weight(1f),
                                selected = uiState.isSubGestureListExpanded,
                                label = stringResource(id = R.string.sub_gesture_list),
                                onClick = {
                                    if (uiState.isSubGestureListExpanded) {
                                        vm.expandSubGestureList(false)
                                    } else {
                                        vm.expandSubGestureList(true, gestureButtonListOffset)
                                    }
                                }
                            )
                        }

                        AnimatedVisibility(
                            modifier = Modifier.fillMaxWidth(),
                            visible = uiState.isBottomGestureButtonListExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column {
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
                                        .onSingleClick { vm.addBottomGestureButton() }
                                        .wrapContentSize(),
                                    text = stringResource(id = R.string.add_gesture_button),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        AnimatedVisibility(
                            modifier = Modifier.fillMaxWidth(),
                            visible = uiState.isSideGestureButtonListExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column {
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
                        }

                        AnimatedVisibility(
                            modifier = Modifier.fillMaxWidth(),
                            visible = uiState.isSubGestureListExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column {
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
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(Spacing4.toPx(), Spacing4.toPx())
                                )
                                drawRoundRect(
                                    color = colorScheme.outlineVariant,
                                    topLeft = bounds.topLeft,
                                    size = bounds.size,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(Spacing4.toPx(), Spacing4.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = Spacing1.toPx())
                                )
                            }
                        }
                )
            }
        }
    }
}

private val PointerTrailStyleOptions = listOf(
    PointerTrailStyle.None,
    PointerTrailStyle.Dots,
    PointerTrailStyle.LightBand,
)

@Composable
internal fun PointerSettingsContent(
    pointer: GestureSettings.Pointer,
    vm: HomeVM,
    scrollState: androidx.compose.foundation.ScrollState? = null,
) {
    var showTrailStyleDropdown by remember { mutableStateOf(false) }
    MyColumn(scrollState = scrollState ?: rememberScrollState()) {
        LabeledSwitch(
            onCheckedChange = { vm.onPointerContinuousModeChange(it) },
            checked = pointer.continuousMode,
            text = stringResource(id = R.string.pointer_continuous_mode),
            secondaryText = stringResource(id = R.string.pointer_continuous_mode_hint)
        )
        val currentPointer by rememberUpdatedState(pointer)
        MyTextSlider(
            value = pointer.continuousModeTimeoutMs / 1000f,
            onValueChange = { vm.onPointerContinuousModeTimeoutChange((it * 1000).toLong()) },
            onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_continuous_timeout_plain),
            valueDisplay = stringResource(id = R.string.pointer_continuous_timeout, pointer.continuousModeTimeoutMs / 1000),
            valueRange = 1f..10f,
        )
        MyTextSlider(
            value = pointer.sensitivityX,
            onValueChange = { vm.onPointerChange(pointer.copy(sensitivityX = it)) },
            onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_sensitivity_x_plain),
            valueDisplay = stringResource(id = R.string.pointer_sensitivity_x, pointer.sensitivityX),
            valueRange = 0.5f..4f,
        )
        MyTextSlider(
            value = pointer.sensitivityY,
            onValueChange = { vm.onPointerChange(pointer.copy(sensitivityY = it)) },
            onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_sensitivity_y_plain),
            valueDisplay = stringResource(id = R.string.pointer_sensitivity_y, pointer.sensitivityY),
            valueRange = 0.5f..4f,
        )
        MyTextSlider(
            value = pointer.acceleration,
            onValueChange = { vm.onPointerChange(pointer.copy(acceleration = it)) },
            onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_acceleration_plain),
            valueDisplay = stringResource(id = R.string.pointer_acceleration, pointer.acceleration),
            valueRange = 0f..2f,
        )
        var localCursorSize by remember(pointer.cursorSizeDp) { mutableStateOf(pointer.cursorSizeDp.toFloat()) }
        MyTextSlider(
            value = localCursorSize,
            onValueChange = { localCursorSize = it },
            onValueChangeFinished = {
                vm.onPointerChange(currentPointer.copy(cursorSizeDp = localCursorSize.toInt()))
                vm.savePointerSettings()
            },
            text = stringResource(id = R.string.pointer_cursor_size_plain),
            valueDisplay = stringResource(id = R.string.pointer_cursor_size, localCursorSize.toInt()),
            valueRange = 12f..64f,
        )
        MyTextSlider(
            value = pointer.cursorAlpha,
            onValueChange = { vm.onPointerChange(pointer.copy(cursorAlpha = it)) },
            onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_cursor_alpha_plain),
            valueDisplay = stringResource(id = R.string.pointer_cursor_alpha, (pointer.cursorAlpha * 100).toInt()),
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
                text = stringResource(id = R.string.pointer_trail),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pointerTrailStyleText(pointer.trailStyle),
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
                    PointerTrailStyleOptions.fastForEach { style ->
                        DropdownMenuItem(
                            onClick = {
                                vm.onPointerTrailStyleChange(style)
                                showTrailStyleDropdown = false
                            },
                            text = { Text(pointerTrailStyleText(style)) }
                        )
                    }
                }
            }
        }
        if (pointer.trailStyle != PointerTrailStyle.None) {
            MyTextSlider(
                value = pointer.trailStrength,
                onValueChange = { vm.onPointerChange(pointer.copy(trailStrength = it)) },
                onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_trail_strength_plain),
            valueDisplay = stringResource(id = R.string.pointer_trail_strength, pointer.trailStrength),
                valueRange = 0.5f..2f,
            )
            MyTextSlider(
                value = pointer.trailAlpha,
                onValueChange = { vm.onPointerChange(pointer.copy(trailAlpha = it)) },
                onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_trail_alpha_plain),
            valueDisplay = stringResource(id = R.string.pointer_trail_alpha, (pointer.trailAlpha * 100).toInt()),
                valueRange = 0.2f..1f,
            )
        }
        LabeledSwitch(
            onCheckedChange = { vm.onPointerClickAnimationChange(it) },
            checked = pointer.clickAnimationEnabled,
            text = stringResource(id = R.string.pointer_click_animation)
        )
        LabeledSwitch(
            onCheckedChange = { vm.onPointerLongPressEnabledChange(it) },
            checked = pointer.longPressEnabled,
            text = stringResource(id = R.string.pointer_long_press),
            secondaryText = stringResource(id = R.string.pointer_long_press_hint)
        )
        if (pointer.longPressEnabled) {
            var localLongPressDelay by remember(pointer.longPressDelayMs) { mutableStateOf(pointer.longPressDelayMs.toFloat()) }
            MyTextSlider(
                value = localLongPressDelay,
                onValueChange = { localLongPressDelay = it },
                onValueChangeFinished = {
                    vm.onPointerChange(currentPointer.copy(longPressDelayMs = localLongPressDelay.toLong()))
                    vm.savePointerSettings()
                },
            text = stringResource(id = R.string.pointer_long_press_delay_plain),
            valueDisplay = stringResource(id = R.string.pointer_long_press_delay, localLongPressDelay.toLong()),
                valueRange = 400f..2000f,
            )
            var localTolerance by remember(pointer.longPressMoveToleranceDp) { mutableStateOf(pointer.longPressMoveToleranceDp.toFloat()) }
            MyTextSlider(
                value = localTolerance,
                onValueChange = { localTolerance = it },
                onValueChangeFinished = {
                    vm.onPointerChange(currentPointer.copy(longPressMoveToleranceDp = localTolerance.toInt()))
                    vm.savePointerSettings()
                },
            text = stringResource(id = R.string.pointer_long_press_tolerance_plain),
            valueDisplay = stringResource(id = R.string.pointer_long_press_tolerance, localTolerance.toInt()),
                valueRange = 2f..16f,
            )
        }
    }
}

@Composable
internal fun pointerTrailStyleText(style: PointerTrailStyle): String {
    return when (style) {
        PointerTrailStyle.None -> stringResource(id = R.string.pointer_trail_style_close)
        PointerTrailStyle.Dots -> stringResource(id = R.string.pointer_trail_style_dot)
        PointerTrailStyle.LightBand -> stringResource(id = R.string.pointer_trail_style_band)
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
            Column(modifier = Modifier.padding(Spacing24)) {
                TextActionButton(
                    onClick = onBackupRequest,
                    text = stringResource(id = R.string.backup),
                    secondaryText = stringResource(id = R.string.backup_hint)
                )
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.padding(vertical = Spacing12),
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
