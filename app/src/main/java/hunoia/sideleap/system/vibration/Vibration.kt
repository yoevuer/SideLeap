package hunoia.sideleap.system.vibration

import android.Manifest.permission.VIBRATE
import android.content.Context
import androidx.annotation.RequiresPermission

private var appContext: Context? = null

fun initVibrationContext(context: Context) {
    appContext = context
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForSlide() {
    val ctx = appContext ?: return
    if (slideEnabled) {
        vibrate(ctx, this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForLongSlide() {
    val ctx = appContext ?: return
    if (longSlideEnabled) {
        vibrate(ctx, this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForActionPanel() {
    val ctx = appContext ?: return
    if (actionPanelEnabled) {
        vibrate(ctx, this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForMoveScreen() {
    val ctx = appContext ?: return
    if (moveScreenEnabled) {
        vibrate(ctx, this)
    }
}
