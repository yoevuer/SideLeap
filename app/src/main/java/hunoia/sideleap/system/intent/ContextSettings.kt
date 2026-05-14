package hunoia.sideleap.system.intent

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import com.blankj.utilcode.util.AppUtils
import hunoia.sideleap.R
import hunoia.sideleap.system.feedback.showToast

fun Context.gotoIgnoreBatteryOptimizations(): Boolean {
    return try {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        true
    } catch (ignored: Exception) {
        showToast(R.string.please_enable_ignoring_battery_optimizations_by_yourself)
        false
    }
}

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(packageName)
}

fun Context.gotoAccessibilitySettings() {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    try {
        startActivity(intent)
    } catch (ignored: Exception) {
        intent.action = Settings.ACTION_SETTINGS
        startActivity(intent)
    }
}

fun Context.gotoOverlaySettings() {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
        data = Uri.parse("package:${packageName}")
    }
    try {
        startActivity(intent)
    } catch (ignored: Exception) {
        intent.action = Settings.ACTION_SETTINGS
        startActivity(intent)
    }
}

fun Context.gotoAppDetailSettings() {
    AppUtils.launchAppDetailsSettings(packageName)
}

fun Context.isAccessibilitySettingsOn(clazz: Class<out AccessibilityService?>): Boolean {
    var accessibilityEnabled = false
    try {
        accessibilityEnabled = Settings.Secure.getInt(
            applicationContext.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED
        ) == 1
    } catch (e: Settings.SettingNotFoundException) {
        e.printStackTrace()
    }
    val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
    if (accessibilityEnabled) {
        val settingValue: String? = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (settingValue != null) {
            mStringColonSplitter.setString(settingValue)
            while (mStringColonSplitter.hasNext()) {
                val accessibilityService = mStringColonSplitter.next()
                if (accessibilityService.equals("${packageName}/${clazz.canonicalName}", ignoreCase = true))
                    return true
            }
        }
    }
    return false
}
