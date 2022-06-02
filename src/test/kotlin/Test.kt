
import net.mamoe.mirai.event.Event

object Test {
	@JvmStatic
	fun main(args: Array<String>) {
		fun <T : Any?> T.p() = println(this)
		val k = Event::class
	}

}
