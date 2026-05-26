package hunoia.luno.service

import android.view.View
import android.view.WindowManager
import hunoia.luno.SideGestureService
import hunoia.luno.gesture.GestureButton
import hunoia.luno.settings.model.AdvancedSettings
import hunoia.luno.settings.model.InitialSettings
import hunoia.luno.system.window.setFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class SideGestureButtonRefreshCoordinator(
    private val host: SideGestureService,
    private val scopeProvider: () -> CoroutineScope,
    private val initialSettingsProvider: suspend () -> InitialSettings,
    private val advancedSettingsProvider: () -> AdvancedSettings?,
    private val buttonViewsProvider: () -> List<View>?,
    private val runtimeStateProvider: () -> SideGestureRuntimeState,
) {

    fun refresh() {
        scopeProvider().launch {
            val initialSettings = initialSettingsProvider()
            val advancedSettings = advancedSettingsProvider() ?: return@launch
            val refreshState = GestureButtonRefreshState(
                initialSettings = initialSettings,
                advancedSettings = advancedSettings,
                runtimeState = runtimeStateProvider(),
            )
            buttonViewsProvider()?.forEach { view ->
                val button = view.tag as? GestureButton ?: return@forEach
                val lp = (view.layoutParams as WindowManager.LayoutParams).apply {
                    updateGestureButtonState(this, view, button, refreshState)
                }
                host.updateWindowLayout(view, lp)
            }
        }
    }

    private fun updateGestureButtonState(
        lp: WindowManager.LayoutParams,
        view: View,
        button: GestureButton,
        state: GestureButtonRefreshState,
    ) {
        lp.updateGestureButton(button)
        lp.setFlags(state.shouldShow(button))
    }
}
