package my

import kotlinx.coroutines.runBlocking
import my.ktbot.utils.KtorUtils

object Test : Print {
	@JvmStatic
	fun main(args: Array<String>) {
		runBlocking {
			KtorUtils.greenTea(true).pl()
		}
	}
}
