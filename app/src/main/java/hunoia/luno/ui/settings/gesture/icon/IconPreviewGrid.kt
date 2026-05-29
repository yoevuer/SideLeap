package hunoia.luno.ui.settings.gesture.icon

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import hunoia.luno.R
import hunoia.luno.config.defaults.SettingsUiDefaults.DimAlpha
import hunoia.luno.quicklaunch.model.ScaleableDefaults.DEFAULT_SCALE
import hunoia.luno.quicklaunch.model.ScaleableDefaults.MAX_SCALE
import hunoia.luno.quicklaunch.model.ScaleableDefaults.MIN_SCALE
import hunoia.luno.ui.component.ExpressiveSection
import hunoia.luno.ui.component.ExpressiveSwitchItem
import hunoia.luno.ui.theme.ItemPadding

@Composable
internal fun IconPreviewGrid(
    selectedId: String,
    selectedBgColor: IconResizeUiState.BgColor?,
    scaleFactors: Map<String, Float>,
    icons: Map<String, Any?>,
    onScaleChange: (Float) -> Unit,
    onShowColorPicker: () -> Unit,
    onBgColorEnabled: (Boolean, Color) -> Unit,
    defaultBgColor: Color,
    modifier: Modifier = Modifier,
) {
    val scrimColor = MaterialTheme.colorScheme.scrim
    Column(
        modifier = modifier.width(250.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .drawWithCache {
                    val bounds = Rect(Offset.Zero, size)
                    val path = Path().apply {
                        addOval(bounds)
                    }
                    onDrawWithContent {
                        drawContent()
                        clipPath(path = path, clipOp = ClipOp.Difference) {
                            drawRect(color = scrimColor.copy(alpha = DimAlpha))
                        }
                    }
                }
                .clip(RectangleShape),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                modifier = Modifier.matchParentSize(),
                columns = GridCells.Fixed(11),
                userScrollEnabled = false
            ) {
                items(11 * 11) { index ->
                    val color = when (index % 2 == 0) {
                        true -> Color.LightGray
                        else -> Color.White
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(color = color)
                    )
                }
            }

            AnimatedContent(
                modifier = Modifier.matchParentSize(),
                targetState = selectedId to selectedBgColor,
                label = "IconChangeAnimation"
            ) { (id, bgColor) ->
                val scaleFactor by rememberUpdatedState(newValue = scaleFactors[id] ?: DEFAULT_SCALE)
                Box {
                    if (bgColor?.enabled == true) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .background(color = bgColor.color ?: defaultBgColor)
                        )
                    }

                    AsyncImage(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, _, zoom, _ ->
                                    val newScale = (scaleFactor * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                                    onScaleChange(newScale)
                                }
                            }
                            .graphicsLayer {
                                scaleX = scaleFactor
                                scaleY = scaleFactor
                            },
                        model = icons[id],
                        contentDescription = null
                    )
                }
            }
        }

        ExpressiveSection {
            ExpressiveSwitchItem(
                onClick = onShowColorPicker,
                onCheckedChange = { enabled -> onBgColorEnabled(enabled, defaultBgColor) },
                checked = selectedBgColor?.enabled ?: false,
                title = stringResource(id = R.string.background_color),
                markColor = selectedBgColor?.color ?: defaultBgColor
            )
        }
    }
}
