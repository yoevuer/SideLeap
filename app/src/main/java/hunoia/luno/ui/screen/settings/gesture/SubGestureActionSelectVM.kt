package hunoia.luno.ui.screen.settings.gesture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.App
import hunoia.luno.R
import hunoia.luno.action.Action
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.definition.ActionCatalog
import hunoia.luno.action.payload.SubGestureActionData
import hunoia.luno.core.serialization.JsonHelper
import hunoia.luno.gesture.SubGestureDirection
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.launcher.model.LauncherInfo
import hunoia.luno.launcher.query.AppQuery
import hunoia.luno.launcher.query.ShortcutQuery
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.settings.model.SubGesture
import hunoia.luno.ui.screen.actionselect.ActionSelectVM.UiState.SelectedRecord
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

    override val initialState: UiState = UiState(title = direction.displayName)

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
                AppQuery.queryLauncherActivities(App.getContext())
            }
            updateUiState { it.copy(apps = apps) }
        }
    }

    fun updateShortcutInfos() {
        viewModelScope.launchWithLoading {
            val createShortcuts = withContext(Dispatchers.IO) {
                ShortcutQuery.getAllAppsWithShortcut(App.getContext())
            }
            updateUiState { it.copy(createShortcuts = createShortcuts) }
        }
    }

    fun toggleMiniWindow(appInfo: AppInfo) {}

    fun done() {
        viewModelScope.launch {
            val selectedList = uiState.selectedRecord.list.filterIsInstance<Action>()
            val action = selectedList.lastOrNull()
            SettingsProvider.updateSubGestureSettings { settings ->
                settings.copy(
                    subGestures = settings.subGestures.map { gesture ->
                        if (gesture.id == subGestureId) gesture.withAction(direction, action)
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
                    value = GlobalActions.EXTRA_LAUNCH_APP,
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
                    value = GlobalActions.EXTRA_LAUNCH_SHORTCUT,
                    data = JsonHelper.encodeToString(shortcutInfo)
                ))
            } else emptyList()
            it.copy(selectedRecord = SelectedRecord(list))
        }
        if (selected) done()
    }

    private fun assembleActions() {
        viewModelScope.launch {
            val settings = SettingsProvider.getSubGestureSettings()
            val currentAction = settings.subGestures
                .find { it.id == subGestureId }
                ?.actionFor(direction)

            val allActions = ActionCatalog.definitions
                .filter { it.isDisplayed }
                .map { it.toAction() }
                .toMutableList()

            val enabledSubGestures = settings.subGestures.filter { it.enabled && it.id != subGestureId }
            enabledSubGestures.forEach { subGesture ->
                allActions.add(
                    Action(
                        value = GlobalActions.SUB_GESTURE,
                        data = JsonHelper.encodeToString(SubGestureActionData(id = subGesture.id))
                    )
                )
            }

            val selectedList = if (currentAction != null && currentAction.value != GlobalActions.NONE) {
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

    private companion object {
        private val SubGestureDirection.displayName: String get() = when (this) {
            SubGestureDirection.Up -> "上"
            SubGestureDirection.Down -> "下"
            SubGestureDirection.Left -> "左"
            SubGestureDirection.Right -> "右"
            SubGestureDirection.UpRight -> "右上"
            SubGestureDirection.DownRight -> "右下"
            SubGestureDirection.DownLeft -> "左下"
            SubGestureDirection.UpLeft -> "左上"
        }
    }
}
