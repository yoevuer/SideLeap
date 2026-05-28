package hunoia.luno.ui.screen.home
import hunoia.luno.ui.theme.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.util.fastForEach
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
import hunoia.luno.ui.screen.home.HomeVM.RenameTarget
import hunoia.luno.ui.screen.home.HomeVM.UiEvent
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVerticalWithSection
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.MinItemHeightNoSecondary
import hunoia.luno.ui.theme.SectionPadding
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.component.MyColumn
import hunoia.luno.ui.component.ExpressiveCard
import hunoia.luno.ui.component.ExpressiveSwitchItem
import hunoia.luno.ui.component.MyTextSlider
import hunoia.luno.ui.component.TopBar
import hunoia.luno.ui.component.ColorPickerBottomSheet
import hunoia.luno.ui.component.ColorSelection
import hunoia.luno.ui.ext.resolveColor
import hunoia.luno.settings.model.ThemeColorKey
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.settings.model.SubGesture
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
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
        var showResetConfirm by remember { mutableStateOf(false) }
        var colorPickerTarget by remember { mutableStateOf<Any?>(null) }
        var colorPickerColor by remember { mutableStateOf(Color.Transparent) }
        var myColumnWindowY by remember { mutableIntStateOf(0) }
        var cardAreaWindowY by remember { mutableIntStateOf(0) }
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
                if (colorPickerTarget != null) {
                    val scheme = MaterialTheme.colorScheme
                    val themeColorArgb = remember(scheme) {
                        ThemeColorKey.entries.associateWith { it.resolveColor(scheme).toArgb() }
                    }
                    ColorPickerBottomSheet(
                        onDismissRequest = { colorPickerTarget = null },
                        onColorSelected = { selection ->
                            when (selection) {
                                is ColorSelection.Custom -> {
                                    when (val target = colorPickerTarget) {
                                        is GestureButton -> vm.updateGestureButtonColor(target, selection.color.toArgb())
                                        is SubGesture -> vm.updateSubGestureColor(target, selection.color.toArgb())
                                    }
                                }
                                is ColorSelection.Theme -> {
                                    themeColorArgb[selection.key]?.let { resolvedArgb ->
                                        when (val target = colorPickerTarget) {
                                            is GestureButton -> vm.updateGestureButtonColor(target, resolvedArgb)
                                            is SubGesture -> vm.updateSubGestureColor(target, resolvedArgb)
                                        }
                                    }
                                }
                            }
                            colorPickerTarget = null
                        },
                        initialColor = colorPickerColor,
                    )
                }
                Column {
                TopBar(
                    onBack = { },
                    title = stringResource(id = R.string.home_title),
                    showBackIcon = false,
                    titleStyle = MaterialTheme.typography.headlineSmall,
                    actions = {}
                )

                MyColumn(
                    modifier = Modifier.onGloballyPositioned { coords ->
                        myColumnWindowY = coords.positionInWindow().y.roundToInt()
                    },
                    scrollState = scrollState,
                ) {
                    HomeHeroCard(
                        uiState = uiState,
                        onClick = {
                            if (uiState.isGestureEnabled) {
                                vm.onAppGestureEnabledChange(false)
                            } else if (!uiState.isAccessibilityEnabled) {
                                context.gotoAccessibilitySettings()
                            } else {
                                vm.onAppGestureEnabledChange(true)
                            }
                        },
                        onCheckedChange = {
                            if (it && !uiState.isAccessibilityEnabled) {
                                context.gotoAccessibilitySettings()
                            } else {
                                vm.onAppGestureEnabledChange(it)
                            }
                        }
                    )

                    Spacer(Modifier.height(SectionPadding))

                    HomeFeatureGrid(
                        uiState = uiState,
                        onAnimationClick = { showAnimationStyle = true },
                        onAnimationEnabledChange = { vm.onShowAnimation(it) },
                        onExcludeClick = { showAppBlacklist = true },
                        onPointerClick = { showPointerSettings = true },
                        onFrozenClick = { showFrozenManage = true },
                        onFreezeClick = { vm.oneKeyFreeze() },
                        onUnfreezeClick = { vm.oneKeyUnfreeze() },
                        onMiniWindowClick = { showMiniWindowSettings = true },
                        onMiniWindowOverrideChange = { vm.onMiniWindowOverrideBoundsChange(it) },
                        onBackupClick = {
                            val appName = context.getString(context.applicationInfo.labelRes)
                            val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(System.currentTimeMillis()))
                            createFileLauncher.launch("${appName}_$date.zip")
                        },
                        onRestoreClick = { getFileLauncher.launch("*/*") },
                        onResetToggle = { showResetConfirm = !showResetConfirm },
                        showResetConfirm = showResetConfirm,
                        onResetConfirm = {
                            vm.reset()
                            showResetConfirm = false
                        },
                        onResetDismiss = { showResetConfirm = false },
                        onCardAreaPosition = { cardAreaWindowY = it },
                    )

                    Spacer(Modifier.height(SectionPadding))

                    LaunchedEffect(
                        uiState.isBottomGestureButtonListExpanded,
                        uiState.isSideGestureButtonListExpanded,
                        uiState.isSubGestureListExpanded,
                    ) {
                        if (uiState.isBottomGestureButtonListExpanded ||
                            uiState.isSideGestureButtonListExpanded ||
                            uiState.isSubGestureListExpanded
                        ) {
                            kotlinx.coroutines.delay(120)
                            scrollState.animateScrollTo(
                                scrollState.maxValue,
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = FastOutSlowInEasing,
                                ),
                            )
                        }
                    }

                    LaunchedEffect(showResetConfirm) {
                        if (showResetConfirm) {
                            kotlinx.coroutines.delay(120)
                            if (cardAreaWindowY > 0 && myColumnWindowY > 0) {
                                val targetScroll = (cardAreaWindowY - myColumnWindowY - 120).coerceAtLeast(0)
                                if (targetScroll > scrollState.value) {
                                    scrollState.animateScrollTo(
                                        targetScroll,
                                        animationSpec = tween(
                                            durationMillis = 400,
                                            easing = FastOutSlowInEasing,
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    HomeGestureSections(
                        uiState = uiState,
                        onBottomHeaderClick = {
                            if (uiState.isBottomGestureButtonListExpanded) {
                                vm.expandBottomGestureButtonList(false)
                            } else {
                                vm.expandSideGestureButtonList(false)
                                vm.expandSubGestureList(false)
                                vm.expandBottomGestureButtonList(true)
                            }
                        },
                        onSideHeaderClick = {
                            if (uiState.isSideGestureButtonListExpanded) {
                                vm.expandSideGestureButtonList(false)
                            } else {
                                vm.expandBottomGestureButtonList(false)
                                vm.expandSubGestureList(false)
                                vm.expandSideGestureButtonList(true)
                            }
                        },
                        onSubHeaderClick = {
                            if (uiState.isSubGestureListExpanded) {
                                vm.expandSubGestureList(false)
                            } else {
                                vm.expandBottomGestureButtonList(false)
                                vm.expandSideGestureButtonList(false)
                                vm.expandSubGestureList(true)
                            }
                        },
                        onBottomButtonClick = onNavToGestureButtonSettings,
                        onSideButtonClick = onNavToGestureButtonSettings,
                        onSubGestureClick = onNavToSubGestureEditor,
                        onBottomCheckedChange = { button, enabled -> vm.onBottomGestureButtonEnabledChange(button, enabled) },
                        onSideCheckedChange = { button, enabled -> vm.onSideGestureButtonEnabledChange(button, enabled) },
                        onSubCheckedChange = { gesture, enabled -> vm.onSubGestureEnabledChange(gesture, enabled) },
                        onAddBottom = { vm.addBottomGestureButton() },
                        onAddSide = { vm.addSideGestureButton() },
                        onAddSub = {
                            val id = java.util.UUID.randomUUID().toString()
                            vm.addSubGesture(id)
                        },
                        onMarkColorClick = { target ->
                            colorPickerTarget = target
                            colorPickerColor = when (target) {
                                is GestureButton -> Color(target.color)
                                is SubGesture -> Color(target.color)
                                else -> Color.Transparent
                            }
                        },
                        onGestureButtonRename = { vm.showRenameDialog(RenameTarget.GestureButton(it)) },
                        onSubGestureRename = { vm.showRenameDialog(RenameTarget.SubGesture(it)) },
                    )
                }

                uiState.renameDialogTarget?.let { target ->
                    val initialName = when (target) {
                        is RenameTarget.GestureButton -> target.button.name
                        is RenameTarget.SubGesture -> target.gesture.name
                    }
                    var text by remember(target) { mutableStateOf(initialName) }
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(100)
                        focusRequester.requestFocus()
                    }
                    AlertDialog(
                        onDismissRequest = { vm.hideRenameDialog() },
                        title = { Text(stringResource(R.string.rename)) },
                        text = {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { vm.doRename(target, text) }),
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { vm.doRename(target, text) }) {
                                Text(stringResource(R.string.confirm))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { vm.hideRenameDialog() }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
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
                                    color = when (button.color == android.graphics.Color.TRANSPARENT) {
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

@Composable
private fun HomeHeroCard(
    uiState: HomeVM.UiState,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    val enabled = uiState.isGestureEnabled
    val status = when {
        enabled -> "手势运行中"
        !uiState.isAccessibilityEnabled -> "需要无障碍权限"
        else -> "手势已关闭"
    }
    val desc = when {
        enabled -> "底部、侧边和子手势会按当前配置响应。"
        !uiState.isAccessibilityEnabled -> "开启前需要先授予无障碍服务权限。"
        else -> "开启后即可使用已配置的所有手势入口。"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing20),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing16),
        ) {
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(Spacing56),
                shape = MaterialTheme.shapes.large,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (enabled) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.surface,
                    contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(
                    imageVector = if (enabled) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.gesture_switch),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(Spacing4))
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(Spacing6))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun HomeFeatureGrid(
    uiState: HomeVM.UiState,
    onAnimationClick: () -> Unit,
    onAnimationEnabledChange: (Boolean) -> Unit,
    onExcludeClick: () -> Unit,
    onPointerClick: () -> Unit,
    onFrozenClick: () -> Unit,
    onFreezeClick: () -> Unit,
    onUnfreezeClick: () -> Unit,
    onMiniWindowClick: () -> Unit,
    onMiniWindowOverrideChange: (Boolean) -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onResetToggle: () -> Unit,
    showResetConfirm: Boolean,
    onResetConfirm: () -> Unit,
    onResetDismiss: () -> Unit,
    onCardAreaPosition: (Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val useTwoColumns = maxWidth >= HomeWideBreakpoint
        if (useTwoColumns) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing12),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing12),
                ) {
                    HomeAnimationCard(uiState, onAnimationClick, onAnimationEnabledChange)
                    HomePointerCard(onPointerClick)
                    HomeMiniWindowCard(uiState, onMiniWindowClick, onMiniWindowOverrideChange)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing12),
                ) {
                    HomeFrozenCard(uiState, onFrozenClick, onFreezeClick, onUnfreezeClick)
                    HomeExcludeCard(uiState, onExcludeClick)
                    HomeToolsCard(
                        onBackupClick, onRestoreClick,
                        onResetToggle, showResetConfirm, onResetConfirm, onResetDismiss,
                        onCardAreaPosition,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing12),
            ) {
                HomeAnimationCard(uiState, onAnimationClick, onAnimationEnabledChange)
                HomePointerCard(onPointerClick)
                HomeFrozenCard(uiState, onFrozenClick, onFreezeClick, onUnfreezeClick)
                HomeMiniWindowCard(uiState, onMiniWindowClick, onMiniWindowOverrideChange)
                HomeExcludeCard(uiState, onExcludeClick)
                HomeToolsCard(
                    onBackupClick, onRestoreClick,
                    onResetToggle, showResetConfirm, onResetConfirm, onResetDismiss,
                    onCardAreaPosition,
                )
            }
        }
    }
}

