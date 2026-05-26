package hunoia.sideleap.ui.dialog

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.basicMarquee
import com.aaron.compose.component.LoadingComponent
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.onSingleClick
import hunoia.sideleap.R
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.ShellCommandData
import hunoia.sideleap.action.VirtualMouseActionData
import hunoia.sideleap.action.display.actionText
import hunoia.sideleap.core.serialization.JsonHelper

import hunoia.sideleap.launcher.model.OpenAppOrUrlData
import hunoia.sideleap.launcher.query.OpenAppOrUrlQuery
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxGotoBottomStrength
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxMoveScreenHover
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxMoveScreenRate
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinGotoBottomStrength
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinMoveScreenHover
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinMoveScreenRate
import hunoia.sideleap.system.shizuku.ShizukuBinderExecutor
import hunoia.sideleap.system.feedback.showToast
import hunoia.sideleap.ui.component.LabeledSwitch
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.ui.component.MyTextSlider
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinInteractiveSize
import hunoia.sideleap.ui.theme.SubMinInteractiveSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author DS-Z
 * @since 2025/6/30
 */

@Composable
fun ActivitySettingsContent(
    action: hunoia.sideleap.action.Action,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    val launcherApps = remember(context) { OpenAppOrUrlQuery.queryLauncherApps(context) }
    val existingData = remember {
        runCatching { JsonHelper.decodeFromString<OpenAppOrUrlData>(action.data) }.getOrNull()
    }

    var appQuery by remember { mutableStateOf("") }
    var selectedApp by remember {
        mutableStateOf(
            if (existingData?.type == OpenAppOrUrlData.TYPE_ACTIVITY && existingData.packageName.isNotBlank()) {
                launcherApps.firstOrNull { it.packageName == existingData.packageName }
            } else null
        )
    }
    var activityQuery by remember { mutableStateOf("") }

    val filteredApps = launcherApps.filter {
        appQuery.isBlank() ||
        it.label.contains(appQuery, ignoreCase = true) ||
        it.packageName.contains(appQuery, ignoreCase = true)
    }
    val activityOptions = remember(selectedApp) {
        val app = selectedApp
        if (app != null) OpenAppOrUrlQuery.queryActivityOptions(
            context = context,
            packageName = app.packageName,
            selectedActivityClassName = "",
            launcherClassName = app.launcherClassName
        ) else emptyList()
    }
    val filteredActivities = activityOptions.filter {
        activityQuery.isBlank() ||
        OpenAppOrUrlQuery.formatActivityOptionText(it, selectedApp?.packageName ?: "").contains(activityQuery, ignoreCase = true) ||
        it.className.contains(activityQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ItemPadding),
        verticalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        if (selectedApp == null) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = appQuery,
                onValueChange = { appQuery = it },
                label = { Text(stringResource(R.string.search_app_hint)) },
                singleLine = true
            )
            if (appQuery.isBlank()) {
                Text(
                    text = stringResource(R.string.no_matching_results),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else if (filteredApps.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_matching_results),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    filteredApps.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedApp = item
                                    appQuery = ""
                                    activityQuery = ""
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(ItemPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var icon by remember(item.packageName) { mutableStateOf<Drawable?>(null) }
                            LaunchedEffect(item.packageName) {
                                icon = withContext(Dispatchers.IO) {
                                    OpenAppOrUrlQuery.loadApplicationIcon(context, item.packageName)
                                }
                            }
                            AsyncImage(
                                modifier = Modifier.size(SubMinInteractiveSize),
                                model = icon,
                                contentDescription = null,
                                contentScale = ContentScale.Fit
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = item.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        } else {
            val app = selectedApp!!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${app.label} (${app.packageName})",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TextButton(onClick = { selectedApp = null }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = activityQuery,
                onValueChange = { activityQuery = it },
                label = { Text(stringResource(R.string.search_activity_hint)) },
                singleLine = true
            )
            if (filteredActivities.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_matching_results),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (if (activityQuery.isBlank()) activityOptions else filteredActivities).forEach { activity ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onConfirm(
                                        JsonHelper.encodeToString(
                                            OpenAppOrUrlData(
                                                type = OpenAppOrUrlData.TYPE_ACTIVITY,
                                                packageName = app.packageName,
                                                activityClassName = activity.className
                                            )
                                        )
                                    )
                                }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(ItemPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(SubMinInteractiveSize)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = OpenAppOrUrlQuery.formatActivityOptionText(activity, app.packageName),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = activity.className,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UrlSettingsContent(
    action: hunoia.sideleap.action.Action,
    onConfirm: (String) -> Unit
) {
    val existingData = remember {
        runCatching { JsonHelper.decodeFromString<OpenAppOrUrlData>(action.data) }.getOrNull()
    }
    var urlInput by remember { mutableStateOf(existingData?.url ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ItemPadding),
        verticalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text(stringResource(R.string.url_link)) },
            singleLine = true
        )
        TextButton(
            modifier = Modifier.align(Alignment.End),
            enabled = urlInput.isNotBlank(),
            onClick = {
                onConfirm(
                    JsonHelper.encodeToString(
                        OpenAppOrUrlData(
                            type = OpenAppOrUrlData.TYPE_URL,
                            url = urlInput.trim()
                        )
                    )
                )
            }
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Text(text = stringResource(id = R.string.confirm))
        }
    }
}

@Composable
fun VirtualMouseActionSettingsContent(
    action: hunoia.sideleap.action.Action,
    onConfirm: (String) -> Unit
) {
    val existingData = remember(action.data) {
        runCatching { JsonHelper.decodeFromString<VirtualMouseActionData>(action.data) }.getOrNull()
    }
    var mode by remember(action.data) { mutableStateOf(existingData?.mode ?: VirtualMouseActionData.Mode.Default) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ItemPadding),
        verticalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        Text(
            text = stringResource(R.string.virtual_mouse_action_mode),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        listOf(
            VirtualMouseActionData.Mode.Default to R.string.virtual_mouse_action_mode_default,
            VirtualMouseActionData.Mode.Continuous to R.string.virtual_mouse_action_mode_continuous,
            VirtualMouseActionData.Mode.Single to R.string.virtual_mouse_action_mode_single,
        ).forEach { (option, labelRes) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSingleClick { onConfirm(JsonHelper.encodeToString(VirtualMouseActionData(option))) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ItemPadding)
            ) {
                RadioButton(
                    selected = mode == option,
                    onClick = { onConfirm(JsonHelper.encodeToString(VirtualMouseActionData(option))) }
                )
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ShellCommandSettingsContent(
    action: hunoia.sideleap.action.Action,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val existingData = remember(action.data) {
        runCatching { JsonHelper.decodeFromString<ShellCommandData>(action.data) }.getOrNull()
    }
    var command by remember(action.data) { mutableStateOf(existingData?.command.orEmpty()) }
    var showToast by remember(action.data) { mutableStateOf(existingData?.showToast ?: true) }
    var testing by remember { mutableStateOf(false) }
    var testOutput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ItemPadding),
        verticalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = command,
            onValueChange = { command = it.take(2000) },
            label = { Text(stringResource(R.string.shell_command_label)) },
            placeholder = { Text(stringResource(R.string.shell_command_placeholder)) },
            minLines = 3,
            maxLines = 6,
        )
        Text(
            text = stringResource(R.string.shell_command_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.shell_command_show_toast),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = showToast,
                onCheckedChange = { showToast = it }
            )
        }
        if (testOutput.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = testOutput,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(ItemPadding),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                enabled = command.isNotBlank() && !testing,
                onClick = {
                    val testCommand = command.trim()
                    testing = true
                    testOutput = context.getString(R.string.testing)
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            ShizukuBinderExecutor.runShellCommand(context.applicationContext, testCommand)
                        }
                        testing = false
                        val output = result.output.ifBlank { context.getString(R.string.shell_command_no_output) }
                        testOutput = if (result.success) {
                            context.getString(
                                R.string.shell_command_test_output,
                                result.exitCode,
                                result.elapsedMs,
                                output
                            )
                        } else {
                            context.getString(
                                R.string.shell_command_test_error_output,
                                result.error ?: "unknown error",
                                result.exitCode,
                                result.elapsedMs,
                                output
                            )
                        }
                        if (result.success) {
                            showToast(result.output.ifBlank { context.getString(R.string.shell_command_no_output) }.take(500))
                        } else {
                            showToast((result.error ?: result.output.ifBlank { "unknown error" }).take(500))
                        }
                    }
                }
            ) {
                Text(stringResource(if (testing) R.string.testing else R.string.test))
            }
            TextButton(
                enabled = command.isNotBlank(),
                onClick = {
                    onConfirm(JsonHelper.encodeToString(ShellCommandData(command.trim(), showToast)))
                }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Text(text = stringResource(id = R.string.confirm))
            }
        }
    }
}

