package hunoia.sideleap.ui.widget.quickapplaunch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hunoia.sideleap.entity.QuickAppLauncherSettings
import hunoia.sideleap.settings.SettingsProvider
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun QuickAppLauncherAdjustPanel(onSettingsChanged: (QuickAppLauncherSettings) -> Unit) {
    val settings by SettingsProvider.quickAppLauncherSettings.collectAsState(initial = QuickAppLauncherSettings())
    val coroutineScope = rememberCoroutineScope()
    var activeLabel by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AdjustSlider("位置高度", settings.panelHeightFraction, 0.05f, 0.9f, activeLabel, { activeLabel = it }) { value ->
                val next = settings.copy(panelHeightFraction = value)
                onSettingsChanged(next)
                coroutineScope.launch { SettingsProvider.updateQuickAppLauncherSettings { next } }
            }
            Spacer(modifier = Modifier.height(12.dp))
            AdjustSlider("内部高度", settings.contentHeightFraction, 0.35f, 0.9f, activeLabel, { activeLabel = it }) { value ->
                val next = settings.copy(contentHeightFraction = value)
                onSettingsChanged(next)
                coroutineScope.launch { SettingsProvider.updateQuickAppLauncherSettings { next } }
            }
            Spacer(modifier = Modifier.height(12.dp))
            AdjustSlider("宽度", settings.panelWidthFraction, 0.65f, 1.0f, activeLabel, { activeLabel = it }) { value ->
                val next = settings.copy(panelWidthFraction = value)
                onSettingsChanged(next)
                coroutineScope.launch { SettingsProvider.updateQuickAppLauncherSettings { next } }
            }
            Spacer(modifier = Modifier.height(12.dp))
            AdjustSlider("水平位置", settings.panelHorizontalBias, 0.0f, 1.0f, activeLabel, { activeLabel = it }) { value ->
                val next = settings.copy(panelHorizontalBias = value)
                onSettingsChanged(next)
                coroutineScope.launch { SettingsProvider.updateQuickAppLauncherSettings { next } }
            }
            Spacer(modifier = Modifier.height(12.dp))
            AdjustSlider("候选应用行数", settings.candidateRows.toFloat(), 1f, 3f, activeLabel, { activeLabel = it }, valueFormatter = { it.roundToInt().toString() }) { value ->
                val next = settings.copy(candidateRows = value.roundToInt().coerceIn(1, 3))
                onSettingsChanged(next)
                coroutineScope.launch { SettingsProvider.updateQuickAppLauncherSettings { next } }
            }
            if (activeLabel == null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        val next = settings.copy(
                            panelHeightFraction = 0.52f,
                            contentHeightFraction = 0.52f,
                            candidateRows = 1,
                            panelWidthFraction = 1.0f,
                            panelHorizontalBias = 0.5f,
                        )
                        onSettingsChanged(next)
                        coroutineScope.launch { SettingsProvider.updateQuickAppLauncherSettings { next } }
                    }
                ) {
                    Text("重置布局", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
internal fun AdjustSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    activeLabel: String?,
    onActiveLabelChange: (String?) -> Unit,
    valueFormatter: (Float) -> String = { "%.2f".format(it) },
    onChange: (Float) -> Unit,
) {
    val active = activeLabel == null || activeLabel == label
    if (activeLabel != null && activeLabel != label) return
    Column {
        Text(text = "$label ${valueFormatter(value)}", color = MaterialTheme.colorScheme.onSurface)
        Slider(
            value = value.coerceIn(min, max),
            onValueChange = { nextValue ->
                onActiveLabelChange(label)
                onChange(nextValue.coerceIn(min, max))
            },
            onValueChangeFinished = { onActiveLabelChange(null) },
            valueRange = min..max
        )
    }
}