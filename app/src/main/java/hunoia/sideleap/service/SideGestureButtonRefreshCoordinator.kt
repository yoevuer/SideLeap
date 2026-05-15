package hunoia.sideleap.service

import android.view.View
import android.view.WindowManager
import androidx.core.view.postDelayed
import hunoia.sideleap.SideGestureService
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.settings.model.InitialSettings
import hunoia.sideleap.system.window.setFlags
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
        if (button.position != Position.Bottom) {
            lp.y += -state.runtimeState.imePadding
        }
        updateTemporaryHideClickListener(view, state.advancedSettings.hideTemporary)
        lp.setFlags(state.shouldShow(button))
    }

    private fun updateTemporaryHideClickListener(view: View, enabled: Boolean) {
        if (enabled) {
            view.setOnClickListener { v ->
                val lp = v.layoutParams as WindowManager.LayoutParams
                lp.setFlags(false)
                host.updateWindowLayout(v, lp)
                v.postDelayed(1000) {
                    val lp2 = v.layoutParams as WindowManager.LayoutParams
                    val gestureButton = (view.tag as? GestureButton)?.enabled == true
                    lp2.setFlags(gestureButton)
                    host.updateWindowLayout(v, lp2)
                }
            }
        } else {
            view.setOnClickListener(null)
        }
    }
}
