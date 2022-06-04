package my

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

object RunMirai {
	@Suppress("UNUSED_PARAMETER")
	@OptIn(ConsoleExperimentalApi::class)
	suspend fun main(vararg args: String) {
		MiraiConsoleTerminalLoader.startAsDaemon()

		MiraiConsole.job.join()
	}
}
