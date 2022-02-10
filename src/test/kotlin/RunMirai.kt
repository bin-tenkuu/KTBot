@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "unused")

package org.example.mirai.plugin

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

@OptIn(ConsoleExperimentalApi::class)
suspend fun main() {
	MiraiConsoleTerminalLoader.startAsDaemon()

	MiraiConsole.job.join()
}
