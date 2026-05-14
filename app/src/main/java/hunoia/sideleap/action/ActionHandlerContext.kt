package hunoia.sideleap.action

import android.content.Context
import hunoia.sideleap.SideGestureService
import hunoia.sideleap.settings.model.ActionSettings
import kotlinx.coroutines.CoroutineScope

data class ActionHandlerContext(
    val service: SideGestureService,
    val appContext: Context,
    val scope: CoroutineScope,
    val actionSettings: ActionSettings,
    val showToast: (String) -> Unit,
    val showLongToast: (String) -> Unit,
    val currentPackageName: () -> String? = { null },
    val toggleKeepScreenOn: () -> Unit = {},
    val showVersionTooLowToast: (Int) -> Unit = {},
    val previousApp: suspend () -> Unit = {},
)
