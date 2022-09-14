package my

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * @author bin
 * @date 2022/09/13
 */
object Coroutines {
	private fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

	@JvmStatic
	fun main(args: Array<String>) = runBlocking {
		val a = async {
			log("I'm computing part of the answer")
			6
		}
		val b = async {
			log("I'm computing another part of the answer")
			7
		}
		log("The answer is ${a.await() * b.await()}")
	}
}
