package my

import my.miraiplus.ObjectMap

object Test {
	@JvmStatic
	fun main(args: Array<String>) {
		fun <T : Any?> T.p() = println(this)
		val map = ObjectMap() + 1L
		map[Long::class].p()
		map[Long::class.javaObjectType].p()
	}

}
