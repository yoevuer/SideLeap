package hunoia.luno.ui.screen.home.sheet

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import hunoia.luno.ui.component.BottomSheetNestedContent
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.component.OptimizedScrollState
import hunoia.luno.ui.screen.home.HomeVM
import hunoia.luno.ui.screen.home.MiniWindowSettingsContent
import hunoia.luno.ui.screen.home.UiState

@Composable
fun MiniWindowSettingsSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    uiState: UiState,
    vm: HomeVM
) {
    if (!show) return
    val scrollState = rememberScrollState()
    OptimizedBottomSheet(
        onDismissRequest = onDismiss,
        scrollState = OptimizedScrollState.Scroll(scrollState)
    ) {
        BottomSheetNestedContent(scrollState = OptimizedScrollState.Scroll(scrollState)) {
            MiniWindowSettingsContent(uiState = uiState, vm = vm)
        }
    }
}
