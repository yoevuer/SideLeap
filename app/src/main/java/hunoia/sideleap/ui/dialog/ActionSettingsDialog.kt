package hunoia.sideleap.ui.dialog

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
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
import hunoia.sideleap.system.intent.normalizeOpenAppOrUrl
import hunoia.sideleap.system.packages.queryIntentActivitiesCompat
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinInteractiveSize
import hunoia.sideleap.ui.theme.SubMinInteractiveSize
import hunoia.sideleap.ui.widget.MyTextSlider

import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import hunoia.sideleap.action.OpenAppOrUrlData
import hunoia.sideleap.utils.JsonHelper
import hunoia.sideleap.system.feedback.showToast

/**
 * @author DS-Z
 * @since 2025/6/30
 */

@Composable
fun OpenAppOrUrlSettingsContent(
    action: hunoia.sideleap.action.Action,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    val launcherApps = remember(context) { queryLauncherApps(context) }
    val existingData = remember {
        runCatching { JsonHelper.decodeFromString<OpenAppOrUrlData>(action.data) }.getOrNull()
    }

    var mode by remember { mutableStateOf(
        when (existingData?.type) {
            OpenAppOrUrlData.TYPE_URL -> 1
            else -> 0
        }
    ) }
    var appQuery by remember { mutableStateOf("") }
    var selectedApp by remember {
        mutableStateOf(
            if (existingData?.type == OpenAppOrUrlData.TYPE_ACTIVITY && existingData.packageName.isNotBlank()) {
                launcherApps.firstOrNull { it.packageName == existingData.packageName }
            } else null
        )
    }
    var selectedActivityClassName by remember {
        mutableStateOf(
            if (existingData?.type == OpenAppOrUrlData.TYPE_ACTIVITY) existingData.activityClassName else ""
        )
    }
    var activityQuery by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf(existingData?.url ?: "") }

    val filteredApps = launcherApps.filter {
        appQuery.isBlank() ||
        it.label.contains(appQuery, ignoreCase = true) ||
        it.packageName.contains(appQuery, ignoreCase = true)
    }
    val activityOptions = remember(selectedApp) {
        val app = selectedApp
        if (app != null) queryActivityOptions(
            context = context,
            packageName = app.packageName,
            selectedActivityClassName = "",
            launcherClassName = app.launcherClassName
        ) else emptyList()
    }
    val filteredActivities = activityOptions.filter {
        activityQuery.isBlank() ||
        formatActivityOptionText(it, selectedApp?.packageName ?: "").contains(activityQuery, ignoreCase = true) ||
        it.className.contains(activityQuery, ignoreCase = true)
    }
    val normalizedUrl = remember(urlInput) {
        urlInput.trim().takeIf { it.isNotBlank() }?.let { normalizeOpenAppOrUrl(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ItemPadding),
        verticalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                R.string.select_activity,
                R.string.add_link
            ).forEachIndexed { index, labelRes ->
                Surface(
                    onClick = { mode = index },
                    shape = RoundedCornerShape(16.dp),
                    color = if (mode == index) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = stringResource(labelRes),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (mode == index) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        when (mode) {
            0 -> {
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
                                            selectedActivityClassName = activityOptions.firstOrNull()?.className.orEmpty()
                                            appQuery = ""
                                            activityQuery = ""
                                        }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(ItemPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        modifier = Modifier.size(SubMinInteractiveSize),
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
                                            text = formatActivityOptionText(activity, app.packageName),
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

            1 -> {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text(stringResource(R.string.url_link)) },
                    singleLine = true
                )
                if (urlInput.isNotBlank()) {
                    if (normalizedUrl != null) {
                        Text(
                            text = normalizedUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.invalid_url),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    enabled = normalizedUrl != null,
                    onClick = {
                        onConfirm(
                            JsonHelper.encodeToString(
                                OpenAppOrUrlData(
                                    type = OpenAppOrUrlData.TYPE_URL,
                                    url = normalizedUrl!!
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
