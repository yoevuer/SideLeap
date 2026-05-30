package hunoia.luno.ui.settings.gesture.icon

import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aaron.compose.ktx.onClick
import hunoia.luno.quicklaunch.model.ScaleableDefaults.DEFAULT_SCALE
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVerticalWithSection
import hunoia.luno.ui.theme.MinInteractiveSize
import hunoia.luno.ui.theme.Spacing16

@Composable
internal fun IconThumbnailStrip(
    ids: List<String>,
    icons: Map<String, Any?>,
    scaleFactors: Map<String, Float>,
    bgColors: Map<String, IconResizeUiState.BgColor?>,
    onSelectedIdChange: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            vertical = ContentPaddingVerticalWithSection,
            horizontal = ContentPaddingHorizontal * 2
        ),
        horizontalArrangement = Arrangement.spacedBy(ContentPaddingHorizontal)
    ) {
        itemsIndexed(
            items = ids,
            key = { _, item -> item }
        ) { _, id ->
            BadgedBox(
                modifier = Modifier
                    .size(MinInteractiveSize)
                    .onClick(enableRipple = false) {
                        onSelectedIdChange(id)
                    },
                badge = {
                    val curScaleFactors by rememberUpdatedState(newValue = scaleFactors)
                    val curBgColors by rememberUpdatedState(newValue = bgColors)
                    val visible by remember(id) {
                        derivedStateOf {
                            val scale = curScaleFactors[id]
                            (scale != null && scale != DEFAULT_SCALE) ||
                                    (curBgColors[id]?.enabled == true)
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Badge(
                            modifier = Modifier.requiredSize(Spacing16),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null
                            )
                        }
                    }
                }
            ) {
                AsyncImage(
                    modifier = Modifier.matchParentSize(),
                    model = icons[id],
                    contentDescription = null
                )
            }
        }
    }
}
