package my.ktbot.annotation

import my.ktbot.utils.sendAdmin
import my.ktbot.utils.toMessage
import my.miraiplus.Caller
import my.miraiplus.injector.Injector
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.data.isContentBlank

/**
 *  @Date:2022/5/31
 *  @author bin
 *  @version 1.0.0
 */
annotation class SendAdmin {
	object Inject : Injector<SendAdmin, BotEvent> {
		override val event = BotEvent::class
		override suspend fun doAfter(ann: SendAdmin, event: BotEvent, caller: Caller, result: Any?) {
			val message = result.toMessage()
			if (message === null || message.isContentBlank()) {
				return
			}
			event.intercept()
			event.sendAdmin(message)
		}
	}
}
