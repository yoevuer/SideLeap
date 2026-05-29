package hunoia.luno.ui.screen.home.sheet

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.ui.component.BottomSheetNestedContent
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.component.OptimizedScrollState
import hunoia.luno.ui.screen.home.HomeVM
import hunoia.luno.ui.screen.home.PointerSettingsContent

@Composable
fun PointerSettingsSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    pointer: GestureSettings.Pointer,
    vm: HomeVM
) {
    if (!show) return
    val scrollState = rememberScrollState()
    OptimizedBottomSheet(
        onDismissRequest = onDismiss,
        scrollState = OptimizedScrollState.Scroll(scrollState)
    ) {
        BottomSheetNestedContent(scrollState = OptimizedScrollState.Scroll(scrollState)) {
            PointerSettingsContent(pointer = pointer, vm = vm, scrollState = scrollState)
        }
    }
}
