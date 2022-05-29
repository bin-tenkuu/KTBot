package my.miraiplus.annotation

import net.mamoe.mirai.event.ConcurrencyKind
import net.mamoe.mirai.event.EventPriority
import org.intellij.lang.annotations.Language

/**
 * 消息匹配
 * @property pattern String [Regex.pattern]
 * @property options Array<out RegexOption> [Regex.options]
 * @property name String
 * @property concurrency ConcurrencyKind []
 * @property priority EventPriority []
 * @constructor
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention
@MustBeDocumented
annotation class MessageHandle(
	@Language("RegExp")
	val pattern: String,
	vararg val options: RegexOption = [],
	val name: String = "",
	val concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
	val priority: EventPriority = EventPriority.NORMAL,
)
