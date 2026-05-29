package hunoia.luno.config.model

import hunoia.luno.R
import hunoia.luno.core.AppContext

internal fun GestureButton.resolveDisplayName(): String {
    if (name.isNotEmpty()) return name
    return AppContext.get().getString(
        when (position) {
            Position.Left -> R.string.left_gesture_button
            Position.Right -> R.string.right_gesture_button
            Position.Bottom -> R.string.bottom_gesture_button
        }
    )
}
