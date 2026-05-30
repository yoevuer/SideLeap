package hunoia.luno.ui.settings

import hunoia.luno.ui.theme.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aaron.compose.ktx.onSingleClick
import hunoia.luno.R
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.OpenAppOrUrlData
import hunoia.luno.core.JsonSerializer
import hunoia.luno.pointer.PointerActionData

@Composable
fun UrlSettingsContent(
    action: Action,
    onConfirm: (String) -> Unit
) {
    val existingData = remember {
        runCatching { JsonSerializer.decodeFromString<OpenAppOrUrlData>(action.data) }.getOrNull()
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
                    JsonSerializer.encodeToString(
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
fun PointerActionSettingsContent(
    action: Action,
    onConfirm: (String) -> Unit
) {
    val existingData = remember(action.data) {
        runCatching { JsonSerializer.decodeFromString<PointerActionData>(action.data) }.getOrNull()
    }
    var mode by remember(action.data) { mutableStateOf(existingData?.mode ?: PointerActionData.Mode.Default) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ItemPadding),
        verticalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        Text(
            text = stringResource(R.string.pointer_action_mode),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        listOf(
            PointerActionData.Mode.Default to R.string.pointer_action_mode_default,
            PointerActionData.Mode.Continuous to R.string.pointer_action_mode_continuous,
            PointerActionData.Mode.Single to R.string.pointer_action_mode_single,
        ).forEach { (option, labelRes) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSingleClick { onConfirm(JsonSerializer.encodeToString(PointerActionData(option))) }
                    .padding(vertical = Spacing2),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ItemPadding)
            ) {
                RadioButton(
                    selected = mode == option,
                    onClick = { onConfirm(JsonSerializer.encodeToString(PointerActionData(option))) }
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
