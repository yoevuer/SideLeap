package hunoia.sideleap.system.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import hunoia.sideleap.SideGestureService
import com.blankj.utilcode.util.ScreenUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@RequiresApi(Build.VERSION_CODES.R)
suspend fun SideGestureService.takeScreenshot(): Bitmap? = suspendCancellableCoroutine { cont ->
    takeScreenshot(0, applicationContext.mainExecutor, object : AccessibilityService.TakeScreenshotCallback {
        override fun onSuccess(screenshotResult: AccessibilityService.ScreenshotResult) {
            val value = Bitmap.wrapHardwareBuffer(screenshotResult.hardwareBuffer, screenshotResult.colorSpace)
            var bmp: Bitmap? = null
            if (value != null) {
                bmp = Bitmap.createBitmap(value, 0, 0, ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight())
            }
            cont.resume(bmp)
        }

        override fun onFailure(errorCode: Int) {
            cont.resume(null)
        }
    })
}