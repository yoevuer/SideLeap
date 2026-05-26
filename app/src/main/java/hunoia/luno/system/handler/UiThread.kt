package hunoia.luno.system.handler

import android.os.Looper

fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()
