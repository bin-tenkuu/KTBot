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
		override val weight: Double = -10.0
	) : Injector.Message<RegexAnn> {
		val map = HashMap<String, Regex>()
		override fun init(ann: RegexAnn, caller: Caller) {
			map[caller.name] = Regex(ann.pattern, ann.option.toSet())
		}

		override suspend fun doBefore(ann: RegexAnn, event: MessageEvent, tmpMap: ObjectMap, caller: Caller): Boolean {
			val result = map[caller.name]?.find(event.message.contentToString()) ?: return false
			tmpMap + result
			return true
		}
	}
}
