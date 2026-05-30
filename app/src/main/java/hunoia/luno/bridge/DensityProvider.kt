package hunoia.luno.bridge

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

object DensityProvider {

    private var displayMetrics: DisplayMetrics? = null

    fun init(context: Context) {
        displayMetrics = context.resources.displayMetrics
    }

    val density: Float get() = displayMetrics?.density ?: 1f

    val densityDpi: Int get() = displayMetrics?.densityDpi ?: DisplayMetrics.DENSITY_DEFAULT

    val screenWidthPx: Int get() = displayMetrics?.widthPixels ?: 0

    val screenHeightPx: Int get() = displayMetrics?.heightPixels ?: 0

    fun dp2px(dp: Float): Int = (dp * density).roundToInt()

    fun dp2px(dp: Int): Int = (dp * density).roundToInt()
}
