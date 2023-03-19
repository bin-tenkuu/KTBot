package my.ktbot.annotation

import kotlinx.coroutines.sync.Mutex
import my.miraiplus.ArgsMap
import my.miraiplus.Caller
import my.miraiplus.Injector
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.utils.MiraiLogger
import java.util.*

/**
 *  @Date:2022/6/1
 *  @author bin
 *  @version 1.0.0
 */
@MustBeDocumented
annotation class LimitAll(val time: Long) {
    companion object Inject : Injector.Message<LimitAll> {
        private val logger = MiraiLogger.Factory.create(LimitAll::class.java)
        private val map = HashMap<String, Mutex>()
        private val timer = Timer("", true)
        override fun doInit(ann: LimitAll, caller: Caller) {
            map[caller.name] = Mutex()
        }

        override suspend fun doBefore(ann: LimitAll, tmpMap: ArgsMap, caller: Caller): Boolean {
            val mutex = map[caller.name] ?: return false
            if (mutex.isLocked) {
                logger.info("${caller.name} 限制中")
                return false
            }
            return mutex.tryLock(caller.name)
        }

        override suspend fun doAfter(
            ann: LimitAll, event: MessageEvent, tmpMap: ArgsMap, caller: Caller, result: Any?,
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
