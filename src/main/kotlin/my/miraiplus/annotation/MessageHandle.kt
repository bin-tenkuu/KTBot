package my.miraiplus.annotation

import net.mamoe.mirai.event.ConcurrencyKind
import net.mamoe.mirai.event.EventPriority
import org.intellij.lang.annotations.Language

annotation class MessageHandle(
	@Language("RegExp")
	val pattern: String,
	vararg val option: RegexOption = [],
	val concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
	val priority: EventPriority = EventPriority.NORMAL,
)
