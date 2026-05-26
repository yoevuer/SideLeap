package hunoia.luno.ui.screen.home.sheet

import androidx.compose.runtime.Composable
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.screen.home.AnimationStyleContent

@Composable
fun AnimationStyleSheet(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return
    OptimizedBottomSheet(onDismissRequest = onDismiss) {
        AnimationStyleContent(onDismiss = onDismiss)
    }
}
