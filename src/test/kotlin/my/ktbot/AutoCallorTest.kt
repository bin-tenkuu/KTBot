package my.ktbot

import my.ktbot.annotation.AutoCall
import my.ktbot.annotation.Qualifier
import my.ktbot.annotation.RegexAnn
import my.ktbot.utils.callor.AutoCallor
import my.ktbot.utils.callor.Caller
import my.ktbot.utils.callor.ObjectMap

suspend fun main() {
	ObjectMap.global
		.set(String::class.java, "ss")
		.set("val", "val")
	AutoCallor.add(TestPlug)
	ObjectMap.global[Caller::class, "fun"]!!.invoke()
}

interface I1

@Suppress("unused", "RedundantSuspendModifier")
object TestPlug : I1 {
	@AutoCall("fun", RegexAnn(""), 0.0)
	suspend fun suspendfunction(str: String, @Qualifier("val") s: String) {
		println("suspendfunction,$str,$s")
	}
}
