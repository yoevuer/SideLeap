package hunoia.luno.bridge.intent

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import hunoia.luno.R
import hunoia.luno.bridge.feedback.showToast
import android.os.Build

object KeepAliveHelper {

    fun gotoSettings(context: Context) {
        try {
            val manufacturer = Build.MANUFACTURER
            val brand = Build.BRAND
            when {
                manufacturer.equals("Huawei", ignoreCase = true) || brand.equals("Huawei", ignoreCase = true) -> gotoHuaweiSetting(context)
                manufacturer.equals("Xiaomi", ignoreCase = true) -> gotoXiaomiSetting(context)
                manufacturer.equals("Oppo", ignoreCase = true) -> gotoOPPOSetting(context)
                manufacturer.equals("Vivo", ignoreCase = true) -> gotoVIVOSetting(context)
                manufacturer.equals("Smartisan", ignoreCase = true) -> gotoSmartisanSetting(context)
                manufacturer.equals("Samsung", ignoreCase = true) || brand.equals("Samsung", ignoreCase = true) -> gotoSamsungSetting(context)
                manufacturer.equals("Meizu", ignoreCase = true) -> gotoMeizuSetting(context)
                else -> showToast(R.string.please_enable_launch_self_permission_by_yourself)
            }
        } catch (ignored: Exception) {
            showToast(R.string.please_enable_launch_self_permission_by_yourself)
        }
    }

    private fun gotoSmartisanSetting(context: Context) {
        showActivity(context, "com.smartisanos.security")
    }

    private fun gotoSamsungSetting(context: Context) {
        try {
            showActivity(context, "com.samsung.android.sm_cn")
        } catch (e: java.lang.Exception) {
            showActivity(context, "com.samsung.android.sm")
        }
    }

    private fun gotoMeizuSetting(context: Context) {
        showActivity(context, "com.meizu.safe")
    }

    private fun gotoVIVOSetting(context: Context) {
        showActivity(context, "com.iqoo.secure")
    }

    private fun gotoOPPOSetting(context: Context) {
        try {
            showActivity(context, "com.coloros.phonemanager")
        } catch (e1: java.lang.Exception) {
            try {
                showActivity(context, "com.oppo.safe")
            } catch (e2: java.lang.Exception) {
                try {
                    showActivity(context, "com.coloros.oppoguardelf")
                } catch (e3: java.lang.Exception) {
                    showActivity(context, "com.coloros.safecenter")
                }
            }
        }
    }

    private fun gotoXiaomiSetting(context: Context) {
        showActivity(
            context,
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
    }

    private fun gotoHuaweiSetting(context: Context) {
        try {
            showActivity(
                context,
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        } catch (e: Exception) {
            showActivity(
                context,
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.bootstart.BootStartActivity"
            )
        }
    }

    private fun showActivity(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        }
    }

    private fun showActivity(context: Context, packageName: String, activityDir: String) {
        val intent = Intent().apply {
            setComponent(ComponentName(packageName, activityDir))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}