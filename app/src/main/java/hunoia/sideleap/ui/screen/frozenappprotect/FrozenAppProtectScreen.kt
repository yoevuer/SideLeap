package hunoia.sideleap.ui.screen.frozenappprotect

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import hunoia.sideleap.R
import hunoia.sideleap.ui.screen.frozenappmanage.FrozenAppSearchField
import hunoia.sideleap.ui.screen.frozenappmanage.FrozenAppSelectableItem
import hunoia.sideleap.ui.widget.LabeledSwitch

@Composable
fun FrozenAppProtectContent(
    onDismiss: () -> Unit,
    vm: FrozenAppProtectVM = viewModel()
) {
    UDFComponent(component = vm.udfComponent, onEvent = { }) { uiState ->
        LaunchedEffect(Unit) {
            vm.reloadApps()
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.protected_list), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = vm::reloadApps) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(id = R.string.refresh))
                }
            }
            LabeledSwitch(
                onCheckedChange = vm::onShowSystemAppsChange,
                checked = uiState.showSystemApps,
                text = stringResource(id = R.string.show_system_apps)
            )

                FrozenAppSearchField(
                    query = uiState.query,
                    onQueryChange = vm::onQueryChange,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (!uiState.hasAnyAppInRange) {
                        item(key = "empty") {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                text = stringResource(id = R.string.no_apps_available),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (!uiState.hasSearchResult) {
                        item(key = "no_match") {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                text = stringResource(id = R.string.no_matching_results),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.protectedAppsFirst, key = { it.packageName }) { app ->
                            FrozenAppSelectableItem(
                                app = app,
                                checked = app.packageName in uiState.protectedPackageNames,
                                isFrozen = uiState.frozenStateByPackage[app.packageName] == true,
                                showFrozenIndicator = true,
                                frozenActionEnabled = uiState.shizukuReady && uiState.frozenStateByPackage[app.packageName] == true && app.packageName !in uiState.runningPackageActions,
                                onFrozenActionClick = { vm.onUnfreeze(app.packageName) },
                                onCheckedChange = { vm.onProtectedChecked(app.packageName, it) }
                            )
                        }
                    }
                }

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = stringResource(id = R.string.protected_list_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
