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
object COCSpecial : Plug(
	name = "骰子：特殊模式",
	regex = Regex("^[.．。]d(?<operator>bug|(?:wr|cb|aj)f?)$", RegexOption.IGNORE_CASE),
	weight = 1.12,
	help = "打开/关闭特殊模式".toPlainText(),
	msgLength = 2..10,
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
		val operator = result["operator"]?.value ?: return null
		if (operator == "bug") {
			CQBotCOC.specialEffects = CQBotCOC.Effects.bug
			return "进入默认状态".toPlainText()
		}
		return try {
			CQBotCOC.specialEffects = CQBotCOC.Effects.valueOf(operator)
			"进入${CQBotCOC.specialEffects.state}状态".toPlainText()
		} catch (e: Exception) {
			"未知状态".toPlainText()
		}
	}
}