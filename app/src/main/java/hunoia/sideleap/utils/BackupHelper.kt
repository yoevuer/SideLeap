package hunoia.sideleap.utils

import android.content.Context
import android.net.Uri
import hunoia.sideleap.BuildConfig
import hunoia.sideleap.constant.Paths
import hunoia.sideleap.entity.global.Backup
import hunoia.sideleap.utils.DataStoreHolder.actionSettings
import hunoia.sideleap.utils.DataStoreHolder.advancedSettings
import hunoia.sideleap.utils.DataStoreHolder.bottomGestureButtons
import hunoia.sideleap.utils.DataStoreHolder.gestureSettings
import hunoia.sideleap.utils.DataStoreHolder.frozenAppSettings
import hunoia.sideleap.utils.DataStoreHolder.initialSettings
import hunoia.sideleap.utils.DataStoreHolder.quickAppLauncherSettings
import hunoia.sideleap.utils.DataStoreHolder.sideGestureButtons
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ZipUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

/**
 * @author aaronzzxup@gmail.com
 * @since 2025/7/1
 */
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
            // 创建临时目录
            FileUtils.createOrExistsDir(backupDir)

            // 写入BackupItem，等待压缩
            val backupItemBytes = getBackupItemBytes()
            val backupItemFile = File(backupItemFilePath).also {
                FileUtils.createFileByDeleteOldFile(it)
                it.appendBytes(backupItemBytes)
            }

            // 压缩图片
            val zipImageDirFile = File(zipImagePath).also {
                FileUtils.createFileByDeleteOldFile(it)
            }
            val imageFiles = FileUtils.listFilesInDir(Paths.Image)
            ZipUtils.zipFiles(imageFiles, zipImageDirFile)

            // 将Backup和图片一起压缩
            val zipFile = File(zipFilePath).also {
                FileUtils.createFileByDeleteOldFile(it)
            }
            ZipUtils.zipFiles(listOf(backupItemFile, zipImageDirFile), zipFile)

            // 写入到用户路径
            context.contentResolver.openOutputStream(saveTo)!!.use { outputStream ->
                val zipFileBytes = FileIOUtils.readFile2BytesByStream(zipFile)
                outputStream.write(zipFileBytes)
                outputStream.flush()
            }
        } catch (ex: Exception) {
            throw ex
        } finally {
            // 删除临时目录
            FileUtils.deleteAllInDir(backupDir)
        }
    }

    suspend fun restore(context: Context, restoreFrom: Uri) {
        try {
            context.contentResolver.openInputStream(restoreFrom)!!.use { inputStream ->
                val input = inputStream.readBytes()
                try {
                    // 兼容旧版备份文件恢复
                    restoreBackupFromBytes(context, input)
                    FileUtils.deleteAllInDir(Paths.Image)
                } catch (ignored: Exception) {
                    val restoreDirFile = File(restoreDir).also {
                        // 创建临时目录
                        FileUtils.createOrExistsDir(it)
                    }
                    val restoreFile = File(restoreFilePath).also {
                        FileUtils.createFileByDeleteOldFile(it)
                        it.appendBytes(input)
                    }
                    ZipUtils.unzipFile(restoreFile, restoreDirFile).forEach { file ->
                        if (file.name == ZIP_BACKUP) {
                            restoreBackupFromBytes(context, file.readBytes())
                        } else if (file.name == ZIP_IMAGES) {
                            FileUtils.deleteAllInDir(Paths.Image)
                            FileUtils.createOrExistsDir(Paths.Image)
                            ZipUtils.unzipFile(file, restoreDirFile).forEach { imageFile ->
                                val destFile = File("${Paths.Image}/${imageFile.name}")
                                FileUtils.copy(imageFile, destFile)
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            throw ex
        } finally {
            // 删除临时目录
            FileUtils.deleteAllInDir(restoreDir)
        }
    }

    private suspend fun getBackupItemBytes(): ByteArray {
        val backup = coroutineScope {
            Backup(
                initialSettings = async { initialSettings.data.first() }.await(),
                advancedSettings = async { advancedSettings.data.first() }.await(),
                gestureSettings = async { gestureSettings.data.first() }.await(),
                actionSettings = async { actionSettings.data.first() }.await(),
                gestureButtons = async { sideGestureButtons.data.first() }.await(),
                bottomGestureButtons = async { bottomGestureButtons.data.first() }.await(),
                quickAppLauncherSettings = async { quickAppLauncherSettings.data.first() }.await(),
                frozenAppSettings = async { frozenAppSettings.data.first() }.await(),
                timestamp = System.currentTimeMillis(),
                version = BuildConfig.VERSION_NAME
            )
        }
        val json = JsonHelper.encodeToString(backup)
        return EncodeUtils.base64Encode(json.toByteArray())
    }

    private suspend fun restoreBackupFromBytes(context: Context, bytes: ByteArray) {
        // 兼容旧版备份文件恢复
        val decoded = EncodeUtils.base64Decode(bytes)
        val backup = JsonHelper.decodeFromString<Backup>(String(decoded))
        val installedPackages = queryInstalledPackageNames(context)
        val sanitizedFrozenSettings = sanitizeFrozenAppSettings(
            backup.frozenAppSettings,
            installedPackages
        )
        coroutineScope {
            listOf(
                async {
                    initialSettings.updateData {
                        backup.initialSettings ?: it
                    }
                },
                async {
                    advancedSettings.updateData {
                        backup.advancedSettings ?: it
                    }
                },
                async {
                    gestureSettings.updateData {
                        backup.gestureSettings ?: it
                    }
                },
                async {
                    actionSettings.updateData {
                        backup.actionSettings ?: it
                    }
                },
                async {
                    sideGestureButtons.updateData {
                        backup.gestureButtons ?: it
                    }
                },
                async {
                    bottomGestureButtons.updateData {
                        backup.bottomGestureButtons ?: it
                    }
                },
                async {
                    quickAppLauncherSettings.updateData {
                        backup.quickAppLauncherSettings ?: it
                    }
                },
                async {
                    frozenAppSettings.updateData {
                        sanitizedFrozenSettings ?: it
                    }
                }
            ).awaitAll()
        }
    }

    private fun sanitizeFrozenAppSettings(
        settings: hunoia.sideleap.entity.global.FrozenAppSettings?,
        installedPackages: Set<String>
    ): hunoia.sideleap.entity.global.FrozenAppSettings? {
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
