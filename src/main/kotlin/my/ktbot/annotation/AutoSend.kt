package my.ktbot.annotation

import my.ktbot.utils.Counter
import my.ktbot.utils.toMessage
import my.miraiplus.injector.Injector
import my.miraiplus.Caller
import net.mamoe.mirai.event.AbstractEvent
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
annotation class AutoSend(val log: Boolean = true) {
	object Inject : Injector<AutoSend>() {
		override suspend fun doBefore(ann: AutoSend, event: MessageEvent, caller: Caller): Boolean {
			return super.doBefore(ann, event, caller)
		}

		override suspend fun doAfter(ann: AutoSend, event: MessageEvent, caller: Caller, result: Any?) {
			val message = result.toMessage()
			if (message === null || message.isContentBlank()) {
				return
			}
			if (ann.log) Counter.log(event)
			(event as? AbstractEvent)?.cancel()
			event.subject.sendMessage(message)
		}
	}
}