@Composable
private fun HomeAnimationCard(
    uiState: HomeVM.UiState,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    ExpressiveCard(
        title = stringResource(id = R.string.animation_style_short),
        subtitle = "回弹、样式与触发反馈",
        icon = Icons.Default.Animation,
        onClick = onClick,
        trailing = {
            Switch(checked = uiState.showAnimation, onCheckedChange = onCheckedChange)
        },
    ) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text("动画样式")
        }
    }
}

@Composable
private fun HomeExcludeCard(uiState: HomeVM.UiState, onClick: () -> Unit) {
    ExpressiveCard(
        title = stringResource(id = R.string.exclude_app_short),
        subtitle = "${uiState.excludedAppCount} 个应用已排除",
        icon = Icons.Default.Block,
        onClick = onClick,
        accent = MaterialTheme.colorScheme.secondaryContainer,
        onAccent = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text("管理排除")
        }
    }
}

@Composable
private fun HomePointerCard(onClick: () -> Unit) {
    ExpressiveCard(
        title = stringResource(id = R.string.pointer),
        subtitle = "灵敏度、轨迹与长按",
        icon = Icons.Default.TouchApp,
        onClick = onClick,
    ) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text("指针设置")
        }
    }
}

@Composable
private fun HomeFrozenCard(
    uiState: HomeVM.UiState,
    onClick: () -> Unit,
    onFreezeClick: () -> Unit,
    onUnfreezeClick: () -> Unit,
) {
    ExpressiveCard(
        title = stringResource(id = R.string.frozen_app_manage_short),
        subtitle = "已冻结 ${uiState.frozenAppCount} / 已选 ${uiState.selectedFrozenAppCount}",
        icon = Icons.Default.AcUnit,
        onClick = onClick,
        accent = MaterialTheme.colorScheme.tertiaryContainer,
        onAccent = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing6)) {
            FilledTonalButton(onClick = onFreezeClick, modifier = Modifier.weight(1f)) {
                Text("冻结")
            }
            FilledTonalButton(onClick = onUnfreezeClick, modifier = Modifier.weight(1f)) {
                Text("解冻")
            }
        }
    }
}