@Composable
fun MoveScreenSettingsContent(vm: ActionSettingsVM = viewModel()) {
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
                var localHoverDelay by remember(uiState.actionSettings.moveScreen.hoverDelayMs) { mutableStateOf(uiState.actionSettings.moveScreen.hoverDelayMs.toFloat()) }
                MyTextSlider(
                    value = uiState.actionSettings.moveScreen.rate,
                    onValueChange = { vm.onMoveScreenRateChange(it) },
                    onValueChangeFinished = { vm.saveSettings() },
                    text = stringResource(id = R.string.move_screen_rate),
                    valueDisplay = String.format("%.1f", uiState.actionSettings.moveScreen.rate),
                    valueRange = MinMoveScreenRate..MaxMoveScreenRate
                )
                MyTextSlider(
                    value = localHoverDelay,
                    onValueChange = { localHoverDelay = it },
                    onValueChangeFinished = {
                        vm.onMoveScreenHoverChange(localHoverDelay)
                        vm.saveSettings()
                    },
                    text = stringResource(id = R.string.hover_trigger_delay),
                    valueDisplay = "${localHoverDelay.toLong()}ms",
                    valueRange = MinMoveScreenHover..MaxMoveScreenHover
                )
            }
        }
    }
}

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
                            color = MaterialTheme.colorScheme.secondary
                        )
                    },
                    placeholder = {
                        Text(
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                            text = stringResource(R.string.typing_package_name_and_click_done),
                            fontSize = 14.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.secondary
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
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
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
                    color = MaterialTheme.colorScheme.secondary,
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
                    valueRange = 500f..5000f
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
                LabeledSwitch(
                    onCheckedChange = { vm.onVolumeScrubHorizontalEnabledChange(it) },
                    checked = uiState.actionSettings.volumeScrub.horizontalEnabled,
                    text = stringResource(id = R.string.horizontal_volume_scrub),
                    secondaryText = stringResource(id = R.string.horizontal_volume_scrub_hint)
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
                    valueRange = 8f..40f
                )
            }
        }
    }
}
