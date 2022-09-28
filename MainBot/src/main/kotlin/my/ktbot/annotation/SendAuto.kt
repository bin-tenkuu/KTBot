package my.ktbot.annotation

import my.ktbot.PluginMain
import my.ktbot.utils.Counter
import my.ktbot.utils.toMessage
import my.miraiplus.ArgsMap
import my.miraiplus.Caller
import my.miraiplus.Injector
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.isContentBlank
import java.time.Duration

/**
 *  @Date:2022/5/29
 *  @author bin
 *  @version 1.0.0
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention
@MustBeDocumented
annotation class SendAuto(
	val log: Boolean = true,
	/**
	 * 在此时间后撤回（单位；ms）
	 */
	val recall: Long = 0,
) {
	companion object Inject : Injector.Message<SendAuto> {
		override val weight: Double
			get() = 1.0

		override suspend fun doBefore(ann: SendAuto, event: MessageEvent, tmpMap: ArgsMap, caller: Caller): Boolean {
			tmpMap["time"] = System.currentTimeMillis()
			return event is FriendMessageEvent || event is GroupMessageEvent
		}

		override suspend fun doAfter(
			ann: SendAuto, event: MessageEvent, tmpMap: ArgsMap, caller: Caller, result: Any?,
		) {
			val message = result.toMessage()
			if (message === null || message.isContentBlank()) {
				return
			}
			if (ann.log) {
				PluginMain.logger.info(
					"${tmpMap["time", 0L].toNow()}:${caller.name} 来源：${event.subject}.${event.sender}"
				)
				Counter.log(event)
			}
			event.intercept()
			val receipt = PluginMain.catch {
				event.subject.sendMessage(message)
			} ?: PluginMain.catch {
				event.subject.sendMessage("发送失败")
			} ?: return
			if (event is GroupMessageEvent && ann.recall > 0) {
				receipt.recallIn(ann.recall)
			}
		}

		private fun Long.toNow() = Duration.ofMillis(System.currentTimeMillis() - this)
	}
}


