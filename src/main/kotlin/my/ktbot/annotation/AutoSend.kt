package my.ktbot.annotation

import my.ktbot.utils.Counter
import my.ktbot.utils.toMessage
import my.miraiplus.Caller
import my.miraiplus.injector.Injector
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.isContentBlank

/**
 *  @Date:2022/5/29
 *  @author bin
 *  @version 1.0.0
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention
@MustBeDocumented
annotation class AutoSend(
	val log: Boolean = true,
	/**
	 * 在此时间后撤回（单位；ms）
	 */
	val recall: Long = 0
) {
	object Inject : Injector.Message<AutoSend> {
		override suspend fun doBefore(ann: AutoSend, event: MessageEvent, caller: Caller): Boolean {
			return super.doBefore(ann, event, caller)
		}

		override suspend fun doAfter(ann: AutoSend, event: MessageEvent, caller: Caller, result: Any?) {
			val message = result.toMessage()
			if (message === null || message.isContentBlank()) {
				return
			}
			if (ann.log) Counter.log(event)
			val receipt = event.subject.sendMessage(message)
			if (event is GroupMessageEvent && ann.recall > 0) {
				receipt.recallIn(ann.recall)
			}
			event.intercept()
		}
	}
}


