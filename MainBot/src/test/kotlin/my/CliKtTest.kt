package my

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.runBlocking

/**
 * @author bin
 * @since 2022/11/22
 */
object CliKtTest {
	@JvmStatic
	fun main(args: Array<String>) {
		val subcommands = A().subcommands(B(), C())
		runBlocking {
			subcommands.main(
				listOf(
					"c",
					"-e",
					"2",
				)
			)
		}
		subcommands.main(
			listOf(
				"c",
				"-e",
				"1",
			)
		)
	}

	class A() : CliktCommand() {
		override fun run() = Unit
	}

	class B() : CliktCommand(help = "B help") {
		override fun run() {
			"B".pl()
		}
	}

	class C() : CliktCommand(help = "C help") {
		private val opt by option("-e", "--explicit")
		override fun run() {
			opt.pl()
		}
	}
}
