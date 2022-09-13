package my

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * @author bin
 * @date 2022/09/13
 */
object Coroutines {
	@JvmStatic
	fun main(args: Array<String>) = runBlocking {
		val a = async {
			println("I'm computing part of the answer")
			6
		}
		val b = async {
			println("I'm computing another part of the answer")
			7
		}
		println("The answer is ${a.await() * b.await()}")
	}
}
