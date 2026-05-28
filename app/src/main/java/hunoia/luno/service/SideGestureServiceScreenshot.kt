package hunoia.luno.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import hunoia.luno.bridge.DensityProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun SideGestureService.takeScreenshot(): Bitmap? = suspendCancellableCoroutine { cont ->
    takeScreenshot(0, applicationContext.mainExecutor, object : AccessibilityService.TakeScreenshotCallback {
        override fun onSuccess(screenshotResult: AccessibilityService.ScreenshotResult) {
            val value = Bitmap.wrapHardwareBuffer(screenshotResult.hardwareBuffer, screenshotResult.colorSpace)
            var bmp: Bitmap? = null
            if (value != null) {
                bmp = Bitmap.createBitmap(value, 0, 0, DensityProvider.screenWidthPx, DensityProvider.screenHeightPx)
            }
            cont.resume(bmp)
        }

        override fun onFailure(errorCode: Int) {
            cont.resume(null)
        }
    })
}
