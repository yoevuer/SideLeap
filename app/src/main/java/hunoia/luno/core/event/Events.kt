package hunoia.luno.core.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

object Events {

    private var scope: CoroutineScope? = null
    private val subscribers = ConcurrentHashMap<KClass<out Any>, CopyOnWriteArrayList<(Any) -> Unit>>()

    var isMainThread: () -> Boolean = { true }

    fun initScope(applicationScope: CoroutineScope) {
        scope = applicationScope
    }

    fun post(event: Any, postOnUiThread: Boolean = true) {
        if (postOnUiThread && !isMainThread()) {
            scope?.launch(Dispatchers.Main) {
                dispatch(event)
            }
        } else {
            dispatch(event)
        }
    }

    fun <T : Any> subscribe(eventClass: KClass<T>, subscriber: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        subscribers.computeIfAbsent(eventClass) { CopyOnWriteArrayList() }.add(subscriber as (Any) -> Unit)
    }

    fun <T : Any> unsubscribe(eventClass: KClass<T>, subscriber: (T) -> Unit) {
        val list = subscribers[eventClass] ?: return
        list.remove(subscriber)
        if (list.isEmpty()) {
            subscribers.remove(eventClass, list)
        }
    }

    internal fun dispatch(event: Any) {
        subscribers[event::class]?.forEach {
            it(event)
        }
    }
}
