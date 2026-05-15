package hunoia.sideleap.gesture.input

import android.view.MotionEvent

object MotionEventDispatcher {

    private val listeners = mutableListOf<OnMotionEventListener>()

    fun addOnMotionEventListener(listener: OnMotionEventListener) {
        listeners.add(listener)
    }

    fun removeOnMotionEventListener(listener: OnMotionEventListener) {
        listeners.remove(listener)
    }

    fun dispatch(event: MotionEvent) {
        val listeners = listeners
        for (index in listeners.indices) {
            val l = listeners.getOrNull(index)
            l?.onDispatch(event)
        }
    }
}

fun interface OnMotionEventListener {

    fun onDispatch(event: MotionEvent)
}