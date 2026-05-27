package hunoia.luno.ui.gesture

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import hunoia.luno.R
import hunoia.luno.ui.action.actionTextCompose
import hunoia.luno.gesture.GestureButton
import hunoia.luno.gesture.Position

@Composable
fun GestureButton.buttonTextCompose(): String {
    return when (position) {
        Position.Left -> stringResource(id = R.string.left_gesture_button)
        Position.Right -> stringResource(id = R.string.right_gesture_button)
        Position.Bottom -> stringResource(id = R.string.bottom_gesture_button)
    }
}

@Composable
fun GestureButton.actionTextCompose(): String {
    var text = ""
    val slideActionText = slideActions.actionTextCompose()
    if (slideActionText.isNotEmpty()) {
        text += slideActionText
    }
    val longSlideActionText = longSlideActions.actionTextCompose()
    if (longSlideActionText.isNotEmpty()) {
        text += if (text.isEmpty()) {
            longSlideActionText
        } else {
            ",$longSlideActionText"
        }
    }
    return text
}
