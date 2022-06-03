package my.ktbot.annotation

import my.ktbot.PlugConfig
import my.ktbot.PluginMain
import my.ktbot.utils.toMessage
import my.miraiplus.Caller
import my.miraiplus.ObjectMap
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
		override suspend fun doAfter(ann: SendAdmin, event: BotEvent, tmpMap: ObjectMap, caller: Caller, result: Any?) {
			val message = result.toMessage()
			if (message === null || message.isContentBlank()) {
				return
			}
			event.intercept()
			val admin = PluginMain.catch {
				PlugConfig.getAdmin(event.bot)
			} ?: return
			PluginMain.catch {
				admin.sendMessage(message)
			} ?: PluginMain.catch {
				admin.sendMessage("发送失败")
			}
		}
	}
}
