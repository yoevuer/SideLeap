package hunoia.luno.ui.screen.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hunoia.luno.R
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxBezierStrokeWidth
import hunoia.luno.config.defaults.SettingsUiDefaults.MinBezierStrokeWidth
import hunoia.luno.config.model.AnimationStyles
import hunoia.luno.config.model.BubbleStyle
import hunoia.luno.config.model.CapsuleStyle
import hunoia.luno.config.model.ColorSource
import hunoia.luno.config.model.LineStyle
import hunoia.luno.config.model.WaveStyle
import hunoia.luno.ui.component.ExpressiveRow
import hunoia.luno.ui.component.ExpressiveSection
import hunoia.luno.ui.component.MyColorDisplay
import hunoia.luno.ui.component.MyTextSlider
import kotlin.math.roundToInt

@Composable
fun ColorSettingsSection(
    type: Int,
    waveStyle: WaveStyle,
    capsuleStyle: CapsuleStyle,
    lineStyle: LineStyle,
    bubbleStyle: BubbleStyle,
    localWaveStrokeWidth: Float,
    localCapsuleStrokeWidth: Float,
    localLineStrokeWidth: Float,
    localBubbleStrokeWidth: Float,
    onWaveStrokeWidthChange: (Float) -> Unit,
    onCapsuleStrokeWidthChange: (Float) -> Unit,
    onLineStrokeWidthChange: (Float) -> Unit,
    onBubbleStrokeWidthChange: (Float) -> Unit,
    onWaveStrokeWidthFinished: () -> Unit,
    onCapsuleStrokeWidthFinished: () -> Unit,
    onLineStrokeWidthFinished: () -> Unit,
    onBubbleStrokeWidthFinished: () -> Unit,
    onBackgroundColorClick: () -> Unit,
    onStrokeColorClick: () -> Unit,
    onBackgroundColorSourceChange: (Boolean) -> Unit,
    onStrokeColorSourceChange: (Boolean) -> Unit,
) {
    ExpressiveSection(title = stringResource(id = R.string.color_outline)) {
        ExpressiveRow(
            onClick = onBackgroundColorClick,
            text = stringResource(id = R.string.background_color),
            icon = {
                MyColorDisplay(
                    color = resolvePreviewColor(
                        when (type) {
                            AnimationStyles.TYPE_WAVE -> waveStyle.backgroundColorSource
                            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.backgroundColorSource
                            AnimationStyles.TYPE_LINE -> lineStyle.backgroundColorSource
                            else -> bubbleStyle.backgroundColorSource
                        },
                        when (type) {
                            AnimationStyles.TYPE_WAVE -> waveStyle.backgroundColorThemeKey
                            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.backgroundColorThemeKey
                            AnimationStyles.TYPE_LINE -> lineStyle.backgroundColorThemeKey
                            else -> bubbleStyle.backgroundColorThemeKey
                        },
                        when (type) {
                            AnimationStyles.TYPE_WAVE -> waveStyle.backgroundColor
                            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.backgroundColor
                            AnimationStyles.TYPE_LINE -> lineStyle.backgroundColor
                            else -> bubbleStyle.backgroundColor
                        }
                    )
                )
            },
            trailing = {
                Switch(
                    checked = when (type) {
                        AnimationStyles.TYPE_WAVE -> waveStyle.backgroundColorSource
                        AnimationStyles.TYPE_CAPSULE -> capsuleStyle.backgroundColorSource
                        AnimationStyles.TYPE_LINE -> lineStyle.backgroundColorSource
                        else -> bubbleStyle.backgroundColorSource
                    } == ColorSource.Theme,
                    onCheckedChange = onBackgroundColorSourceChange
                )
            }
        )
        ExpressiveRow(
            onClick = onStrokeColorClick,
            text = stringResource(id = R.string.stroke_color),
            icon = {
                MyColorDisplay(
                    color = resolvePreviewColor(
                        when (type) {
                            AnimationStyles.TYPE_WAVE -> waveStyle.strokeColorSource
                            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.strokeColorSource
                            AnimationStyles.TYPE_LINE -> lineStyle.strokeColorSource
                            else -> bubbleStyle.strokeColorSource
                        },
                        when (type) {
                            AnimationStyles.TYPE_WAVE -> waveStyle.strokeColorThemeKey
                            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.strokeColorThemeKey
                            AnimationStyles.TYPE_LINE -> lineStyle.strokeColorThemeKey
                            else -> bubbleStyle.strokeColorThemeKey
                        },
                        when (type) {
                            AnimationStyles.TYPE_WAVE -> waveStyle.strokeColor
                            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.strokeColor
                            AnimationStyles.TYPE_LINE -> lineStyle.strokeColor
                            else -> bubbleStyle.strokeColor
                        }
                    )
                )
            },
            trailing = {
                Switch(
                    checked = when (type) {
                        AnimationStyles.TYPE_WAVE -> waveStyle.strokeColorSource
                        AnimationStyles.TYPE_CAPSULE -> capsuleStyle.strokeColorSource
                        AnimationStyles.TYPE_LINE -> lineStyle.strokeColorSource
                        else -> bubbleStyle.strokeColorSource
                    } == ColorSource.Theme,
                    onCheckedChange = onStrokeColorSourceChange
                )
            }
        )
        MyTextSlider(
            value = when (type) {
                AnimationStyles.TYPE_WAVE -> localWaveStrokeWidth
                AnimationStyles.TYPE_CAPSULE -> localCapsuleStrokeWidth
                AnimationStyles.TYPE_LINE -> localLineStrokeWidth
                else -> localBubbleStrokeWidth
            },
            onValueChange = { v ->
                when (type) {
                    AnimationStyles.TYPE_WAVE -> onWaveStrokeWidthChange(v)
                    AnimationStyles.TYPE_CAPSULE -> onCapsuleStrokeWidthChange(v)
                    AnimationStyles.TYPE_LINE -> onLineStrokeWidthChange(v)
                    else -> onBubbleStrokeWidthChange(v)
                }
            },
            onValueChangeFinished = {
                when (type) {
                    AnimationStyles.TYPE_WAVE -> onWaveStrokeWidthFinished()
                    AnimationStyles.TYPE_CAPSULE -> onCapsuleStrokeWidthFinished()
                    AnimationStyles.TYPE_LINE -> onLineStrokeWidthFinished()
                    else -> onBubbleStrokeWidthFinished()
                }
            },
            text = stringResource(id = R.string.stroke_width),
            valueDisplay = "${when (type) {
                AnimationStyles.TYPE_WAVE -> localWaveStrokeWidth
                AnimationStyles.TYPE_CAPSULE -> localCapsuleStrokeWidth
                AnimationStyles.TYPE_LINE -> localLineStrokeWidth
                else -> localBubbleStrokeWidth
            }.roundToInt()}px",
            valueRange = MinBezierStrokeWidth.toFloat()..MaxBezierStrokeWidth.toFloat()
        )
    }
}
