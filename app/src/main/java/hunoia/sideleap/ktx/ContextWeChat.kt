package hunoia.sideleap.ktx

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import hunoia.sideleap.R
import hunoia.sideleap.constant.WECHAT_PACKAGE
import hunoia.sideleap.utils.showToast

fun Context.gotoWechat(): Boolean {
    return try {
        val intent = Intent().apply {
            setAction("com.tencent.mm.action.BIZSHORTCUT")
            addCategory(Intent.CATEGORY_DEFAULT)
            setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        true
    } catch (exception: Exception) {
        showToast(R.string.goto_wechat_failed)
        false
    }
}

fun Context.gotoWechatScan(): Boolean {
    val intent = packageManager.getLaunchIntentForPackage(WECHAT_PACKAGE)
    return if (intent != null &&
        packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
    ) {
        intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
        intent.setAction("android.intent.action.VIEW")
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
            true
        } catch (exception: Exception) {
            showToast(R.string.goto_wechat_failed)
            false
        }
    } else {
        showToast(R.string.goto_wechat_failed)
        false
    }
}