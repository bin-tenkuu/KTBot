package my

import java.util.regex.Pattern
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object Test : Print {
	@JvmStatic
	fun main(args: Array<String>) {
		"v_s_sz159949".substring(4, 12).pl()
		val pattern = Pattern.compile("^\\(?<a1>1+\\):(?<a2>2+)\$")

		val regex = Regex("(?<=\\(\\?<)\\w+(?=>)")
		val r = regex.findAll(pattern.pattern()).map { it.value }.toList()
		val 中文 = Thread.currentThread().stackTrace
		r
	}

	class A : ReadOnlyProperty<A, String> {
		val a: String by this
		val b: String by this

		override operator fun getValue(a: A, property: KProperty<*>): String {
			return a.a
		}
	}
}

// private operator fun <E> List<E>.getValue(e: E?, property: KProperty<E?>): E {
//
// }

// private operator fun <E> List<E>.getValue(e: E?, property: KProperty<E?>): E {
//
// }
