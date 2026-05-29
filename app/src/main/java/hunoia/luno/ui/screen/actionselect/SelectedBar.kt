package hunoia.luno.ui.screen.actionselect

import hunoia.luno.ui.theme.*

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hunoia.luno.R

@Composable
fun SelectedBar(
    selectedItems: List<Any>,
    maxSelectCount: Int,
    showMaxSelectCount: Boolean,
    itemLabel: (Any) -> String,
    onRemoveItem: (Any) -> Unit,
    onClearAll: () -> Unit,
) {
    if (selectedItems.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPaddingHorizontal * 2, vertical = Spacing4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (showMaxSelectCount) {
                stringResource(R.string.selected_count, selectedItems.size, maxSelectCount)
            } else {
                stringResource(R.string.selected_count_no_limit, selectedItems.size)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onClearAll,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = Spacing2)
        ) {
            Text(stringResource(R.string.clear_all))
        }
    }
}
