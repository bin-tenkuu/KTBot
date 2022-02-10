package my.ktbot.plugin.plugs.subPlugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.plugs.CQBotPicture
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/12
 */
object SeTuSet : Plug(
	name = ".色图失败列表",
	regex = Regex("^[.．。]色图失败列表$"),
	weight = 3.0,
	needAdmin = true
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		return CQBotPicture.setuSet.joinToString().toPlainText()
	}
}