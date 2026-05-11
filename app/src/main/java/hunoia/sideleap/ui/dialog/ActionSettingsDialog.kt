package hunoia.sideleap.ui.dialog

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.aaron.compose.component.LoadingComponent
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.onSingleClick
import hunoia.sideleap.R
import hunoia.sideleap.constant.GlobalSettings.MaxGotoBottomStrength
import hunoia.sideleap.constant.GlobalSettings.MaxMoveScreenHover
import hunoia.sideleap.constant.GlobalSettings.MaxMoveScreenRate
import hunoia.sideleap.constant.GlobalSettings.MinGotoBottomStrength
import hunoia.sideleap.constant.GlobalSettings.MinMoveScreenHover
import hunoia.sideleap.constant.GlobalSettings.MinMoveScreenRate
import hunoia.sideleap.ktx.queryIntentActivitiesCompat
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinInteractiveSize
import hunoia.sideleap.ui.widget.MyTextSlider

import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import hunoia.sideleap.entity.OpenAppOrUrlData
import hunoia.sideleap.utils.JsonHelper
import hunoia.sideleap.utils.showToast

/**
 * @author DS-Z
 * @since 2025/6/30
 */

@Composable
fun OpenAppOrUrlSettingsContent(
    action: hunoia.sideleap.entity.Action,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    var data by remember {
        mutableStateOf(
            try {
                JsonHelper.decodeFromString<OpenAppOrUrlData>(action.data)
            } catch (e: Exception) {
                OpenAppOrUrlData()
            }
        )
    }
    var showAppSelector by remember { mutableStateOf(false) }
    var showActivitySelector by remember { mutableStateOf(false) }
    var appQuery by remember { mutableStateOf("") }
    var activityQuery by remember { mutableStateOf("") }

    val launcherApps = remember(context) { queryLauncherApps(context) }
    val selectedLauncherApp = launcherApps.firstOrNull { it.packageName == data.packageName }
    val filteredLauncherApps = launcherApps.filter {
        appQuery.isBlank() || it.label.contains(appQuery, ignoreCase = true) || it.packageName.contains(appQuery, ignoreCase = true)
    }
    val activityOptions = remember(context, data.packageName, data.activityClassName, selectedLauncherApp) {
        queryActivityOptions(
            context = context,
            packageName = data.packageName,
            selectedActivityClassName = data.activityClassName,
            launcherClassName = selectedLauncherApp?.launcherClassName
        )
    }
    val filteredActivityOptions = activityOptions.filter {
        activityQuery.isBlank() || formatActivityOptionText(it, data.packageName).contains(activityQuery, ignoreCase = true) ||
            it.className.contains(activityQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ItemPadding),
        verticalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        Text(text = stringResource(R.string.launch_mode), style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = data.type == OpenAppOrUrlData.TYPE_ACTIVITY,
                onClick = { data = data.copy(type = OpenAppOrUrlData.TYPE_ACTIVITY) }
            )
            Text(
                text = stringResource(R.string.mode_activity),
                modifier = Modifier.onSingleClick { data = data.copy(type = OpenAppOrUrlData.TYPE_ACTIVITY) }
            )
            RadioButton(
                selected = data.type == OpenAppOrUrlData.TYPE_URL,
                onClick = { data = data.copy(type = OpenAppOrUrlData.TYPE_URL) }
            )
            Text(
                text = stringResource(R.string.mode_url),
                modifier = Modifier.onSingleClick { data = data.copy(type = OpenAppOrUrlData.TYPE_URL) }
            )
        }

        if (data.type == OpenAppOrUrlData.TYPE_ACTIVITY) {
            Box {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onClick {
                            showActivitySelector = false
                            showAppSelector = true
                        },
                    value = selectedLauncherApp?.label?.let { "$it (${selectedLauncherApp.packageName})" }
                        ?: data.packageName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.package_name)) },
                    singleLine = true
                )
                DropdownMenu(
                    expanded = showAppSelector,
                    onDismissRequest = {
                        showAppSelector = false
                        appQuery = ""
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(ItemPadding)) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = appQuery,
                            onValueChange = { appQuery = it },
                            label = { Text(stringResource(R.string.search_app_hint)) },
                            singleLine = true
                        )
                        if (filteredLauncherApps.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.no_matching_results)) },
                                onClick = {},
                                enabled = false
                            )
                        } else {
                            filteredLauncherApps.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(ItemPadding),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                modifier = Modifier.size(MinInteractiveSize),
                                                model = runCatching { context.packageManager.getApplicationIcon(item.packageName) }.getOrNull(),
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
                                    },
                                    onClick = {
                                        val nextActivityClassName = resolveInitialActivityClassName(
                                            context = context,
                                            packageName = item.packageName,
                                            selectedActivityClassName = data.activityClassName,
                                            launcherClassName = item.launcherClassName
                                        )
                                        data = data.copy(
                                            packageName = item.packageName,
                                            activityClassName = nextActivityClassName
                                        )
                                        appQuery = ""
                                        showAppSelector = false
                                        showActivitySelector = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (data.packageName.isNotBlank()) {
                Box {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onClick {
                                showAppSelector = false
                                showActivitySelector = true
                            },
                        value = activityOptions.firstOrNull { it.className == data.activityClassName }?.label
                            ?: data.activityClassName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.activity_class_name)) },
                        singleLine = true
                    )
                    DropdownMenu(
                        expanded = showActivitySelector,
                        onDismissRequest = {
                            showActivitySelector = false
                            activityQuery = ""
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(ItemPadding)) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = activityQuery,
                                onValueChange = { activityQuery = it },
                                label = { Text(stringResource(R.string.search_activity_hint)) },
                                singleLine = true
                            )
                            if (filteredActivityOptions.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.no_matching_results)) },
                                    onClick = {},
                                    enabled = false
                                )
                            } else {
                                filteredActivityOptions.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(ItemPadding),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = null
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = formatActivityOptionText(item, data.packageName),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = item.className,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            data = data.copy(activityClassName = item.className)
                                            activityQuery = ""
                                            showActivitySelector = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = data.url,
                onValueChange = { data = data.copy(url = it) },
                label = { Text(stringResource(R.string.url_link)) },
                singleLine = true
            )
        }
        TextButton(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                val trimmedPackageName = data.packageName.trim()
                val trimmedActivityClassName = data.activityClassName.trim()
                val trimmedUrl = data.url.trim()
                val isValid = if (data.type == OpenAppOrUrlData.TYPE_ACTIVITY) {
                    trimmedPackageName.isNotEmpty() && trimmedActivityClassName.isNotEmpty()
                } else {
                    trimmedUrl.isNotEmpty()
                }
                if (isValid) {
                    onConfirm(
                        JsonHelper.encodeToString(
                            data.copy(
                                packageName = trimmedPackageName,
                                activityClassName = trimmedActivityClassName,
                                url = trimmedUrl
                            )
                        )
                    )
                } else {
                    showToast(R.string.please_input_complete_info)
                }
            }
        ) {
            Text(text = stringResource(id = R.string.confirm))
        }
    }
}

