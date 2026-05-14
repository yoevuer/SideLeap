package hunoia.sideleap.utils

import android.content.Context
import hunoia.sideleap.freeze.FreezeAction
import hunoia.sideleap.freeze.OneKeyFreezeResult

object FrozenAppActionUtils {

    suspend fun oneKeyFreeze(context: Context): OneKeyFreezeResult {
        return FreezeAction.oneKeyFreeze(context)
    }
}
