package my.ktbot.annotation

import my.miraiplus.Caller
import my.miraiplus.injector.Injector
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At

/**
 *  @Date:2022/6/2
 *  @author bin
 *  @version 1.0.0
 */
annotation class NeedAtBot {
	object Inject : Injector.Message<NeedAtBot> {
		override suspend fun doBefore(ann: NeedAtBot, event: MessageEvent, caller: Caller): Boolean {
			return event.message.contains(At(event.bot.id))
		}
	}
}
