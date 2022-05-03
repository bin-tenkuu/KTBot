
import my.ktbot.annotation.AutoCall
import my.ktbot.annotation.RegexAnn
import my.ktbot.utils.callor.AutoCallor
import my.ktbot.utils.callor.Caller
import my.ktbot.utils.callor.ObjectMap
import my.ktbot.utils.println

suspend fun main() {
	ObjectMap.global + "string"
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
	val String.get get() = "$this.get"

}
