package hunoia.sideleap.ui.screen.unlock

import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.ui.screen.unlock.UnlockVM.UiEvent
import hunoia.sideleap.ui.screen.unlock.UnlockVM.UiState

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/23
 */
class UnlockVM : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState()

    data class UiState(val value: Int = 0)

    sealed interface UiEvent
}