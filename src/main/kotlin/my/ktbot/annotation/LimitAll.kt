package my.ktbot.annotation

import kotlinx.coroutines.sync.Mutex
import my.miraiplus.Caller
import my.miraiplus.ObjectMap
import my.miraiplus.injector.Injector
import net.mamoe.mirai.event.events.MessageEvent
import java.util.*

/**
 *  @Date:2022/6/1
 *  @author bin
 *  @version 1.0.0
 */
@MustBeDocumented
annotation class LimitAll(val time: Long) {
	companion object Inject : Injector.Message<LimitAll> {
		private val map = HashMap<String, Mutex>()
		private val timer = Timer("", true)
		override fun doInit(ann: LimitAll, caller: Caller) {
			map[caller.name] = Mutex()
		}

		override suspend fun doBefore(ann: LimitAll, event: MessageEvent, tmpMap: ObjectMap, caller: Caller): Boolean {
			return map[caller.name]?.tryLock(caller.name) ?: false
		}

		override suspend fun doAfter(
			ann: LimitAll, event: MessageEvent, tmpMap: ObjectMap, caller: Caller, result: Any?,
		) {
			timer.schedule(Unlock(caller.name), ann.time)
		}

		override fun doDestroy(ann: LimitAll, caller: Caller) {
			map -= caller.name
		}

		private class Unlock(private val name: String) : TimerTask() {
			override fun run() {
				map[name]?.unlock(name)
			}
		}
	}
}
