package hunoia.sideleap.settings

import android.content.Context
import android.net.Uri
import hunoia.sideleap.BuildConfig
import hunoia.sideleap.constant.Paths
import hunoia.sideleap.core.serialization.JsonHelper
import hunoia.sideleap.settings.model.Backup
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ZipUtils
import kotlinx.coroutines.flow.first
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

object BackupHelper {

    private const val ZIP_BACKUP = "backup"
    private const val ZIP_IMAGES = "images"

    private val backupDir = "${Paths.AppCache}/backup"
    private val restoreDir = "${Paths.AppCache}/restore"

    private val backupItemFilePath = "$backupDir/$ZIP_BACKUP"
    private val zipImagePath = "$backupDir/$ZIP_IMAGES"
    private val zipFilePath = "$backupDir/zip"
    private val restoreFilePath = "$restoreDir/restore"

    suspend fun backup(context: Context, saveTo: Uri) {
        try {
            FileUtils.createOrExistsDir(backupDir)

            val backupItemBytes = getBackupItemBytes()
            val backupItemFile = File(backupItemFilePath).also {
                FileUtils.createFileByDeleteOldFile(it)
                it.appendBytes(backupItemBytes)
            }

            val zipImageDirFile = File(zipImagePath).also {
                FileUtils.createFileByDeleteOldFile(it)
            }
            val imageFiles = FileUtils.listFilesInDir(Paths.Image)
            ZipUtils.zipFiles(imageFiles, zipImageDirFile)

            val zipFile = File(zipFilePath).also {
                FileUtils.createFileByDeleteOldFile(it)
            }
            ZipUtils.zipFiles(listOf(backupItemFile, zipImageDirFile), zipFile)

            context.contentResolver.openOutputStream(saveTo)!!.use { outputStream ->
                val zipFileBytes = FileIOUtils.readFile2BytesByStream(zipFile)
                outputStream.write(zipFileBytes)
                outputStream.flush()
            }
        } catch (ex: Exception) {
            throw ex
        } finally {
            FileUtils.deleteAllInDir(backupDir)
        }
    }

    suspend fun restore(context: Context, restoreFrom: Uri) {
        try {
            context.contentResolver.openInputStream(restoreFrom)!!.use { inputStream ->
                val input = inputStream.readBytes()
                val restoreDirFile = File(restoreDir).also {
                    FileUtils.createOrExistsDir(it)
                }
                val restoreFile = File(restoreFilePath).also {
                    FileUtils.createFileByDeleteOldFile(it)
                    it.appendBytes(input)
                }
                val extracted = ZipUtils.unzipFile(restoreFile, restoreDirFile)
                var restored = false
                var imagesRestored = false
                for (file in extracted) {
                    when (file.name) {
                        ZIP_BACKUP -> {
                            restoreBackupFromBytes(context, file.readBytes())
                            restored = true
                        }
                        ZIP_IMAGES -> {
                            FileUtils.deleteAllInDir(Paths.Image)
                            FileUtils.createOrExistsDir(Paths.Image)
                            ZipUtils.unzipFile(file, restoreDirFile).forEach { imageFile ->
                                val destFile = File("${Paths.Image}/${imageFile.name}")
                                FileUtils.copy(imageFile, destFile)
                            }
                            imagesRestored = true
                        }
                    }
                }
                if (!restored) throw IllegalStateException("restore failed: no backup entry found in zip")
                if (!imagesRestored) throw IllegalStateException("restore failed: no images entry found in zip")
            }
        } catch (ex: Exception) {
            throw ex
        } finally {
            FileUtils.deleteAllInDir(restoreDir)
        }
    }

    private suspend fun getBackupItemBytes(): ByteArray {
        val backup = SettingsProvider.snapshotAll()
        val json = JsonHelper.encodeToString(backup)
        return EncodeUtils.base64Encode(json.toByteArray())
    }

    private suspend fun restoreBackupFromBytes(context: Context, bytes: ByteArray) {
        val decoded = EncodeUtils.base64Decode(bytes)
        val backup = JsonHelper.decodeFromString<Backup>(String(decoded))
        if (backup.initialSettings == null && backup.advancedSettings == null &&
            backup.gestureSettings == null && backup.actionSettings == null &&
            backup.gestureButtons == null && backup.bottomGestureButtons == null &&
            backup.quickAppLauncherSettings == null && backup.frozenAppSettings == null
        ) {
            throw IllegalStateException("restore failed: backup contains no settings")
        }
        val installedPackages = queryInstalledPackageNames(context)
        val sanitizedFrozenSettings = sanitizeFrozenAppSettings(
            backup.frozenAppSettings,
            installedPackages
        )
        val modifiedBackup = if (sanitizedFrozenSettings != null) {
            backup.copy(frozenAppSettings = sanitizedFrozenSettings)
        } else {
            backup
        }
        SettingsProvider.restoreAll(modifiedBackup)
        val verified = SettingsProvider.snapshotAll()
        if (verified.initialSettings != modifiedBackup.initialSettings) {
            throw IllegalStateException("restore failed: initialSettings mismatch after restoreAll")
        }
    }

    private fun sanitizeFrozenAppSettings(
        settings: hunoia.sideleap.settings.model.FrozenAppSettings?,
        installedPackages: Set<String>
    ): hunoia.sideleap.settings.model.FrozenAppSettings? {
        settings ?: return null
        val protected = settings.protectedPackageNames.filterTo(mutableSetOf()) { it in installedPackages }
        val oneKey = settings.oneKeyPackageNames
            .filterTo(mutableSetOf()) { it in installedPackages && it !in protected }
        return settings.copy(
            oneKeyPackageNames = oneKey,
            protectedPackageNames = protected
        )
    }

    private fun queryInstalledPackageNames(context: Context): Set<String> {
        val pm = context.packageManager
        return try {
            val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(0)
            }
            apps.map { it.packageName }.filter { it.isNotBlank() }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }
}