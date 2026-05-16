package hunoia.sideleap.ui.screen.quickapplaunchermanage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.aaron.compose.component.UDFComponent
import hunoia.sideleap.R
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.ui.widget.SectionCard
import hunoia.sideleap.ui.widget.TextActionButton
import hunoia.sideleap.ui.widget.TopBar
import hunoia.sideleap.ui.widget.quickapplaunch.rememberAppIconAsync

@Composable
fun QuickAppLauncherManageScreen(onBack: () -> Unit, vm: QuickAppLauncherManageVM = viewModel()) {
    UDFComponent(component = vm.udfComponent, onEvent = {}) { uiState ->
        val context = LocalContext.current
        val keyOf = remember { { app: hunoia.sideleap.launcher.model.AppInfo -> "${app.packageName}/${app.className}" } }
        var searchQuery by remember { mutableStateOf("") }
        val filteredApps = remember(searchQuery, uiState.apps) {
            if (searchQuery.isBlank()) uiState.apps
            else uiState.apps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onBack = onBack, title = "管理隐藏应用")
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                item(key = "clear_stats") {
                    SectionCard {
                        TextActionButton(onClick = { vm.clearStats() }, text = "清除最近/频率记录")
                    }
                }
                item(key = "search_field") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        placeholder = { Text(stringResource(R.string.search_app_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.search_clear_cd))
                                }
                            }
                        },
                        singleLine = true
                    )
                }
                if (searchQuery.isNotBlank() && filteredApps.isEmpty()) {
                    item(key = "no_results") {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            text = stringResource(R.string.no_matching_results),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item(key = "hidden_header") {
                    SectionCard(modifier = Modifier.padding(top = SectionPadding), title = "已隐藏") { }
                }
                val keys = uiState.settings.hiddenApps.toList()
                items(keys, key = { "hidden:$it" }) { key ->
                    val app = filteredApps.firstOrNull { keyOf(it) == key } ?: return@items
                    val icon = rememberAppIconAsync(context, app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (icon != null) {
                            AsyncImage(
                                model = icon,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.width(40.dp).height(40.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(40.dp).height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = app.label, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = app.packageName, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextActionButton(onClick = { vm.setHidden(app, false) }, text = "恢复")
                    }
                }
                item(key = "all_header") {
                    SectionCard(modifier = Modifier.padding(top = SectionPadding), title = "全部应用 - 添加隐藏") { }
                }
                items(filteredApps, key = { "all:${keyOf(it)}" }) { app ->
                    val key = keyOf(app)
                    val selected = uiState.settings.hiddenApps.contains(key)
                    val icon = rememberAppIconAsync(context, app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (icon != null) {
                            AsyncImage(
                                model = icon,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.width(40.dp).height(40.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(40.dp).height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = app.packageName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = selected,
                            onCheckedChange = { vm.setHidden(app, it) }
                        )
                    }
                }
            }
        }
    }
}
