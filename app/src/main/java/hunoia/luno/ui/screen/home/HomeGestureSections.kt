package hunoia.luno.ui.screen.home

import hunoia.luno.ui.theme.*
import hunoia.luno.R
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.SubGesture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun HomeGestureSections(
    uiState: UiState,
    onBottomHeaderClick: () -> Unit,
    onSideHeaderClick: () -> Unit,
    onSubHeaderClick: () -> Unit,
    onBottomButtonClick: (GestureButton) -> Unit,
    onSideButtonClick: (GestureButton) -> Unit,
    onSubGestureClick: (String) -> Unit,
    onBottomCheckedChange: (GestureButton, Boolean) -> Unit,
    onSideCheckedChange: (GestureButton, Boolean) -> Unit,
    onSubCheckedChange: (SubGesture, Boolean) -> Unit,
    onAddBottom: () -> Unit,
    onAddSide: () -> Unit,
    onAddSub: () -> Unit,
    onMarkColorClick: (Any) -> Unit,
    onGestureButtonRename: (GestureButton) -> Unit = {},
    onSubGestureRename: (SubGesture) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column {
            GestureEntryCard(
                title = stringResource(id = R.string.bottom_gesture_button_list_short),
                subtitle = "${uiState.bottomGestureButtons.count { it.enabled }} / ${uiState.bottomGestureButtons.size} 个已启用",
                icon = Icons.Default.ArrowUpward,
                expanded = uiState.isBottomGestureButtonListExpanded,
                onClick = onBottomHeaderClick,
            )
            GestureButtonList(
                visible = uiState.isBottomGestureButtonListExpanded,
                buttons = uiState.bottomGestureButtons,
                onItemClick = onBottomButtonClick,
                onCheckedChange = onBottomCheckedChange,
                onAddClick = onAddBottom,
                onMarkColorClick = onMarkColorClick,
                onRenameClick = onGestureButtonRename,
            )
        }
        Spacer(Modifier.height(Spacing12))
        Column {
            GestureEntryCard(
                title = stringResource(id = R.string.side_gesture_button_list_short),
                subtitle = "${uiState.sideGestureButtons.count { it.enabled }} / ${uiState.sideGestureButtons.size} 个已启用",
                icon = Icons.Default.SwapHoriz,
                expanded = uiState.isSideGestureButtonListExpanded,
                onClick = onSideHeaderClick,
                accent = MaterialTheme.colorScheme.secondaryContainer,
                onAccent = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            GestureButtonList(
                visible = uiState.isSideGestureButtonListExpanded,
                buttons = uiState.sideGestureButtons,
                onItemClick = onSideButtonClick,
                onCheckedChange = onSideCheckedChange,
                onAddClick = onAddSide,
                onMarkColorClick = onMarkColorClick,
                onRenameClick = onGestureButtonRename,
            )
        }
        Spacer(Modifier.height(Spacing12))
        Column {
            GestureEntryCard(
                title = stringResource(id = R.string.sub_gesture_list),
                subtitle = "${uiState.subGestures.count { it.enabled }} / ${uiState.subGestures.size} 个已启用",
                icon = Icons.Default.AllInclusive,
                expanded = uiState.isSubGestureListExpanded,
                onClick = onSubHeaderClick,
                accent = MaterialTheme.colorScheme.tertiaryContainer,
                onAccent = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            SubGestureList(
                visible = uiState.isSubGestureListExpanded,
                gestures = uiState.subGestures,
                onItemClick = onSubGestureClick,
                onCheckedChange = onSubCheckedChange,
                onAddClick = onAddSub,
                onMarkColorClick = onMarkColorClick,
                onRenameClick = onSubGestureRename,
            )
        }
    }
}
