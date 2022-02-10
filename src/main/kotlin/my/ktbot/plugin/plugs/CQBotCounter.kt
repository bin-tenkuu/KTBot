package my.ktbot.plugin.plugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.utils.Counter
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/13
 */
object CQBotCounter : Plug(
	name = "日志",
	regex = Regex("^[.．。]日志$"),
	weight = 10.0,
	needAdmin = true,
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		return Counter.state(event.subject)
	}
}