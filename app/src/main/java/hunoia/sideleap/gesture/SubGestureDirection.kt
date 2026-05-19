package hunoia.sideleap.gesture

import kotlinx.serialization.Serializable

@Serializable
enum class SubGestureDirection {
    Up,
    Down,
    Left,
    Right,
    UpRight,
    DownRight,
    DownLeft,
    UpLeft,
}
