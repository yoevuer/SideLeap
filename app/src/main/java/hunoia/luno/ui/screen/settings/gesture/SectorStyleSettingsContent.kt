package hunoia.luno.ui.screen.settings.gesture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hunoia.luno.R
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxArcLength
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxItemSize
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxSpacing
import hunoia.luno.config.defaults.SettingsUiDefaults.MinArcLength
import hunoia.luno.config.defaults.SettingsUiDefaults.MinItemSize
import hunoia.luno.config.defaults.SettingsUiDefaults.MinSpacing
import hunoia.luno.config.model.ArcStyle
import hunoia.luno.ui.theme.RootPadding
import hunoia.luno.ui.theme.ScrollBottomPadding
import hunoia.luno.ui.theme.SectionPadding
import hunoia.luno.ui.component.MyTextSlider
import hunoia.luno.ui.component.ExpressiveSection
import hunoia.luno.ui.screen.settings.gesture.ArcStylePreview
import hunoia.luno.ui.screen.settings.gesture.PieStylePreview
import kotlin.math.roundToInt

@Composable
fun ArcOrPieSettingsContent(
    itemSize: Int,
    arcLength: Int,
    spacing: Float,
    onItemSizeChange: (Int) -> Unit,
    onArcLengthChange: (Int) -> Unit,
    onSpacingChange: (Float) -> Unit,
    isPie: Boolean = false,
    modifier: Modifier = Modifier
) {
    var localItemSize by remember(itemSize) { mutableStateOf(itemSize.toFloat()) }
    var localArcLength by remember(arcLength) { mutableStateOf(arcLength.toFloat()) }
    var localSpacing by remember(spacing) { mutableStateOf(spacing) }

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(RootPadding)
            .padding(bottom = ScrollBottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isPie) {
            PieStylePreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = SectionPadding)
            )
        } else {
            ArcStylePreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = SectionPadding)
            )
        }
        ExpressiveSection(title = stringResource(id = R.string.icon)) {
            MyTextSlider(
                value = localItemSize,
                onValueChange = { localItemSize = it },
                onValueChangeFinished = { onItemSizeChange(localItemSize.toInt()) },
                text = stringResource(id = R.string.icon_size),
                valueDisplay = "${localItemSize.roundToInt()}px",
                valueRange = MinItemSize.toFloat()..MaxItemSize.toFloat()
            )
        }
        ExpressiveSection(
            modifier = Modifier.padding(top = SectionPadding),
            title = stringResource(id = R.string.layout)
        ) {
            MyTextSlider(
                value = localArcLength,
                onValueChange = { localArcLength = it },
                onValueChangeFinished = { onArcLengthChange(localArcLength.toInt()) },
                text = stringResource(id = R.string.arc_length),
                valueDisplay = "${localArcLength.roundToInt()}px",
                valueRange = MinArcLength.toFloat()..MaxArcLength.toFloat()
            )
            MyTextSlider(
                value = localSpacing,
                onValueChange = { localSpacing = it },
                onValueChangeFinished = { onSpacingChange(localSpacing) },
                text = stringResource(id = R.string.spacing),
                valueDisplay = String.format("%.2f", localSpacing),
                valueRange = MinSpacing..MaxSpacing
            )
        }
    }
}