private data class LauncherAppOption(
    val packageName: String,
    val launcherClassName: String,
    val label: String
)

private data class ActivityOption(
    val className: String,
    val label: String
)

private fun queryLauncherApps(context: android.content.Context): List<LauncherAppOption> {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val result = mutableListOf<LauncherAppOption>()
    val seenPackages = mutableSetOf<String>()
    for (resolveInfo in packageManager.queryIntentActivitiesCompat(intent, PackageManager.MATCH_ALL)) {
        val activityInfo = resolveInfo.activityInfo ?: continue
        val packageName = activityInfo.packageName ?: continue
        if (!seenPackages.add(packageName)) continue
        result += LauncherAppOption(
            packageName = packageName,
            launcherClassName = activityInfo.name,
            label = activityInfo.loadLabel(packageManager).toString()
        )
    }
    return result.sortedWith(compareBy<LauncherAppOption> { it.label }.thenBy { it.packageName })
}

private fun queryActivityOptions(
    context: android.content.Context,
    packageName: String,
    selectedActivityClassName: String,
    launcherClassName: String?
): List<ActivityOption> {
    if (packageName.isBlank()) return emptyList()
    val packageManager = context.packageManager
    val exportedActivities = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        }
    } catch (_: Exception) {
        null
    }?.activities
        ?.filter { it.exported }
        ?.map {
            ActivityOption(
                className = it.name,
                label = it.loadLabel(packageManager).toString()
            )
        }
        .orEmpty()

    return if (exportedActivities.isNotEmpty()) {
        exportedActivities
            .distinctBy { it.className }
            .sortedWith(compareBy<ActivityOption> { it.label }.thenBy { it.className })
    } else {
        listOfNotNull(
            selectedActivityClassName.takeIf { it.isNotBlank() }?.let { ActivityOption(it, it) },
            launcherClassName?.takeIf { it.isNotBlank() }?.let { ActivityOption(it, it) }
        ).distinctBy { it.className }
    }
}

private fun formatActivityOptionText(option: ActivityOption, packageName: String): String {
    val shortClassName = if (packageName.isNotBlank() && option.className.startsWith("$packageName.")) {
        option.className.removePrefix("$packageName.")
    } else {
        option.className
    }
    return shortClassName
}

private fun resolveInitialActivityClassName(
    context: android.content.Context,
    packageName: String,
    selectedActivityClassName: String,
    launcherClassName: String?
): String {
    return queryActivityOptions(
        context = context,
        packageName = packageName,
        selectedActivityClassName = selectedActivityClassName,
        launcherClassName = launcherClassName
    ).firstOrNull()?.className ?: ""
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
                MyTextSlider(
                    value = uiState.actionSettings.moveScreen.rate,
                    onValueChange = { vm.onMoveScreenRateChange(it) },
                    onValueChangeFinished = { vm.saveSettings() },
                    text = stringResource(id = R.string.move_screen_rate),
                    sliderValueHint = stringResource(id = R.string.slow) to stringResource(id = R.string.fast),
                    valueRange = MinMoveScreenRate..MaxMoveScreenRate
                )
                MyTextSlider(
                    value = uiState.actionSettings.moveScreen.hoverDelayMs.toFloat(),
                    onValueChange = { vm.onMoveScreenHoverChange(it) },
                    onValueChangeFinished = { vm.saveSettings() },
                    text = stringResource(id = R.string.hover_trigger_delay),
                    sliderValueHint = stringResource(id = R.string.short1) to stringResource(id = R.string.long1),
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
                MyTextSlider(
                    value = uiState.actionSettings.gotoBottom.strength.toFloat(),
                    onValueChange = { vm.onGotoBottomStrengthChange(it) },
                    onValueChangeFinished = { vm.saveSettings() },
                    text = stringResource(id = R.string.strength),
                    sliderValueHint = stringResource(id = R.string.small) to stringResource(id = R.string.large),
                    valueRange = MinGotoBottomStrength..MaxGotoBottomStrength
                )
            }
        }
    }
}
