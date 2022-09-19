package my

import java.util.regex.Pattern

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
}
