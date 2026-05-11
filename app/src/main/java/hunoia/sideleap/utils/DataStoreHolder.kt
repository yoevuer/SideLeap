package hunoia.sideleap.utils

import androidx.datastore.core.DataStore
import hunoia.sideleap.App
import hunoia.sideleap.constant.DataStoreFiles
import hunoia.sideleap.entity.GestureButton
import hunoia.sideleap.entity.global.ActionSettings
import hunoia.sideleap.entity.global.AdvancedSettings
import hunoia.sideleap.entity.global.GestureSettings
import hunoia.sideleap.entity.global.InitialSettings
import hunoia.sideleap.entity.QuickAppLauncherSettings
import hunoia.sideleap.ktx.dataStore

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/24
 */
object DataStoreHolder {

    val initialSettings: DataStore<InitialSettings> = run {
        val fileName = DataStoreFiles.INITIAL_SETTINGS
        val defValue = InitialSettings()
        App.getContext().dataStore(fileName, defValue)
    }

    val advancedSettings: DataStore<AdvancedSettings> = run {
        val fileName = DataStoreFiles.ADVANCED_SETTINGS
        val defValue = AdvancedSettings()
        App.getContext().dataStore(fileName, defValue)
    }

    val gestureSettings: DataStore<GestureSettings> = run {
        val fileName = DataStoreFiles.GESTURE_SETTINGS
        val defValue = GestureSettings()
        App.getContext().dataStore(fileName, defValue)
    }

    val actionSettings: DataStore<ActionSettings> = run {
        val fileName = DataStoreFiles.ACTION_SETTINGS
        val defValue = ActionSettings()
        App.getContext().dataStore(fileName, defValue)
    }

    val bottomGestureButtons: DataStore<List<GestureButton>> = run {
        val fileName = DataStoreFiles.BOTTOM_GESTURE_BUTTONS
        val defValue = GestureButton.BottomDefaults
        App.getContext().dataStore(fileName, defValue)
    }

    val sideGestureButtons: DataStore<List<GestureButton>> = run {
        val fileName = DataStoreFiles.SIDE_GESTURE_BUTTONS
        val defValue = GestureButton.SideDefaults
        App.getContext().dataStore(fileName, defValue)
    }

    val quickAppLauncherSettings: DataStore<QuickAppLauncherSettings> = run {
        val fileName = DataStoreFiles.QUICK_APP_LAUNCHER
        val defValue = QuickAppLauncherSettings()
        App.getContext().dataStore(fileName, defValue)
    }

    suspend fun resetAll() {
        initialSettings.updateData { InitialSettings() }
        advancedSettings.updateData { AdvancedSettings() }
        gestureSettings.updateData { GestureSettings() }
        actionSettings.updateData { ActionSettings() }
        sideGestureButtons.updateData { GestureButton.SideDefaults }
        bottomGestureButtons.updateData { GestureButton.BottomDefaults }
        quickAppLauncherSettings.updateData { QuickAppLauncherSettings() }
    }
}
