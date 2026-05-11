package hunoia.sideleap.ktx

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/1
 */

val LocalNavController = staticCompositionLocalOf<NavController> { error("No NavController provided") }