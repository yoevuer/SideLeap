package hunoia.sideleap.ktx

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import hunoia.sideleap.R
import hunoia.sideleap.entity.AppInfo
import hunoia.sideleap.entity.LauncherInfo
import hunoia.sideleap.action.OpenAppOrUrlData
import hunoia.sideleap.freeze.FreezeLaunch
import hunoia.sideleap.launcher.launch.Launcher
import hunoia.sideleap.utils.LauncherDiagnostics
import hunoia.sideleap.system.feedback.showToast
import hunoia.sideleap.system.feedback.showVersionTooLowToast

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

fun Context.launchShortcutInfo(shortcutInfo: LauncherInfo.ShortcutInfo): Boolean {
    return try {
        val intents = shortcutInfo
            .intents
            .map {
                Intent.parseUri(it, Intent.URI_INTENT_SCHEME).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            .toTypedArray()
        startActivities(intents)
        true
    } catch (ignored: Exception) {
        showToast(getString(R.string.launch_shortcut_info_failed, shortcutInfo.label))
        false
    }
}

fun Context.launchAppInfo(appInfo: AppInfo, miniWindow: Boolean = appInfo.miniWindow): Boolean {
    val launchSucceed = launchApp(appInfo.packageName, appInfo.className, miniWindow)
    if (!launchSucceed) {
        showToast(getString(R.string.launch_app_info_failed, appInfo.label))
    }
    return launchSucceed
}

fun Context.launchApp(packageName: String, className: String, miniWindow: Boolean = false): Boolean {
    return Launcher.launchApp(this, packageName, className, miniWindow)
}

suspend fun Context.launchAppWithAutoUnfreeze(
    packageName: String,
    className: String,
    miniWindow: Boolean = false,
    unfreezePackage: suspend (context: Context, packageName: String) -> Boolean = { _, _ -> true }
): Boolean {
    return FreezeLaunch.launchWithAutoUnfreeze(this, packageName, className, miniWindow, unfreezePackage)
}

suspend fun Context.launchAppActivityWithAutoUnfreeze(
    packageName: String,
    className: String,
    unfreezePackage: suspend (context: Context, packageName: String) -> Boolean = { _, _ -> true }
): Boolean {
    return FreezeLaunch.launchActivityWithAutoUnfreeze(this, packageName, className, unfreezePackage)
}

@RequiresApi(Build.VERSION_CODES.N)
fun Context.launchAppInPopup(packageName: String, className: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Launcher.launchAppInPopup(this, packageName, className)
        } else {
            showVersionTooLowToast(this, R.string.action_popup_screen)
            return false
        }
    }

fun Context.launchAppActivity(packageName: String, className: String): Boolean {
    return try {
        val trimmedPackage = packageName.trim()
        val trimmedClass = className.trim()
        val intent = Intent().apply {
            component = ComponentName.createRelative(trimmedPackage, trimmedClass)
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

fun Context.launchOpenAppOrUrl(data: OpenAppOrUrlData): Boolean {
    return when (data.type) {
        OpenAppOrUrlData.TYPE_ACTIVITY -> {
            if (data.packageName.isBlank() || data.activityClassName.isBlank()) {
                showToast(R.string.launch_failed)
                false
            } else {
                launchAppActivity(data.packageName, data.activityClassName)
            }
        }

        OpenAppOrUrlData.TYPE_URL -> launchUrl(data.url)
        else -> {
            showToast(R.string.launch_failed)
            false
        }
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
