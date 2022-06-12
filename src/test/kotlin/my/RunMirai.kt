package my

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

object RunMirai {
	@JvmStatic
	@Suppress("UNUSED_PARAMETER")
	@OptIn(ConsoleExperimentalApi::class)
	fun main(vararg args: String) {
		MiraiConsoleTerminalLoader.startAsDaemon()

		runBlocking {
			MiraiConsole.job.join()
		}
	}
}
