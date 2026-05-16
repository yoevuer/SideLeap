package hunoia.sideleap.system.intent

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import hunoia.sideleap.R
import hunoia.sideleap.system.api.showToast
import com.blankj.utilcode.util.RomUtils

object KeepAliveHelper {

    fun gotoSettings(context: Context) {
        try {
            when {
                RomUtils.isHuawei() -> gotoHuaweiSetting(context)
                RomUtils.isXiaomi() -> gotoXiaomiSetting(context)
                RomUtils.isOppo() -> gotoOPPOSetting(context)
                RomUtils.isVivo() -> gotoVIVOSetting(context)
                RomUtils.isSmartisan() -> gotoSmartisanSetting(context)
                RomUtils.isSamsung() -> gotoSamsungSetting(context)
                RomUtils.isMeizu() -> gotoMeizuSetting(context)
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