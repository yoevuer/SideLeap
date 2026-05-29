package hunoia.luno.ui.home

import hunoia.luno.ui.theme.*
import hunoia.luno.R
import hunoia.luno.ui.component.ExpressiveCard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.stringResource
import kotlin.math.roundToInt

@Composable
fun HomeExcludeCard(uiState: UiState, onClick: () -> Unit) {
    ExpressiveCard(
        title = stringResource(id = R.string.exclude_app_short),
        subtitle = "${uiState.excludedAppCount} 个应用已排除",
        icon = Icons.Default.Block,
        onClick = onClick,
        accent = MaterialTheme.colorScheme.secondaryContainer,
        onAccent = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text("管理排除")
        }
    }
}

@Composable
fun HomePointerCard(onClick: () -> Unit) {
    ExpressiveCard(
        title = stringResource(id = R.string.pointer),
        subtitle = "灵敏度、轨迹与长按",
        icon = Icons.Default.TouchApp,
        onClick = onClick,
    ) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text("指针设置")
        }
    }
}

@Composable
fun HomeFrozenCard(
    uiState: UiState,
    onClick: () -> Unit,
    onFreezeClick: () -> Unit,
    onUnfreezeClick: () -> Unit,
) {
    ExpressiveCard(
        title = stringResource(id = R.string.frozen_app_manage_short),
        subtitle = "已冻结 ${uiState.frozenAppCount} / 已选 ${uiState.selectedFrozenAppCount}",
        icon = Icons.Default.AcUnit,
        onClick = onClick,
        accent = MaterialTheme.colorScheme.tertiaryContainer,
        onAccent = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing6)) {
            FilledTonalButton(onClick = onFreezeClick, modifier = Modifier.weight(1f)) {
                Text("冻结")
            }
            FilledTonalButton(onClick = onUnfreezeClick, modifier = Modifier.weight(1f)) {
                Text("解冻")
            }
        }
    }
}

@Composable
fun HomeMiniWindowCard(
    uiState: UiState,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    ExpressiveCard(
        title = stringResource(id = R.string.mini_window_position_short),
        subtitle = if (uiState.miniWindowOverrideBounds) "自定义位置与大小" else "由系统决定小窗边界",
        icon = Icons.Default.Widgets,
        onClick = onClick,
        trailing = {
            Switch(checked = uiState.miniWindowOverrideBounds, onCheckedChange = onCheckedChange)
        },
    ) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text("位置大小")
        }
    }
}

@Composable
fun HomeToolsCard(
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onResetToggle: () -> Unit,
    showResetConfirm: Boolean,
    onResetConfirm: () -> Unit,
    onResetDismiss: () -> Unit,
    onCardAreaPosition: (Int) -> Unit,
) {
    ExpressiveCard(
        title = "工具",
        subtitle = "备份、恢复、恢复默认",
        icon = Icons.Default.Build,
        onClick = {},
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing6)) {
            FilledTonalButton(onClick = onBackupClick, modifier = Modifier.weight(1f)) {
                Text("备份")
            }
            FilledTonalButton(onClick = onRestoreClick, modifier = Modifier.weight(1f)) {
                Text("恢复")
            }
            FilledTonalButton(onClick = onResetToggle, modifier = Modifier.weight(1f)) {
                Text("默认")
            }
        }
        AnimatedVisibility(
            visible = showResetConfirm,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        onCardAreaPosition(coords.positionInWindow().y.roundToInt())
                    }
                    .padding(top = Spacing12),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                Column(modifier = Modifier.padding(Spacing14)) {
                    Text(
                        text = stringResource(id = R.string.reset_default_settings_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Spacer(Modifier.height(Spacing12))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing6),
                    ) {
                        FilledTonalButton(
                            onClick = onResetDismiss,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("取消")
                        }
                        FilledTonalButton(
                            onClick = onResetConfirm,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("确认重置")
                        }
                    }
                }
            }
        }
    }
}
