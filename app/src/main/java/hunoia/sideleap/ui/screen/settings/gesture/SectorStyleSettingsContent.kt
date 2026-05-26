package hunoia.sideleap.ui.screen.settings.gesture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hunoia.sideleap.R
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxArcLength
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxItemSize
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxSpacing
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinArcLength
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinItemSize
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinSpacing
import hunoia.sideleap.settings.model.ArcStyle
import hunoia.sideleap.ui.theme.RootPadding
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.ui.component.MyTextSlider
import hunoia.sideleap.ui.component.SectionCard
import kotlin.math.roundToInt

@Composable
fun ArcOrPieSettingsContent(
    itemSize: Int,
    arcLength: Int,
    spacing: Float,
    onItemSizeChange: (Int) -> Unit,
    onArcLengthChange: (Int) -> Unit,
    onSpacingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var localItemSize by remember(itemSize) { mutableStateOf(itemSize.toFloat()) }
    var localArcLength by remember(arcLength) { mutableStateOf(arcLength.toFloat()) }
    var localSpacing by remember(spacing) { mutableStateOf(spacing) }

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(RootPadding)
            .padding(bottom = ScrollBottomPadding)
    ) {
        SectionCard(title = stringResource(id = R.string.icon)) {
            MyTextSlider(
                value = localItemSize,
                onValueChange = { localItemSize = it },
                onValueChangeFinished = { onItemSizeChange(localItemSize.toInt()) },
                text = stringResource(id = R.string.icon_size),
                valueDisplay = "${localItemSize.roundToInt()}px",
                valueRange = MinItemSize.toFloat()..MaxItemSize.toFloat()
            )
        }
        SectionCard(
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
