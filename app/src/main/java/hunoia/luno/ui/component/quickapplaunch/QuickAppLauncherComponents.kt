package hunoia.luno.ui.component.quickapplaunch
import hunoia.luno.ui.theme.*

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import hunoia.luno.R
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import hunoia.luno.launcher.LauncherFacade
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.ui.theme.ShapeSmall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun KeyboardRow(
    view: View,
    keys: List<Pair<String, String?>>,
    keyHeight: Dp,
    onDelete: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    onAdjust: (() -> Unit)? = null,
    onToken: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellWidth = (maxWidth - Spacing6 * (keys.size - 1)) / keys.size
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing6), modifier = Modifier.fillMaxWidth()) {
            keys.forEach { (label, token) ->
                Surface(
                    modifier = Modifier
                        .width(cellWidth)
                        .height(keyHeight)
                        .clip(RoundedCornerShape(Spacing10))
                        .combinedClickable(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                when (label) {
                                    "删除" -> onDelete?.invoke()
                                    "调整" -> onAdjust?.invoke()
                                    else -> token?.let(onToken)
                                }
                            },
                            onLongClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                when (label) {
                                    "QW" -> onToken("1")
                                    "ER" -> onToken("2")
                                    "TY" -> onToken("3")
                                    "UI" -> onToken("4")
                                    "OP" -> onToken("5")
                                    "AS" -> onToken("6")
                                    "DF" -> onToken("7")
                                    "GH" -> onToken("8")
                                    "JK" -> onToken("9")
                                    "L" -> onToken("0")
                                    "删除" -> onClear?.invoke()
                                }
                            }
                        ),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        when (label) {
                            "调整" -> Icon(Icons.Outlined.Tune, contentDescription = stringResource(R.string.quick_app_launcher_adjust))
                            "删除" -> Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.quick_app_launcher_delete_input))
                            else -> Text(label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun rememberAppIconAsync(context: Context, packageName: String): Drawable? {
    var icon: Drawable? by remember(packageName) { mutableStateOf(LauncherFacade.getCachedIcon(packageName)) }
    if (icon == null) {
        LaunchedEffect(packageName) {
            icon = withContext(Dispatchers.IO) {
                LauncherFacade.loadIcon(context, packageName)
            }?.also { LauncherFacade.cacheIcon(packageName, it) }
        }
    }
    return icon
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AppItem(app: AppInfo, onClick: () -> Unit, onLongPress: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(64.dp)
            .combinedClickable(onClick = { onClick() }, onLongClick = { onLongPress() })
            .padding(Spacing4),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = rememberAppIconAsync(context, app.packageName)
        if (icon != null) {
            AsyncImage(
                model = icon,
                contentDescription = app.label,
                modifier = Modifier.height(Spacing40).fillMaxWidth().clip(RoundedCornerShape(Spacing10)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .height(Spacing40)
                    .fillMaxWidth()
                        .clip(RoundedCornerShape(ShapeSmall))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}
