package my.miraiplus.injector

import my.miraiplus.annotation.MessageHandle
import my.miraiplus.util.Caller
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*

/**
 *  @Date:2022/5/28
 *  @author bin
 *  @version 1.0.0
 */
object MessageSendInjector : Injector<MessageHandle>(-10.0) {

	override suspend fun doAfter(ann: MessageHandle, event: MessageEvent, caller: Caller, result: Any?) {
		val message = toMessage(result)
		if (message === null || message.isContentBlank()) {
			return
		}
		event.subject.sendMessage(message)
	}

	@JvmStatic
	private fun toMessage(any: Any?): Message? {
		return when (any) {
			null -> null
			Unit -> null
			is Message -> any
			is CharSequence -> if (any.isEmpty()) EmptyMessageChain else PlainText(any)
			is Array<*> -> buildMessageChain {
				addAll(any.mapNotNull(::toMessage))
			}
			is Iterable<*> -> buildMessageChain {
				addAll(any.mapNotNull(::toMessage))
			}
			else -> PlainText(any.toString())
		}
	}
}
