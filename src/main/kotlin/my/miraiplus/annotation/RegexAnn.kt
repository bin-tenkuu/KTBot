package my.miraiplus.annotation

import my.miraiplus.Caller
import my.miraiplus.ObjectMap
import my.miraiplus.injector.Injector
import net.mamoe.mirai.event.events.MessageEvent
import org.intellij.lang.annotations.Language

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention
@MustBeDocumented
annotation class RegexAnn(
	/**
	 * 正则匹配字符串，参见[Regex.pattern]
	 */
	@Language("RegExp") val pattern: String,
	/**
	 * 正则匹配选项，参见[Regex.options]
	 */
	vararg val option: RegexOption = [],
) {
	class Inject(
		override val weight: Double = -10.0,
	) : Injector.Message<RegexAnn> {
		val map = HashMap<String, Regex>()
		override fun doInit(ann: RegexAnn, caller: Caller) {
			map[caller.fieldName] = Regex(ann.pattern, ann.option.toSet())
		}

		override suspend fun doBefore(ann: RegexAnn, event: MessageEvent, tmpMap: ObjectMap, caller: Caller): Boolean {
			val result = map[caller.fieldName]?.find(event.message.contentToString()) ?: return false
			tmpMap + result
			return true
		}

		override fun doDestroy(ann: RegexAnn, caller: Caller) {
			map -= caller.fieldName
		}
	}

	companion object {
		fun RegexAnn.joinToString(): String {
			return option.joinTo(StringBuilder(), "、") {
				when (it) {
					RegexOption.IGNORE_CASE -> "忽略大小写"
					RegexOption.MULTILINE -> "多行文本"
					RegexOption.DOT_MATCHES_ALL -> "跨行匹配"
					else -> ""
				}
			}.toString()
		}
	}
}
