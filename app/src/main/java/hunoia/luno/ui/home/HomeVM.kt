package hunoia.luno.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.bridge.feedback.showToast
import hunoia.luno.config.ConfigProvider
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.resolveDisplayName
import hunoia.luno.config.model.SubGesture
import hunoia.luno.config.SubGestureCleaner
import hunoia.luno.core.AppContext
import hunoia.luno.R
import hunoia.luno.shizuku.ShizukuManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeVM : HomeVMBase() {

    override val initialState: UiState = UiState()

    init {
        loadData()
        loadFrozenCount()
        viewModelScope.launch {
            ShizukuManager.autoRequestPermissionIfNeeded()
            ShizukuManager.ensureWriteSecureSettings()
        }
    }

    fun backup(context: Context, saveTo: Uri) {
        viewModelScope.launchWithLoading(
            Dispatchers.IO + CoroutineExceptionHandler { _, _ ->
                toast(R.string.backup_failed)
            },
            cancelable = false
        ) {
            BackupService.backup(context, saveTo) { toast(it) }
        }
    }

    fun restore(context: Context, restoreFrom: Uri) {
        viewModelScope.launchWithLoading(
            Dispatchers.IO + CoroutineExceptionHandler { _, _ ->
                toast(R.string.restore_failed)
            },
            cancelable = false
        ) {
            BackupService.restore(context, restoreFrom) { toast(it) }
        }
    }

    fun addSubGesture(id: String) {
        viewModelScope.launch {
            ConfigProvider.updateSubGestureSettings { settings ->
                val newGesture = SubGesture(
                    id = id,
                    name = AppContext.get().getString(R.string.sub_gesture_default_name, settings.subGestures.size + 1),
                    color = android.graphics.Color.argb(255, kotlin.random.Random.nextInt(256), kotlin.random.Random.nextInt(256), kotlin.random.Random.nextInt(256))
                )
                settings.copy(subGestures = settings.subGestures + newGesture)
            }
        }
    }

    fun onSubGestureEnabledChange(gesture: SubGesture, enabled: Boolean) {
        updateUiState {
            val list = it.subGestures
            val index = list.indexOf(gesture)
            if (index < 0) it else {
                it.copy(subGestures = list.mapIndexed { i, g ->
                    if (i == index) g.copy(enabled = enabled) else g
                })
            }
        }
        saveSettings()
    }

    fun expandSubGestureList(expanded: Boolean, scrollOffset: Int = Int.MAX_VALUE) {
        updateUiState {
            it.copy(
                isSubGestureListExpanded = expanded,
                isBottomGestureButtonListExpanded = it.isBottomGestureButtonListExpanded && !expanded,
                isSideGestureButtonListExpanded = it.isSideGestureButtonListExpanded && !expanded
            )
        }
        if (expanded && scrollOffset != Int.MAX_VALUE) {
            sendUiEvent(UiEvent.ScrollToEvent(scrollOffset))
        }
    }

    fun addBottomGestureButton() {
        if (uiState.bottomGestureButtons.size >= 10) {
            toast(R.string.gesture_button_size_max)
            return
        }
        viewModelScope.launch {
            val maxNum = uiState.bottomGestureButtons.maxOfOrNull {
                parseNumberSuffix(it.name.ifEmpty { it.resolveDisplayName() })
            } ?: 0
            val name = AppContext.get().getString(R.string.bottom_gesture_button_name, maxNum + 1)
            ConfigProvider.updateBottomGestureButtons {
                it + GestureButton.createBottom(name = name)
            }
            delay(50)
            sendUiEvent(UiEvent.ScrollToBottom)
        }
    }

    fun addSideGestureButton() {
        if (uiState.sideGestureButtons.size >= 20) {
            toast(R.string.gesture_button_size_max)
            return
        }
        viewModelScope.launch {
            val maxNum = uiState.sideGestureButtons.chunked(2).maxOfOrNull { pair ->
                parseNumberSuffix(pair.first().name.ifEmpty { pair.first().resolveDisplayName() })
            } ?: 0
            val leftName = AppContext.get().getString(R.string.left_gesture_button_name, maxNum + 1)
            val rightName = AppContext.get().getString(R.string.right_gesture_button_name, maxNum + 1)
            ConfigProvider.updateSideGestureButtons {
                it + GestureButton.createSidePair(leftName = leftName, rightName = rightName)
            }
            delay(50)
            sendUiEvent(UiEvent.ScrollToBottom)
        }
    }

    fun expandBottomGestureButtonList(expanded: Boolean, scrollOffset: Int = Int.MAX_VALUE) {
        updateUiState {
            it.copy(
                isBottomGestureButtonListExpanded = expanded,
                isSideGestureButtonListExpanded = it.isSideGestureButtonListExpanded && !expanded,
                isSubGestureListExpanded = it.isSubGestureListExpanded && !expanded
            )
        }
        if (expanded && scrollOffset != Int.MAX_VALUE) {
            sendUiEvent(UiEvent.ScrollToEvent(scrollOffset))
        }
    }

    fun expandSideGestureButtonList(expanded: Boolean, scrollOffset: Int = Int.MAX_VALUE) {
        updateUiState {
            it.copy(
                isSideGestureButtonListExpanded = expanded,
                isBottomGestureButtonListExpanded = it.isBottomGestureButtonListExpanded && !expanded,
                isSubGestureListExpanded = it.isSubGestureListExpanded && !expanded
            )
        }
        if (expanded && scrollOffset != Int.MAX_VALUE) {
            sendUiEvent(UiEvent.ScrollToEvent(scrollOffset))
        }
    }

    fun updateGestureButtonColor(button: GestureButton, color: Int) {
        viewModelScope.launch {
            if (button.position == Position.Bottom) {
                ConfigProvider.updateBottomGestureButtons { buttons ->
                    buttons.map {
                        if (it.id == button.id && it.position == button.position) it.copy(color = color)
                        else it
                    }
                }
            } else {
                ConfigProvider.updateSideGestureButtons { buttons ->
                    buttons.map {
                        if (it.id == button.id && it.position == button.position) it.copy(color = color)
                        else it
                    }
                }
            }
        }
    }

    fun updateSubGestureColor(gesture: SubGesture, color: Int) {
        viewModelScope.launch {
            ConfigProvider.updateSubGestureSettings { settings ->
                settings.copy(
                    subGestures = settings.subGestures.map {
                        if (it.id == gesture.id) it.copy(color = color) else it
                    }
                )
            }
        }
    }

    fun onAppGestureEnabledChange(enabled: Boolean) {
        updateUiState {
            it.copy(isGestureEnabled = enabled)
        }
        saveSettings()
    }

    fun onBottomGestureButtonEnabledChange(button: GestureButton, enabled: Boolean) {
        updateUiState {
            val buttons = it.bottomGestureButtons
            val index = buttons.indexOf(button)
            if (index < 0) it else {
                it.copy(bottomGestureButtons = buttons.mapIndexed { i, b ->
                    if (i == index) b.copy(enabled = enabled) else b
                })
            }
        }
        saveSettings()
    }

    fun onSideGestureButtonEnabledChange(button: GestureButton, enabled: Boolean) {
        updateUiState {
            val buttons = it.sideGestureButtons
            val index = buttons.indexOf(button)
            if (index < 0) it else {
                it.copy(sideGestureButtons = buttons.mapIndexed { i, b ->
                    if (i == index) b.copy(enabled = enabled) else b
                })
            }
        }
        saveSettings()
    }

    fun deleteSubGesture(gesture: SubGesture) {
        viewModelScope.launch {
            ConfigProvider.updateSubGestureSettings { settings ->
                val cleanedGestureList = settings.subGestures.filter { it.id != gesture.id }
                settings.copy(subGestures = cleanedGestureList)
            }
            cleanSubGestureReferences(gesture.id)
            delay(50)
        }
    }

    fun showRenameDialog(target: RenameTarget) {
        updateUiState { it.copy(renameDialogTarget = target) }
    }

    fun hideRenameDialog() {
        updateUiState { it.copy(renameDialogTarget = null) }
    }

    fun doRename(target: RenameTarget, newName: String) {
        if (newName.isBlank()) {
            hideRenameDialog()
            return
        }
        viewModelScope.launch {
            when (target) {
                is RenameTarget.GestureButton -> {
                    val button = target.button
                    if (button.position == Position.Bottom) {
                        ConfigProvider.updateBottomGestureButtons { buttons ->
                            buttons.map {
                                if (it.id == button.id && it.position == button.position) it.copy(name = newName)
                                else it
                            }
                        }
                    } else {
                        ConfigProvider.updateSideGestureButtons { buttons ->
                            buttons.map {
                                if (it.id == button.id && it.position == button.position) it.copy(name = newName)
                                else it
                            }
                        }
                    }
                }
                is RenameTarget.SubGesture -> {
                    ConfigProvider.updateSubGestureSettings { settings ->
                        settings.copy(
                            subGestures = settings.subGestures.map {
                                if (it.id == target.gesture.id) it.copy(name = newName) else it
                            }
                        )
                    }
                }
            }
            hideRenameDialog()
        }
    }
}

internal fun parseNumberSuffix(text: String): Int {
    val match = Regex("""(\d+)$""").find(text)
    return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
}

internal suspend fun cleanSubGestureReferences(deletedId: String) {
    SubGestureCleaner.cleanSubGestureReferences(
        deletedId = deletedId,
        shouldRemove = { SubGestureCleaner.isSubGestureAction(it) }
    )
}
