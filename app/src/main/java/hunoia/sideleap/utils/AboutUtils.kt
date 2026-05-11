package hunoia.sideleap.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/7
 */
object AboutUtils {

    fun openGithubRepo(context: Context) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Hunoia/SideLeap")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun openOriginalProject(context: Context) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/aaronzzx/gulugulu/releases")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
