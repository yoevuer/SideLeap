package hunoia.sideleap.launcher.launch

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.net.Uri
import androidx.annotation.RequiresApi
import hunoia.sideleap.R
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.model.LauncherInfo
import hunoia.sideleap.action.OpenAppOrUrlData
import hunoia.sideleap.system.feedback.showToast
import com.blankj.utilcode.util.ScreenUtils
import kotlin.math.roundToInt

object Launcher {

    fun launchApp(context: Context, packageName: String, className: String, miniWindow: Boolean): Boolean {
        if (miniWindow && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return launchAppInPopup(context, packageName, className)
        }
        return try {
            val intent = Intent().apply {
                setClassName(packageName, className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (context.packageManager.resolveActivity(intent, 0) == null) {
                val fallback = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
                context.startActivity(fallback)
                return true
            }
            context.startActivity(intent)
            true
        } catch (ignored: Exception) {
            false
        }
    }

    fun launchAppActivity(context: Context, packageName: String, className: String): Boolean {
        return try {
            val component = ComponentName.createRelative(packageName, className)
            val intent = Intent().apply {
                setComponent(component)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (context.packageManager.resolveActivity(intent, 0) == null) return false
            context.startActivity(intent)
            true
        } catch (ignored: Exception) {
            false
        }
    }

    fun launchAppInfo(context: Context, appInfo: AppInfo, miniWindow: Boolean): Boolean {
        return launchApp(context, appInfo.packageName, appInfo.className, miniWindow)
    }

    fun launchShortcutInfo(context: Context, shortcutInfo: LauncherInfo.ShortcutInfo): Boolean {
        return try {
            val intents = shortcutInfo.intents.map {
                Intent.parseUri(it, Intent.URI_INTENT_SCHEME).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }.toTypedArray()
            context.startActivities(intents)
            true
        } catch (ignored: Exception) {
            showToast(context.getString(R.string.launch_shortcut_info_failed, shortcutInfo.label))
            false
        }
    }

    fun launchOpenAppOrUrl(context: Context, data: OpenAppOrUrlData): Boolean {
        return when (data.type) {
            OpenAppOrUrlData.TYPE_ACTIVITY -> {
                if (data.packageName.isBlank() || data.activityClassName.isBlank()) {
                    showToast(context.getString(R.string.launch_failed))
                    false
                } else {
                    launchAppActivity(context, data.packageName, data.activityClassName)
                }
            }
            OpenAppOrUrlData.TYPE_URL -> launchUrl(context, data.url)
            else -> {
                showToast(context.getString(R.string.launch_failed))
                false
            }
        }
    }

    fun launchUrl(context: Context, url: String): Boolean {
        return try {
            val normalizedUrl = normalizeOpenAppOrUrl(url) ?: run {
                showToast(context.getString(R.string.invalid_url))
                return false
            }
            if (normalizedUrl.startsWith("intent:")) {
                val intent = Intent.parseUri(normalizedUrl, Intent.URI_INTENT_SCHEME).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(context.packageManager) == null) {
                    showToast(context.getString(R.string.launch_failed))
                    return false
                }
                context.startActivity(intent)
                return true
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) == null) {
                showToast(context.getString(R.string.launch_failed))
                return false
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            showToast(context.getString(R.string.launch_failed))
            false
        }
    }

    fun normalizeOpenAppOrUrl(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return null
        if (trimmed.startsWith("intent:")) {
            return runCatching { Intent.parseUri(trimmed, Intent.URI_INTENT_SCHEME); trimmed }.getOrNull()
        }
        val hasExplicitScheme = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:").containsMatchIn(trimmed)
        val candidate = if (hasExplicitScheme || trimmed.contains("://")) trimmed else "https://$trimmed"
        val uri = runCatching { Uri.parse(candidate) }.getOrNull() ?: return null
        return if (uri.scheme.isNullOrBlank()) null else candidate
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun launchAppInPopup(context: Context, packageName: String, className: String): Boolean {
        return MiniWindow.startActivity(context, ComponentName.createRelative(packageName, className))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun isMiniWindowSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT)
    }
}

@RequiresApi(Build.VERSION_CODES.N)
private object MiniWindow {

    fun startActivity(context: Context, component: ComponentName): Boolean {
        return try {
            val intent = Intent().apply {
                setComponent(component)
                setAction(Intent.ACTION_MAIN)
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val activityOptions = getActivityOptions()
            context.startActivity(intent, activityOptions.toBundle())
            true
        } catch (ignored: Exception) {
            showToast(context.getString(R.string.launch_mini_window_failed))
            false
        }
    }

    private fun getActivityOptions(): ActivityOptions {
        val brand = Build.BRAND.lowercase()
        return when (brand) {
            "huawei", "honor" -> makeActivityOptions(102)
            "oppo", "oneplus", "realme" -> makeActivityOptions(100)
            else -> makeActivityOptions(5)
        }
    }

    private fun makeActivityOptions(mode: Int): ActivityOptions {
        return ActivityOptions.makeBasic().also {
            try {
                val method = ActivityOptions::class.java.getMethod(
                    "setLaunchWindowingMode", Int::class.javaPrimitiveType
                )
                method.invoke(it, mode)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val screenWidth = ScreenUtils.getScreenWidth()
            val screenHeight = ScreenUtils.getScreenHeight()
            var bounds: Rect? = null
            if (mode == 5) {
                val width = screenWidth
                val scaledWidth = width * 0.7f
                val left = ((screenWidth - scaledWidth) / 2f).roundToInt()
                val right = left + width
                val height = (width / 0.625f).roundToInt()
                val top = (screenHeight - height) / 2
                val bottom = top + height
                bounds = Rect(left, top, right, bottom)
            }
            it.setLaunchBounds(bounds)
        }
    }
}
