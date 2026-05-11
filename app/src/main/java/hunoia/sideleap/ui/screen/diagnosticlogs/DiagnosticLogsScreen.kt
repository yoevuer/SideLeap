package hunoia.sideleap.ui.screen.diagnosticlogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hunoia.sideleap.utils.DataStoreHolder
import hunoia.sideleap.utils.LauncherDiagnostics
import hunoia.sideleap.ui.widget.LabeledSwitch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun DiagnosticLogsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var enabled by remember { mutableStateOf(true) }
    var entries by remember { mutableStateOf<List<String>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        val diagnosticsEnabled = DataStoreHolder.advancedSettings.data.first().diagnosticsEnabled
        LauncherDiagnostics.setEnabled(diagnosticsEnabled)
        enabled = diagnosticsEnabled
        if (diagnosticsEnabled) {
            LauncherDiagnostics.d(context, "diagnostics screen opened")
        }
        entries = LauncherDiagnostics.entries(context)
    }

    fun refresh() {
        entries = LauncherDiagnostics.entries(context)
    }

    LaunchedEffect(entries) {
        if (entries.isNotEmpty()) {
            listState.animateScrollToItem(entries.size - 1)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = LauncherDiagnostics.copyText(context)
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(text.toByteArray())
            }
        } catch (e: Exception) {
            android.util.Log.e(LauncherDiagnostics.TAG, "export failed", e)
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "启动器诊断日志",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    LauncherDiagnostics.clear(context)
                    refresh()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text("清空", color = MaterialTheme.colorScheme.onError) }
            Button(onClick = { refresh() }) { Text("刷新") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val text = LauncherDiagnostics.copyText(context)
                if (text.isNotEmpty()) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("LauncherDiagnostics", text))
                }
                onBack()
            }) { Text("复制") }
            Button(onClick = {
                exportLauncher.launch("launcher_diagnostics.txt")
            }) { Text("导出") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                hunoia.sideleap.utils.ShizukuUtils.requestPermissionIfNeeded()
                hunoia.sideleap.utils.ShizukuUtils.dumpState(context, "diagnostic")
                refresh()
            }) { Text("Shizuku 状态检查") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                hunoia.sideleap.utils.ShizukuUtils.dumpDisabledPackagesViaUserService(context)
            }) { Text("Shizuku 冻结包诊断") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                coroutineScope.launch {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        hunoia.sideleap.utils.ShizukuUtils.fetchDisabledPackageNames(context)
                    }
                    refresh()
                }
            }) { Text("Shizuku 冻结包列表") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                hunoia.sideleap.utils.ShizukuUtils.enablePackageForDiagnostic(context, "com.alibaba.wireless")
            }) { Text("Shizuku 解冻诊断") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                coroutineScope.launch {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        hunoia.sideleap.utils.AppInfoUtils.inspectDisabledAppsByPackageManager(context)
                    }
                    refresh()
                }
            }) { Text("PM 冻结包诊断") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LabeledSwitch(
            onCheckedChange = { value ->
                LauncherDiagnostics.setEnabled(value)
                enabled = value
                coroutineScope.launch {
                    DataStoreHolder.advancedSettings.updateData {
                        it.copy(diagnosticsEnabled = value)
                    }
                }
            },
            checked = enabled,
            text = "启用诊断日志"
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "共 ${entries.size} 条，最近 200 条",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "暂无诊断日志；请先打开一次启动器或检查外部调用入口",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                itemsIndexed(entries) { index, entry ->
                    Text(
                        text = entry,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}