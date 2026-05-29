package hunoia.luno.ui.screen.settings.gesture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.core.AppContext
import hunoia.luno.R
import hunoia.luno.config.model.Action
import hunoia.luno.action.api.ActionFacade

import hunoia.luno.action.payload.SubGestureActionData
import hunoia.luno.core.JsonHelper
import hunoia.luno.config.model.SubGestureDirection
import hunoia.luno.quicklaunch.QuickLaunchFacade
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.config.ConfigProvider
import hunoia.luno.config.model.SubGesture
import hunoia.luno.ui.screen.actionselect.UiState.SelectedRecord
import hunoia.luno.ui.screen.settings.gesture.SubGestureActionSelectVM.UiEvent
import hunoia.luno.ui.screen.settings.gesture.SubGestureActionSelectVM.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubGestureActionSelectVM(
    private val subGestureId: String,
    private val direction: SubGestureDirection
) : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState()

    val actionSettingsDialog = ActionSettingsDialog()

    init {
        assembleActions()
    }

    fun select(obj: Any, selected: Boolean) {
        if (obj is AppInfo) {
            selectAppInfo(obj, selected)
        } else if (obj is LauncherInfo.ShortcutInfo) {
            selectShortcutInfo(obj, selected)
        } else if (obj is Action) {
            selectAction(obj, selected)
        }
    }

    fun showOpenAppOrUrlDialog() {}
    fun showActionSettingsDialog(action: Action) {
        actionSettingsDialog.show(true, action)
    }

    fun updateAppInfos() {
        viewModelScope.launchWithLoading {
            val apps = withContext(Dispatchers.IO) {
                QuickLaunchFacade.queryApps(AppContext.get())
            }
            updateUiState { it.copy(apps = apps) }
        }
    }

    fun updateShortcutInfos() {
        viewModelScope.launchWithLoading {
            val createShortcuts = withContext(Dispatchers.IO) {
                QuickLaunchFacade.queryShortcuts(AppContext.get())
            }
            updateUiState { it.copy(createShortcuts = createShortcuts) }
        }
    }

    fun toggleMiniWindow(appInfo: AppInfo) {}

    fun done() {
        viewModelScope.launch {
            val selectedList = uiState.selectedRecord.list.filterIsInstance<Action>()
            val action = selectedList.lastOrNull()
            ConfigProvider.updateSubGestureSettings { settings ->
                settings.copy(
                    subGestures = settings.subGestures.map { gesture ->
                        if (gesture.id == subGestureId) gesture.withAction(direction, action?.value)
                        else gesture
                    }
                )
            }
        }.invokeOnCompletion {
            finish()
        }
    }

    private fun selectAction(action: Action, selected: Boolean) {
        updateUiState {
            val list = if (selected) listOf(action) else emptyList<Any>()
            it.copy(selectedRecord = SelectedRecord(list))
        }
        if (selected) done()
    }

    private fun selectAppInfo(appInfo: AppInfo, selected: Boolean) {
        updateUiState {
            val list = if (selected) {
                listOf(Action(
                    value = ActionFacade.EXTRA_LAUNCH_APP,
                    data = JsonHelper.encodeToString(appInfo)
                ))
            } else emptyList()
            it.copy(selectedRecord = SelectedRecord(list))
        }
        if (selected) done()
    }

    private fun selectShortcutInfo(shortcutInfo: LauncherInfo.ShortcutInfo, selected: Boolean) {
        updateUiState {
            val list = if (selected) {
                listOf(Action(
                    value = ActionFacade.EXTRA_LAUNCH_SHORTCUT,
                    data = JsonHelper.encodeToString(shortcutInfo)
                ))
            } else emptyList()
            it.copy(selectedRecord = SelectedRecord(list))
        }
        if (selected) done()
    }

    private fun assembleActions() {
        viewModelScope.launch {
            val settings = ConfigProvider.getSubGestureSettings()
            val currentAction = settings.subGestures
                .find { it.id == subGestureId }
                ?.actionFor(direction)

            val allActions = ActionFacade.definitions
                .filter { it.isDisplayed }
                .map { it.toAction() }
                .toMutableList()

            val enabledSubGestures = settings.subGestures.filter { it.enabled && it.id != subGestureId }
            enabledSubGestures.forEach { subGesture ->
                allActions.add(
                    Action(
                        value = ActionFacade.SUB_GESTURE,
                        data = JsonHelper.encodeToString(SubGestureActionData(id = subGesture.id))
                    )
                )
            }

            val selectedList = if (currentAction != null && currentAction != ActionFacade.NONE) {
                listOf(currentAction as Any)
            } else emptyList()

            updateUiState {
                it.copy(
                    actions = allActions,
                    subGestures = settings.subGestures,
                    selectedRecord = SelectedRecord(selectedList)
                )
            }
        }
    }

    data class UiState(
        val title: String = "",
        val actions: List<Action> = emptyList(),
        val subGestures: List<SubGesture> = emptyList(),
        val apps: List<AppInfo> = emptyList(),
        val createShortcuts: List<LauncherInfo> = emptyList(),
        val launchShortcuts: List<LauncherInfo> = emptyList(),
        val selectedRecord: SelectedRecord = SelectedRecord(),
        val actionSettingsDialog: ActionSettingsDialogValue = ActionSettingsDialogValue(false, Action.NONE),
    )

    inner class ActionSettingsDialog {

        fun show(show: Boolean, action: Action = Action.NONE) {
            updateUiState {
                it.copy(actionSettingsDialog = it.actionSettingsDialog.copy(show = show, action = action))
            }
        }
    }

    data class ActionSettingsDialogValue(
        val show: Boolean,
        val action: Action
    )

    sealed interface UiEvent

    class Factory(
        private val subGestureId: String,
        private val direction: SubGestureDirection
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubGestureActionSelectVM(subGestureId, direction) as T
        }
    }
}
