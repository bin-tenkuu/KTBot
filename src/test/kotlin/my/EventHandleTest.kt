package my

import kotlinx.coroutines.delay
import my.ktbot.PluginMain
import my.miraiplus.MyEventHandle

/**
 *  @Date:2022/6/4
 *  @author bin
 *  @version 1.0.0
 */
object EventHandleTest : Print {
	@JvmStatic
	fun main(args: Array<String>) {
		MyEventHandle(PluginMain) + object {
			private val a: Int = 0
			private val b: Int get() = 1
			private val String.c: Int get() = 2
			private fun d() {}
			private suspend fun e() = delay(1)
		}
	}
}
