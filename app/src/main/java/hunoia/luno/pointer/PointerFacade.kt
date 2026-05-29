package hunoia.luno.pointer

import android.content.Context
import androidx.compose.ui.geometry.Offset
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.GestureSettings
import kotlinx.coroutines.CoroutineScope

object PointerFacade {

    var runtimeProvider: (() -> PointerRuntime?)? = null

    fun isActive(): Boolean = runtimeProvider?.invoke()?.isActive == true

    fun show(continuousModeOverride: Boolean? = null) {
        runtimeProvider?.invoke()?.show(continuousModeOverride)
    }

    fun end() {
        runtimeProvider?.invoke()?.end()
    }

    fun clampPosition(position: Offset): Offset = clampPointerPosition(position)

    fun toggleContinuousMode() {
        val runtime = runtimeProvider?.invoke() ?: return
        val current = runtime.getCurrentSettings() ?: return
        runtime.onSettingsUpdate(current.copy(continuousMode = !current.continuousMode))
    }

    fun pointerContinuousModeOverride(action: Action): Boolean? =
        action.pointerContinuousModeOverride()
}
