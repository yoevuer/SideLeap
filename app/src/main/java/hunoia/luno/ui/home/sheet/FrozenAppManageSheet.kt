package hunoia.luno.ui.home.sheet

import androidx.compose.runtime.Composable
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.freeze.FrozenAppManageContent

@Composable
fun FrozenAppManageSheet(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return
    OptimizedBottomSheet(onDismissRequest = onDismiss) {
        FrozenAppManageContent(onDismiss = onDismiss)
    }
}
