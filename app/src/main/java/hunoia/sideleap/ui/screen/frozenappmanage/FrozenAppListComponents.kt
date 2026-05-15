package hunoia.sideleap.ui.screen.frozenappmanage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.ktx.onClick
import hunoia.sideleap.R
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.ext.icon

@Composable
fun FrozenAppSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(id = R.string.search_app_hint)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.search_clear_cd)
                    )
                }
            }
        },
        singleLine = true
    )
}

@Composable
fun FrozenAppSelectableItem(
    app: AppInfo,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isFrozen: Boolean = false,
    showFrozenIndicator: Boolean = false,
    frozenActionEnabled: Boolean = false,
    onFrozenActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .onClick { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        AsyncImage(
            model = app.icon,
            contentDescription = null,
            imageLoader = context.imageLoader,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(40.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp)
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isFrozen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showFrozenIndicator) {
            IconButton(
                enabled = frozenActionEnabled,
                onClick = {
                    onFrozenActionClick?.invoke()
                }
            ) {
                Icon(
                    modifier = Modifier.alpha(if (isFrozen) 1f else 0.45f),
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = stringResource(id = R.string.frozen_state_indicator),
                    tint = if (isFrozen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
