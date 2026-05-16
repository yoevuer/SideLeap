package hunoia.sideleap.system.api

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.blankj.utilcode.util.AppUtils
import hunoia.sideleap.R

fun Context.gotoAppDetailSettings() {
    AppUtils.launchAppDetailsSettings(packageName)
}

fun Context.launchAssist(): Boolean {
    return try {
        val intent = Intent().apply {
            setAction(Intent.ACTION_ASSIST)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        true
    } catch (ignored: Exception) {
        showToast(R.string.launch_assist_failed)
        false
    }
}

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

fun Context.launchUrl(url: String): Boolean {
    return try {
        val normalizedUrl = normalizeOpenAppOrUrl(url) ?: run {
            showToast(R.string.invalid_url)
            return false
        }
        if (normalizedUrl.startsWith("intent:")) {
            val intent = Intent.parseUri(normalizedUrl, Intent.URI_INTENT_SCHEME).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(packageManager) == null) {
                showToast(R.string.launch_failed)
                return false
            }
            startActivity(intent)
            return true
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(packageManager) == null) {
            showToast(R.string.launch_failed)
            return false
        }
        startActivity(intent)
        true
    } catch (e: Exception) {
        showToast(R.string.launch_failed)
        false
    }
}

fun normalizeOpenAppOrUrl(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null

    if (trimmed.startsWith("intent:")) {
        return runCatching {
            Intent.parseUri(trimmed, Intent.URI_INTENT_SCHEME)
            trimmed
        }.getOrNull()
    }

    val hasExplicitScheme = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:").containsMatchIn(trimmed)
    val candidate = if (hasExplicitScheme || trimmed.contains("://")) {
        trimmed
    } else {
        "https://$trimmed"
    }
    val uri = runCatching { Uri.parse(candidate) }.getOrNull() ?: return null
    return if (uri.scheme.isNullOrBlank()) null else candidate
}
