package hunoia.luno.ui.screen.actionselect

import android.graphics.Bitmap
import hunoia.luno.action.api.appInfo
import hunoia.luno.action.api.shortcutInfo
import hunoia.luno.config.model.Action
import hunoia.luno.quicklaunch.model.qualifiedName
import hunoia.luno.quicklaunch.model.qualifiedNameWithIntents
import hunoia.luno.core.JsonHelper
import hunoia.luno.core.Paths
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.ui.component.IconResizeEvent
import java.io.FileOutputStream
import java.io.File

class EventHandler(
    private val onUpdateUiState: ((UiState) -> UiState) -> Unit,
    private val onSaveSettings: () -> Unit,
    private val subscribeToIconResizeEvent: ((IconResizeEvent) -> Unit) -> Unit
) {
    fun init() {
        subscribeToIconResizeEvent { event ->
            val scaleFactors = event.scaleFactors
            val bgColors = event.bgColors
            onUpdateUiState { state ->
                val selectedList = state.selectedRecord.list.toMutableList()
                scaleFactors.forEach { (id, scaleFactor) ->
                    val index = selectedList.indexOfFirst { obj ->
                        (obj as? Action)?.appInfo?.qualifiedName == id
                    }
                    if (index != -1) {
                        val old = selectedList[index] as Action
                        val appInfo = old.appInfo
                        if (appInfo != null) {
                            selectedList[index] = old.copy(data = JsonHelper.encodeToString(appInfo.copy(iconScale = scaleFactor)))
                        }
                        return@forEach
                    }
                    val index2 = selectedList.indexOfFirst { obj ->
                        (obj as? Action)?.shortcutInfo?.qualifiedNameWithIntents == id
                    }
                    if (index2 != -1) {
                        val old = selectedList[index2] as Action
                        val shortcutInfo = old.shortcutInfo
                        if (shortcutInfo != null) {
                            selectedList[index2] = old.copy(data = JsonHelper.encodeToString(shortcutInfo.copy(iconScale = scaleFactor)))
                        }
                    }
                }
                bgColors.forEach { (id, color) ->
                    val index = selectedList.indexOfFirst { obj ->
                        (obj as? Action)?.appInfo?.qualifiedName == id
                    }
                    if (index != -1) {
                        val old = selectedList[index] as Action
                        val appInfo = old.appInfo
                        if (appInfo != null) {
                            selectedList[index] = old.copy(data = JsonHelper.encodeToString(appInfo.copy(iconBgColor = color)))
                        }
                        return@forEach
                    }
                    val index2 = selectedList.indexOfFirst { obj ->
                        (obj as? Action)?.shortcutInfo?.qualifiedNameWithIntents == id
                    }
                    if (index2 != -1) {
                        val old = selectedList[index2] as Action
                        val shortcutInfo = old.shortcutInfo
                        if (shortcutInfo != null) {
                            selectedList[index2] = old.copy(data = JsonHelper.encodeToString(shortcutInfo.copy(iconBgColor = color)))
                        }
                    }
                }

                val shortcutInfos = mutableMapOf<Int, LauncherInfo.ShortcutInfo>()
                selectedList.forEachIndexed { index, obj ->
                    val action = obj as? Action ?: return@forEachIndexed
                    val shortcutInfo = action.shortcutInfo ?: return@forEachIndexed
                    val iconBitmap = shortcutInfo.iconBitmap ?: return@forEachIndexed
                    val iconPath = "${Paths.Image}/${System.currentTimeMillis()}"
                    val fos = FileOutputStream(iconPath)
                    iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    shortcutInfos[index] = shortcutInfo.copy(iconPath = iconPath)
                }
                shortcutInfos.forEach { (index, shortcutInfo) ->
                    val action = selectedList[index] as? Action ?: return@forEach
                    selectedList[index] = action.copy(data = JsonHelper.encodeToString(shortcutInfo))
                }

                state.copy(selectedRecord = UiState.SelectedRecord(selectedList))
            }
            onSaveSettings()
        }
    }
}
