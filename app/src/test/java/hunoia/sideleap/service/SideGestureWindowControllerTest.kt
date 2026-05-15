package hunoia.sideleap.service

import android.view.Gravity
import android.view.WindowManager
import hunoia.sideleap.gesture.GestureActions
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.gesture.Position
import org.junit.Assert.assertEquals
import org.junit.Test

class SideGestureWindowControllerTest {

    @Test
    fun updateGestureButton_leftRightBottom_matchesExpectedLayout() {
        val left = WindowManager.LayoutParams().apply {
            updateGestureButton(
                button = GestureButton(
                    id = "left",
                    position = Position.Left,
                    enabled = true,
                    start = 0.25f,
                    end = 0.75f,
                    width = 18,
                    slideActions = GestureActions(),
                    longSlideActions = GestureActions(),
                    color = 0,
                    alignRegion = true,
                    excludeSystemGestureRects = false,
                    limitMaxExcludeSystemGestureLength = true,
                ),
                rootWidth = 1080,
                rootHeight = 2400,
            )
        }
        assertEquals(18, left.width)
        assertEquals(1200, left.height)
        assertEquals(600, left.y)
        assertEquals(0, left.x)
        assertEquals(Gravity.LEFT or Gravity.TOP, left.gravity)

        val right = WindowManager.LayoutParams().apply {
            updateGestureButton(
                button = GestureButton(
                    id = "right",
                    position = Position.Right,
                    enabled = true,
                    start = 0.25f,
                    end = 0.75f,
                    width = 18,
                    slideActions = GestureActions(),
                    longSlideActions = GestureActions(),
                    color = 0,
                    alignRegion = true,
                    excludeSystemGestureRects = false,
                    limitMaxExcludeSystemGestureLength = true,
                ),
                rootWidth = 1080,
                rootHeight = 2400,
            )
        }
        assertEquals(18, right.width)
        assertEquals(1200, right.height)
        assertEquals(600, right.y)
        assertEquals(0, right.x)
        assertEquals(Gravity.RIGHT or Gravity.TOP, right.gravity)

        val bottom = WindowManager.LayoutParams().apply {
            updateGestureButton(
                button = GestureButton(
                    id = "bottom",
                    position = Position.Bottom,
                    enabled = true,
                    start = 0.25f,
                    end = 0.75f,
                    width = 18,
                    slideActions = GestureActions(),
                    longSlideActions = GestureActions(),
                    color = 0,
                    alignRegion = true,
                    excludeSystemGestureRects = false,
                    limitMaxExcludeSystemGestureLength = true,
                ),
                rootWidth = 1080,
                rootHeight = 2400,
            )
        }
        assertEquals(540, bottom.width)
        assertEquals(18, bottom.height)
        assertEquals(270, bottom.x)
        assertEquals(2382, bottom.y)
        assertEquals(Gravity.LEFT or Gravity.TOP, bottom.gravity)
    }
}
