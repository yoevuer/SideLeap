package hunoia.sideleap.ui.widget.quickapplaunch

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.util.IconResizeCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

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
        val cellWidth = (maxWidth - 6.dp * (keys.size - 1)) / keys.size
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            keys.forEach { (label, token) ->
                Surface(
                    modifier = Modifier
                        .width(cellWidth)
                        .height(keyHeight)
                        .clip(RoundedCornerShape(10.dp))
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
                            "调整" -> Icon(Icons.Outlined.Tune, contentDescription = "调整面板")
                            "删除" -> Icon(Icons.Outlined.DeleteOutline, contentDescription = "删除输入")
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
    var icon: Drawable? by remember(packageName) { mutableStateOf(IconResizeCache.iconCache[packageName]) }
    if (icon == null) {
        LaunchedEffect(packageName) {
            icon = withContext(Dispatchers.IO) {
                runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
            }?.also { IconResizeCache.iconCache[packageName] = it }
        }
    }
    return icon
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AppItem(app: AppInfo, isFrozen: Boolean = false, onClick: () -> Unit, onLongPress: (IntOffset, IntOffset) -> Unit) {
    val context = LocalContext.current
    var itemPos by remember { mutableStateOf(IntOffset.Zero) }
    var itemSize by remember { mutableStateOf(IntOffset.Zero) }
    val iconAlpha = if (isFrozen) 0.5f else 1f
    Column(
        modifier = Modifier
            .width(64.dp)
            .alpha(iconAlpha)
            .clip(RoundedCornerShape(10.dp))
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.localToWindow(Offset.Zero)
                itemPos = IntOffset(pos.x.roundToInt(), pos.y.roundToInt())
                itemSize = IntOffset(coordinates.size.width, coordinates.size.height)
            }
            .combinedClickable(onClick = { onClick() }, onLongClick = { onLongPress(itemPos, itemSize) })
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = rememberAppIconAsync(context, app.packageName)
        if (icon != null) {
            AsyncImage(
                model = icon,
                contentDescription = app.label,
                modifier = Modifier.height(40.dp).fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}