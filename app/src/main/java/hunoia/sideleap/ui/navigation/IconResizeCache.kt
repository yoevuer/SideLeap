package hunoia.sideleap.ui.navigation

import android.graphics.drawable.Drawable
import java.util.Collections
import java.util.LinkedHashMap

object IconResizeCache {

    private const val MAX_ICON_CACHE_SIZE = 200

    val iconCache: MutableMap<String, Drawable> = Collections.synchronizedMap(
        object : LinkedHashMap<String, Drawable>(MAX_ICON_CACHE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Drawable>?): Boolean {
                return size > MAX_ICON_CACHE_SIZE
            }
        }
    )

    val iconBgColorCache: MutableMap<String, Int> = Collections.synchronizedMap(
        object : LinkedHashMap<String, Int>(MAX_ICON_CACHE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Int>?): Boolean {
                return size > MAX_ICON_CACHE_SIZE
            }
        }
    )
}