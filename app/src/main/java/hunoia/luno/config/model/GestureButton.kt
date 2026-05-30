package hunoia.luno.config.model

import android.os.SystemClock
import android.graphics.Color as AndroidColor
import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import hunoia.luno.bridge.DensityProvider
import hunoia.luno.config.model.GestureActions
import hunoia.luno.bridge.vibration.VibrationEffects
import kotlin.random.Random
import kotlinx.serialization.Serializable

private fun randomColor(): Int = AndroidColor.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

object GestureButtonDefaults {

    const val ID_DEFAULT = "1"
    const val Enabled = true
    const val Start = 0.0f
    const val End = 0.1f
    val Width = DensityProvider.dp2px(16f)
    val SlideActions = GestureActions()
    val LongSlideActions = GestureActions()
    val LongSlideActionPanelStyles = LongSlideActionPanelStyles()
    val TapActions = GestureActions()
    const val Color = android.graphics.Color.TRANSPARENT
    const val AlignRegion = true
    val SlideTriggerDistance = DensityProvider.dp2px(30f)
    val LongSlideTriggerDistance = DensityProvider.dp2px(100f)
    const val LongPressTriggerDelayMs = 250L
    const val LongSlideTriggerImmediately = true
    const val LongSlideTriggerDelayMs = 100L
    const val SlideVibrate = true
    const val LongSlideVibrate = true
    const val TapVibrate = true
    const val LongPressVibrate = true
    const val VibrateImmediately = false
    const val CustomVibrationMs = 50L
    const val FitSoftKeyboard = true
    const val IsPreciseSlideType = true
    const val HideLandscape = false
    const val HideScreenLock = false
    const val HideHomeScreen = false
    val SideDefaults = listOf(
        GestureButton(
            id = ID_DEFAULT,
            position = Position.Left,
            angle = defaultGestureAngleFor(Position.Left),
            start = 0.0f,
            end = 1.0f,
            slideActions = GestureActions(center = hunoia.luno.config.model.Action.toList(hunoia.luno.action.GlobalActions.BACK))
        ),
        GestureButton(
            id = ID_DEFAULT,
            position = Position.Right,
            angle = defaultGestureAngleFor(Position.Right),
            start = 0.0f,
            end = 1.0f,
            slideActions = GestureActions(center = hunoia.luno.config.model.Action.toList(hunoia.luno.action.GlobalActions.BACK))
        )
    )
    val BottomDefaults = listOf(
        GestureButton(
            id = ID_DEFAULT,
            position = Position.Bottom,
            angle = defaultGestureAngleFor(Position.Bottom),
            enabled = false,
            start = 0.0f,
            end = 1.0f,
            slideActions = GestureActions(center = hunoia.luno.config.model.Action.toList(hunoia.luno.action.GlobalActions.HOME)),
            longSlideActions = GestureActions(center = hunoia.luno.config.model.Action.toList(hunoia.luno.action.GlobalActions.RECENT))
        )
    )
}

@Serializable
@Keep
data class GestureButton(
    val id: String,
    val name: String = "",
    val position: Position,
    val angle: GestureAngle,
    val enabled: Boolean = GestureButtonDefaults.Enabled,
    val start: Float = GestureButtonDefaults.Start,
    val end: Float = GestureButtonDefaults.End,
    val width: Int = GestureButtonDefaults.Width,
    val slideActions: GestureActions = GestureButtonDefaults.SlideActions,
    val longSlideActions: GestureActions = GestureButtonDefaults.LongSlideActions,
    val longSlideActionPanelStyles: LongSlideActionPanelStyles = GestureButtonDefaults.LongSlideActionPanelStyles,
    val tapActions: GestureActions = GestureButtonDefaults.TapActions,
    val color: Int = GestureButtonDefaults.Color,
    val alignRegion: Boolean = GestureButtonDefaults.AlignRegion,
    val slideVibrate: Boolean = GestureButtonDefaults.SlideVibrate,
    val longSlideVibrate: Boolean = GestureButtonDefaults.LongSlideVibrate,
    val tapVibrate: Boolean = GestureButtonDefaults.TapVibrate,
    val longPressVibrate: Boolean = GestureButtonDefaults.LongPressVibrate,
    val vibrateImmediately: Boolean = GestureButtonDefaults.VibrateImmediately,
    val vibrationEffect: VibrationEffects = VibrationEffects.Click,
    val customVibrationMs: Long = GestureButtonDefaults.CustomVibrationMs,
    val slideTriggerDistance: Int = GestureButtonDefaults.SlideTriggerDistance,
    val longSlideTriggerDistance: Int = GestureButtonDefaults.LongSlideTriggerDistance,
    val longPressTriggerDelayMs: Long = GestureButtonDefaults.LongPressTriggerDelayMs,
    val longSlideTriggerImmediately: Boolean = GestureButtonDefaults.LongSlideTriggerImmediately,
    val longSlideTriggerDelayMs: Long = GestureButtonDefaults.LongSlideTriggerDelayMs,
    val fitSoftKeyboard: Boolean = GestureButtonDefaults.FitSoftKeyboard,
    val isPreciseSlideType: Boolean = GestureButtonDefaults.IsPreciseSlideType,
    val hideLandscape: Boolean = GestureButtonDefaults.HideLandscape,
    val hideScreenLock: Boolean = GestureButtonDefaults.HideScreenLock,
    val hideHomeScreen: Boolean = GestureButtonDefaults.HideHomeScreen,
) : Comparable<GestureButton> {

    companion object {
        const val ID_DEFAULT = GestureButtonDefaults.ID_DEFAULT

        val SideDefaults: List<GestureButton> get() = GestureButtonDefaults.SideDefaults
        val BottomDefaults: List<GestureButton> get() = GestureButtonDefaults.BottomDefaults

        fun createSidePair(leftName: String = "", rightName: String = ""): List<GestureButton> {
            val id = SystemClock.uptimeMillis().toString()
            val colorInt = randomColor()
            val color = Color(colorInt).toArgb()
            val b1 = GestureButton(
                id = id,
                name = leftName,
                position = Position.Left,
                angle = defaultGestureAngleFor(Position.Left),
                color = color
            )
            val b2 = GestureButton(
                id = id,
                name = rightName,
                position = Position.Right,
                angle = defaultGestureAngleFor(Position.Right),
                color = color
            )
            return listOf(b1, b2)
        }

        fun createBottom(name: String = ""): GestureButton {
            val id = SystemClock.uptimeMillis().toString()
            val colorInt = randomColor()
            val color = Color(colorInt).toArgb()
            return GestureButton(
                id = id,
                name = name,
                position = Position.Bottom,
                angle = defaultGestureAngleFor(Position.Bottom),
                color = color
            )
        }
    }

    val isDefault: Boolean = id == ID_DEFAULT

    override fun compareTo(other: GestureButton): Int {
        val idCompared = id.compareTo(other.id)
        if (idCompared == 0) {
            return position.compareTo(other.position)
        }
        return idCompared
    }
}
