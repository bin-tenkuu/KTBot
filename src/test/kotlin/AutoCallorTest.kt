package org.example.mirai.plugin

import my.ktbot.annotation.AutoCall
import my.ktbot.utils.AutoCallor

suspend fun main() {
	val obj: Any = TestPlug
	val param = AutoCallor.ObjectMap()
		.set(String::class.java, "ss")
		.set(Int::class.java, 1)
		.set(Long::class.java, 2)
		.set(Double::class.java, 3.0)
	val callor = AutoCallor()
	callor.add(obj)
	callor.forEach(param)
}

abstract class Plug

@Suppress("unused", "RedundantSuspendModifier")
object TestPlug : Plug() {
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
