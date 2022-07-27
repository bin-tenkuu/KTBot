package my.ktbot.annotation

import my.miraiplus.Caller
import my.miraiplus.ArgsMap
import my.miraiplus.Injector
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At

/**
 *  @Date:2022/6/2
 *  @author bin
 *  @version 1.0.0
 */
@MustBeDocumented
annotation class NeedAtBot {
	companion object Inject : Injector.Message<NeedAtBot> {
		override suspend fun doBefore(ann: NeedAtBot, event: MessageEvent, tmpMap: ArgsMap, caller: Caller): Boolean {
			return event.message.contains(At(event.bot.id))
		}
	}
}
