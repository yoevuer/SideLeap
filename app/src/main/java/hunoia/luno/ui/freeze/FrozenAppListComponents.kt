package hunoia.luno.ui.freeze
import hunoia.luno.ui.theme.*

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import hunoia.luno.R
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.icon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FrozenAppSelectableItem(
    app: AppInfo,
    isFrozen: Boolean = false,
    isPending: Boolean = false,
    longClickEnabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (longClickEnabled) onLongClick else null,
            )
            .padding(Spacing2)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(Spacing40)
                    .then(
                        if (isPending) Modifier.border(
                            Spacing2,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.medium
                        ) else Modifier
                    )
            ) {
                val context = LocalContext.current
                AsyncImage(
                    model = app.icon,
                    contentDescription = null,
                    imageLoader = context.imageLoader,
                    contentScale = ContentScale.Crop,
                    colorFilter = if (isFrozen) {
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    } else null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                )
            }
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isFrozen) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing4)
            )
        }
    }
}
