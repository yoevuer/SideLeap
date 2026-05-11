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
import hunoia.sideleap.utils.MiniWindowUtils
import hunoia.sideleap.utils.LauncherDiagnostics
import hunoia.sideleap.utils.showToast
import hunoia.sideleap.utils.showVersionTooLowToast

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
    val ctx = this
    LauncherDiagnostics.d(ctx, "launchApp: pkg=$packageName cls=$className miniWindow=$miniWindow")
    return try {
        var launchMiniWindowSucceed = false
        if (miniWindow) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                launchMiniWindowSucceed = launchAppInPopup(packageName, className)
            } else {
                showVersionTooLowToast(ctx)
            }
        }
        if (!launchMiniWindowSucceed) {
            if (className.isEmpty()) {
                packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    LauncherDiagnostics.d(ctx, "launchApp: using launchIntent for frozen app flags=$flags")
                    startActivity(this)
                } ?: throw Exception("No activity found")
            } else {
                val intent = Intent().apply {
                    setClassName(packageName, className)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(packageManager) == null) {
                    packageManager.getLaunchIntentForPackage(packageName)?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        LauncherDiagnostics.d(ctx, "launchApp: using launchIntent flags=$flags")
                        startActivity(this)
                    } ?: throw Exception("No activity found")
                } else {
                    LauncherDiagnostics.d(this, "launchApp: using direct intent flags=${intent.flags}")
                    startActivity(intent)
                }
            }
        }
        true
    } catch (e: Exception) {
        LauncherDiagnostics.d(this, "launchApp: failed pkg=$packageName cls=$className")
        if (!miniWindow) {
            showToast(R.string.launch_app_failed)
        }
        false
    }
}

@RequiresApi(Build.VERSION_CODES.N)
fun Context.launchAppInPopup(packageName: String, className: String): Boolean {
    val componentName = ComponentName.createRelative(packageName, className)
    return MiniWindowUtils.startActivity(this, componentName)
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

fun Context.launchUrl(url: String): Boolean {
    return try {
        val trimmedUrl = url.trim()
        val normalizedUrl = if (trimmedUrl.contains("://") || trimmedUrl.startsWith("intent:")) {
            trimmedUrl
        } else {
            "https://$trimmedUrl"
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