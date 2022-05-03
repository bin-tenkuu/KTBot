package my.ktbot

import my.ktbot.annotation.AutoCall
import my.ktbot.annotation.Qualifier
import my.ktbot.annotation.RegexAnn
import my.ktbot.utils.callor.AutoCallor
import my.ktbot.utils.callor.Caller
import my.ktbot.utils.callor.ObjectMap
import my.ktbot.utils.println

suspend fun main() {
	ObjectMap.global
		.set(String::class.java, "ss")
		.set("val", "val")

	AutoCallor.add(object {
		@AutoCall("f", RegexAnn(""), 0.0)
		val f = 0
	})
	ObjectMap.global[Caller::class, "f"]!!.invoke().println()
	AutoCallor.add(object {
		@AutoCall("v", RegexAnn(""), 0.0)
		private val v = 0
	})
	ObjectMap.global[Caller::class, "v"]!!.invoke().println()
	AutoCallor.add(object {
		@AutoCall("fun", RegexAnn(""), 0.0)
		private fun suspendfunction(str: String, @Qualifier("val") s: String) {
			println("suspendfunction,$str,$s")
		}
	})
	ObjectMap.global[Caller::class, "fun"]!!.invoke().println()
	AutoCallor.add(AutoCallorTest)
	ObjectMap.global[Caller::class, "sfun"]!!.invoke().println()
}

object AutoCallorTest {
	@JvmStatic
	@AutoCall("sfun", RegexAnn(""), 0.0)
	private fun suspendfunction(str: String, @Qualifier("val") s: String): String {
		println("suspendfunction,$str,$s")
		return "sfun"
	}
}
