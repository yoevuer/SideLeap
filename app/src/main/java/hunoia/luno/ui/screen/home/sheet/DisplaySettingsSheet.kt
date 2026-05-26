package hunoia.luno.ui.screen.home.sheet

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import hunoia.luno.ui.component.BottomSheetNestedContent
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.component.OptimizedScrollState
import hunoia.luno.ui.screen.home.DisplaySettingsContent
import hunoia.luno.ui.screen.home.HomeVM

@Composable
fun DisplaySettingsSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    uiState: HomeVM.UiState,
    vm: HomeVM,
    onShowAnimationStyle: () -> Unit,
    onShowMiniWindowSettings: () -> Unit
) {
    if (!show) return
    val scrollState = rememberScrollState()
    OptimizedBottomSheet(
        onDismissRequest = onDismiss,
        scrollState = OptimizedScrollState.Scroll(scrollState)
    ) {
        BottomSheetNestedContent(scrollState = OptimizedScrollState.Scroll(scrollState)) {
            DisplaySettingsContent(
                uiState = uiState,
                vm = vm,
                showAnimationStyle = onShowAnimationStyle,
                showMiniWindowSettings = onShowMiniWindowSettings
            )
        }
    }
}
