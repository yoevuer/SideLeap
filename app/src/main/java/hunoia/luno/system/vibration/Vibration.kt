package hunoia.luno.system.vibration

import android.Manifest.permission.VIBRATE
import android.content.Context
import androidx.annotation.RequiresPermission

internal var appContext: Context? = null

fun initVibrationContext(context: Context) {
    appContext = context
}


