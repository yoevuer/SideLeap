package hunoia.sideleap.ui.screen.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.component.UDFComponent
import hunoia.sideleap.R
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.ui.widget.MyColumn
import hunoia.sideleap.ui.widget.SectionCard
import hunoia.sideleap.ui.widget.TextActionButton
import hunoia.sideleap.ui.widget.TopBar
import hunoia.sideleap.utils.AboutUtils

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/23
 */

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onNavToDiagnosticLogs: () -> Unit = {},
    vm: AboutVM = viewModel()
) {
    UDFComponent(component = vm.udfComponent, onEvent = {}) { uiState ->
        Column {
            TopBar(
                onBack = onBack,
                title = stringResource(id = R.string.about)
            )
            MyColumn(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(ItemPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        modifier = Modifier.size(100.dp),
                        model = uiState.icon,
                        contentDescription = null,
                        imageLoader = LocalContext.current.imageLoader
                    )
                    Text(
                        text = uiState.appName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.W900)
                    )
                    Text(
                        text = uiState.versionName,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.overview)
                ) {
                    val context = LocalContext.current
                    TextActionButton(
                        onClick = { AboutUtils.openGithubRepo(context) },
                        text = stringResource(id = R.string.github_repo),
                        prefix = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.github),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    TextActionButton(
                        onClick = { AboutUtils.openOriginalProject(context) },
                        text = stringResource(id = R.string.original_project),
                        prefix = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.github),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    TextActionButton(
                        onClick = onNavToDiagnosticLogs,
                        text = stringResource(id = R.string.diagnostics_log)
                    )
                }
            }
        }
    }
}
