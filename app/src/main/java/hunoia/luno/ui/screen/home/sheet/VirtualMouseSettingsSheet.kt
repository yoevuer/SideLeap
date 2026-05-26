package hunoia.luno.ui.screen.home.sheet

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.ui.component.BottomSheetNestedContent
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.component.OptimizedScrollState
import hunoia.luno.ui.screen.home.HomeVM
import hunoia.luno.ui.screen.home.VirtualMouseSettingsContent

@Composable
fun VirtualMouseSettingsSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    virtualMouse: GestureSettings.VirtualMouse,
    vm: HomeVM
) {
    if (!show) return
    val scrollState = rememberScrollState()
    OptimizedBottomSheet(
        onDismissRequest = onDismiss,
        scrollState = OptimizedScrollState.Scroll(scrollState)
    ) {
        BottomSheetNestedContent(scrollState = OptimizedScrollState.Scroll(scrollState)) {
            VirtualMouseSettingsContent(virtualMouse = virtualMouse, vm = vm)
        }
    }
}
