package my.miraiplus.annotation

import my.miraiplus.ArgsMap
import my.miraiplus.Caller
import my.miraiplus.Injector
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import org.intellij.lang.annotations.Language

/**
 * 根据正则匹配
 * @property pattern [String] 正则匹配字符串，参见[Regex.pattern]
 * @property option [Array]<[RegexOption]> 正则匹配选项，参见[Regex.options]
 * @constructor
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention
@MustBeDocumented
annotation class RegexAnn(
	@Language("RegExp") val pattern: String,
	vararg val option: RegexOption = [],
) {
	class Inject(
		override val weight: Double = -10.0,
	) : Injector.Message<RegexAnn> {
		val map = HashMap<String, Regex>()
		override fun doInit(ann: RegexAnn, caller: Caller) {
			map[caller.fieldName] = Regex(ann.pattern, ann.option.toSet())
		}

		override suspend fun doBefore(ann: RegexAnn, event: MessageEvent, tmpMap: ArgsMap, caller: Caller): Boolean {
			val result = map[caller.fieldName]?.find(event.message.textString) ?: return false
			val groups = result.groups
			tmpMap + result + groups
			for (i in groups.indices) {
				groups[i]?.value?.let {
					tmpMap[i.toString()] = it
				}
			}
			return true
		}

		override fun doDestroy(ann: RegexAnn, caller: Caller) {
			map -= caller.fieldName
		}

		private val MessageChain.textString get() = filterIsInstance<PlainText>().joinToString("") { it.content }
	}
}