@Composable
private fun HomeMiniWindowCard(
    uiState: HomeVM.UiState,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    ExpressiveCard(
        title = stringResource(id = R.string.mini_window_position_short),
        subtitle = if (uiState.miniWindowOverrideBounds) "自定义位置与大小" else "由系统决定小窗边界",
        icon = Icons.Default.Widgets,
        onClick = onClick,
        trailing = {
            Switch(checked = uiState.miniWindowOverrideBounds, onCheckedChange = onCheckedChange)
        },
    ) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text("位置大小")
        }
    }
}

@Composable
private fun HomeToolsCard(
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onResetToggle: () -> Unit,
    showResetConfirm: Boolean,
    onResetConfirm: () -> Unit,
    onResetDismiss: () -> Unit,
    onCardAreaPosition: (Int) -> Unit,
) {
    ExpressiveCard(
        title = "工具",
        subtitle = "备份、恢复、恢复默认",
        icon = Icons.Default.Build,
        onClick = {},
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing6)) {
            FilledTonalButton(onClick = onBackupClick, modifier = Modifier.weight(1f)) {
                Text("备份")
            }
            FilledTonalButton(onClick = onRestoreClick, modifier = Modifier.weight(1f)) {
                Text("恢复")
            }
            FilledTonalButton(onClick = onResetToggle, modifier = Modifier.weight(1f)) {
                Text("默认")
            }
        }
        AnimatedVisibility(
            visible = showResetConfirm,
            enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        onCardAreaPosition(coords.positionInWindow().y.roundToInt())
                    }
                    .padding(top = Spacing12),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                Column(modifier = Modifier.padding(Spacing14)) {
                    Text(
                        text = stringResource(id = R.string.reset_default_settings_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Spacer(Modifier.height(Spacing12))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing6),
                    ) {
                        FilledTonalButton(
                            onClick = onResetDismiss,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("取消")
                        }
                        FilledTonalButton(
                            onClick = onResetConfirm,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("确认重置")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeGestureSections(
    uiState: HomeVM.UiState,
    onBottomHeaderClick: () -> Unit,
    onSideHeaderClick: () -> Unit,
    onSubHeaderClick: () -> Unit,
    onBottomButtonClick: (GestureButton) -> Unit,
    onSideButtonClick: (GestureButton) -> Unit,
    onSubGestureClick: (String) -> Unit,
    onBottomCheckedChange: (GestureButton, Boolean) -> Unit,
    onSideCheckedChange: (GestureButton, Boolean) -> Unit,
    onSubCheckedChange: (SubGesture, Boolean) -> Unit,
    onAddBottom: () -> Unit,
    onAddSide: () -> Unit,
    onAddSub: () -> Unit,
    onMarkColorClick: (Any) -> Unit,
    onGestureButtonRename: (GestureButton) -> Unit = {},
    onSubGestureRename: (SubGesture) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column {
            GestureEntryCard(
                title = stringResource(id = R.string.bottom_gesture_button_list_short),
                subtitle = "${uiState.bottomGestureButtons.count { it.enabled }} / ${uiState.bottomGestureButtons.size} 个已启用",
                icon = Icons.Default.ArrowUpward,
                expanded = uiState.isBottomGestureButtonListExpanded,
                onClick = onBottomHeaderClick,
            )
            GestureButtonList(
                visible = uiState.isBottomGestureButtonListExpanded,
                buttons = uiState.bottomGestureButtons,
                onItemClick = onBottomButtonClick,
                onCheckedChange = onBottomCheckedChange,
                onAddClick = onAddBottom,
                onMarkColorClick = onMarkColorClick,
                onRenameClick = onGestureButtonRename,
            )
        }
        Spacer(Modifier.height(Spacing12))
        Column {
            GestureEntryCard(
                title = stringResource(id = R.string.side_gesture_button_list_short),
                subtitle = "${uiState.sideGestureButtons.count { it.enabled }} / ${uiState.sideGestureButtons.size} 个已启用",
                icon = Icons.Default.SwapHoriz,
                expanded = uiState.isSideGestureButtonListExpanded,
                onClick = onSideHeaderClick,
                accent = MaterialTheme.colorScheme.secondaryContainer,
                onAccent = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            GestureButtonList(
                visible = uiState.isSideGestureButtonListExpanded,
                buttons = uiState.sideGestureButtons,
                onItemClick = onSideButtonClick,
                onCheckedChange = onSideCheckedChange,
                onAddClick = onAddSide,
                onMarkColorClick = onMarkColorClick,
                onRenameClick = onGestureButtonRename,
            )
        }
        Spacer(Modifier.height(Spacing12))
        Column {
            GestureEntryCard(
                title = stringResource(id = R.string.sub_gesture_list),
                subtitle = "${uiState.subGestures.count { it.enabled }} / ${uiState.subGestures.size} 个已启用",
                icon = Icons.Default.AllInclusive,
                expanded = uiState.isSubGestureListExpanded,
                onClick = onSubHeaderClick,
                accent = MaterialTheme.colorScheme.tertiaryContainer,
                onAccent = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            SubGestureList(
                visible = uiState.isSubGestureListExpanded,
                gestures = uiState.subGestures,
                onItemClick = onSubGestureClick,
                onCheckedChange = onSubCheckedChange,
                onAddClick = onAddSub,
                onMarkColorClick = onMarkColorClick,
                onRenameClick = onSubGestureRename,
            )
        }
    }
}

@Composable
private fun GestureEntryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    accent: Color = MaterialTheme.colorScheme.primaryContainer,
    onAccent: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    ExpressiveCard(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = onClick,
        accent = accent,
        onAccent = onAccent,
        trailing = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(if (expanded) "收起" else "展开")
        }
    }
}

@Composable
private fun GestureButtonList(
    visible: Boolean,
    buttons: List<GestureButton>,
    onItemClick: (GestureButton) -> Unit,
    onCheckedChange: (GestureButton, Boolean) -> Unit,
    onAddClick: () -> Unit,
    onMarkColorClick: (Any) -> Unit,
    onRenameClick: (GestureButton) -> Unit = {},
) {
    AnimatedVisibility(
        modifier = Modifier.fillMaxWidth(),
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) + fadeOut(),
    ) {
        Column(modifier = Modifier.padding(top = Spacing12)) {
            buttons.fastForEach { button ->
                key(button) {
                    val markColor = when (button.color == android.graphics.Color.TRANSPARENT) {
                        true -> MaterialTheme.colorScheme.primary.copy(alpha = GestureButtonColorAlpha)
                        else -> Color(button.color).copy(alpha = GestureButtonColorAlpha)
                    }
                    ExpressiveSwitchItem(
                        title = button.buttonTextCompose(),
                        checked = button.enabled,
                        markColor = markColor,
                        onMarkColorClick = { onMarkColorClick(button) },
                        onClick = { onItemClick(button) },
                        onCheckedChange = { onCheckedChange(button, it) },
                        modifier = Modifier.padding(bottom = Spacing8),
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(Spacing24)
                                    .onSingleClick { onRenameClick(button) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "重命名",
                                    modifier = Modifier.size(Spacing16),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                }
            }
            AddEntryButton(
                text = stringResource(id = R.string.add_gesture_button),
                onClick = onAddClick,
            )
        }
    }
}

@Composable
private fun SubGestureList(
    visible: Boolean,
    gestures: List<SubGesture>,
    onItemClick: (String) -> Unit,
    onCheckedChange: (SubGesture, Boolean) -> Unit,
    onAddClick: () -> Unit,
    onMarkColorClick: (Any) -> Unit,
    onRenameClick: (SubGesture) -> Unit = {},
) {
    AnimatedVisibility(
        modifier = Modifier.fillMaxWidth(),
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) + fadeOut(),
    ) {
        Column(modifier = Modifier.padding(top = Spacing12)) {
            gestures.fastForEach { gesture ->
                key(gesture.id) {
                    ExpressiveSwitchItem(
                        title = gesture.name,
                        checked = gesture.enabled,
                        markColor = Color(gesture.color).copy(alpha = GestureButtonColorAlpha),
                        onMarkColorClick = { onMarkColorClick(gesture) },
                        onClick = { onItemClick(gesture.id) },
                        onCheckedChange = { onCheckedChange(gesture, it) },
                        modifier = Modifier.padding(bottom = Spacing8),
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(Spacing24)
                                    .onSingleClick { onRenameClick(gesture) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "重命名",
                                    modifier = Modifier.size(Spacing16),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                }
            }
            AddEntryButton(
                text = stringResource(id = R.string.add_sub_gesture),
                onClick = onAddClick,
            )
        }
    }
}

@Composable
private fun AddEntryButton(text: String, onClick: () -> Unit) {
    FilledTonalButton(
        modifier = Modifier.fillMaxWidth().heightIn(min = MinItemHeightNoSecondary),
        onClick = onClick,
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
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
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onPointerContinuousModeChange(it) },
            checked = pointer.continuousMode,
            title = stringResource(id = R.string.pointer_continuous_mode),
            subtitle = stringResource(id = R.string.pointer_continuous_mode_hint)
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
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onPointerClickAnimationChange(it) },
            checked = pointer.clickAnimationEnabled,
            title = stringResource(id = R.string.pointer_click_animation)
        )
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onPointerLongPressEnabledChange(it) },
            checked = pointer.longPressEnabled,
            title = stringResource(id = R.string.pointer_long_press),
            subtitle = stringResource(id = R.string.pointer_long_press_hint)
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
