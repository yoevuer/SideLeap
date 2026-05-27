package hunoia.luno.system.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Build
import androidx.annotation.RequiresApi

object GlobalAction {

    fun lockScreen(service: AccessibilityService?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN) ?: false
    }

    fun powerDialog(service: AccessibilityService?): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) ?: false
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun takeScreenshot(service: AccessibilityService?): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT) ?: false
    }

    fun toggleSplitScreen(service: AccessibilityService?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN) ?: false
    }
}
