import my.ktbot.annotation.AutoCall
import my.miraiplus.annotation.Qualifier
import my.ktbot.annotation.RegexAnn
import my.ktbot.utils.callor.AutoCallor
import my.ktbot.utils.callor.Caller
import my.miraiplus.ObjectMap

suspend fun main() {
	fun <T> T.println() = apply { println(toString()) }
	ObjectMap.global.set("a", "a") + "string"
	AutoCallor.add(ObjectTest).forEach {
		(it as Caller)().println()
	}
}

object ObjectTest {
	@AutoCall("", RegexAnn(""), 0.0)
	private val private = "private"

	@AutoCall("", RegexAnn(""), 0.0)
	const val const = "const"

	@AutoCall("", RegexAnn(""), 0.0)
	val String.exGet get() = "$this.exGet"

	@AutoCall("", RegexAnn(""), 0.0)
	fun ObjectMap.func(@Qualifier("a") s: String, @Qualifier("global") map: ObjectMap) = "$this.func($s, $map)"

}
