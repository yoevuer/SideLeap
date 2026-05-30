package hunoia.luno.ktx

import android.content.Context
import hunoia.luno.shizuku.ShizukuManager
import hunoia.luno.shizuku.ShizukuStatus

fun Context.shizukuStatusLabel(status: ShizukuStatus): String {
    return when {
        status.permissionGranted -> "Shizuku Ready"
        status.binderAlive -> "Shizuku: No Permission"
        status.installed -> "Shizuku: Not Running"
        else -> "Shizuku: Not Installed"
    }
}

fun Context.shizukuStatusSummary(status: ShizukuStatus): String {
    return "${shizukuStatusLabel(status)} | ${status.executorLabel}"
}
