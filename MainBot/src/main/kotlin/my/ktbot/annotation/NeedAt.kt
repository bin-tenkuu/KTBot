package my.ktbot.annotation

import my.miraiplus.ArgsMap
import my.miraiplus.Caller
import my.miraiplus.Injector
import net.mamoe.mirai.message.data.At

/**
 * @param bot 是否为@Bot
 * @Date:2022/6/2
 * @author bin
 * @version 1.0.0
 */
@MustBeDocumented
annotation class NeedAt(val bot: Boolean = false) {
    companion object Inject : Injector.Message<NeedAt> {
        override suspend fun doBefore(ann: NeedAt, tmpMap: ArgsMap, caller: Caller): Boolean {
            val event =  tmpMap[event] ?: return false
            if (ann.bot) {
                return event.message.contains(At(event.bot.id))
            }
            val list = event.message.filterIsInstance<At>()
            if (list.isEmpty()) return false
            tmpMap["NeedAt"] = list
            return true
        }
    }
}
