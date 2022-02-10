package my.ktbot.plugin.plugs.subPlugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.plugs.CQBotCOC
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

/**
 *
 * @author bin
 * @since 2022/1/7
 */
object COCCheater : Plug(
	name = "骰子:打开全1模式",
	regex = Regex("^[.．。]dall1$", RegexOption.IGNORE_CASE),
	weight = 1.11,
	msgLength = 5..10,
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		CQBotCOC.cheater = !CQBotCOC.cheater
		return ("全1" + (if (CQBotCOC.cheater) "开" else "关")).toPlainText()
	}
}