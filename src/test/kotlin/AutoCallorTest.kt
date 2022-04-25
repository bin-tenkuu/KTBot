package org.example.mirai.plugin

import my.ktbot.annotation.AutoCall
import my.ktbot.utils.callor.AutoCallor
import my.ktbot.utils.callor.ObjectMap

suspend fun main() {
	val obj: Any = TestPlug
	val param = ObjectMap()
		.set(String::class.java, "ss")
		.set(Int::class.java, 1)
		.set(Long::class.java, 2)
		.set(Double::class.java, 3.0) + 4 + 5L + 6.0
	val callor = AutoCallor()
	callor.add(obj)
	callor.forEach(param)
}

@Suppress("unused", "RedundantSuspendModifier")
object TestPlug {
	@JvmStatic
	@get:AutoCall
	val valJvmField = "valJvmField"

	@get:AutoCall
	val valField = "valField"

	@AutoCall
	fun function(str: String, int: Int) {
		println("function,$str,$int")
	}

	@AutoCall
	suspend fun suspendfunction(str: String, int: Int) {
		println("suspendfunction,$str,$int")
	}
}
