package hunoia.sideleap.ktx

import android.content.Context
import android.content.Intent
import android.net.Uri
import hunoia.sideleap.R
import hunoia.sideleap.utils.showToast

fun Context.gotoAlipayScan(): Boolean {
    return try {
        val uri = Uri.parse("alipayqr://platformapi/startapp?saId=10000007")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        true
    } catch (e: Exception) {
        showToast(R.string.goto_alipay_failed)
        false
    }
}

fun Context.gotoAlipayPayCode(): Boolean {
    return try {
        val uri = Uri.parse("alipayqr://platformapi/startapp?saId=20000056")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        true
    } catch (e: Exception) {
        showToast(R.string.goto_alipay_failed)
        false
    }
}