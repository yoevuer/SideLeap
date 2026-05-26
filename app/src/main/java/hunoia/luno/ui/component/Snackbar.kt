package hunoia.luno.ui.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.areNavigationBarsVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hunoia.luno.ui.theme.ScrollBottomPadding

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MySnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { Snackbar(it) }
) {
    val paddingBottom = when (WindowInsets.areNavigationBarsVisible) {
        true -> 0.dp
        else -> ScrollBottomPadding
    }
    SnackbarHost(
        modifier = modifier.padding(bottom = paddingBottom),
        hostState = hostState,
        snackbar = snackbar
    )
}