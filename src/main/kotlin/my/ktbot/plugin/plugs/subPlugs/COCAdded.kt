package my.ktbot.plugin.plugs.subPlugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.plugs.CQBotCOC
import my.ktbot.plugin.utils.DiceResult
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

/**
 *
 * @author bin
 * @since 2022/1/7
 */
object COCAdded : Plug(
	name = "骰子：加骰",
	regex = Regex("^[.．。]dp(?<num> ?\\d*)$", RegexOption.IGNORE_CASE),
	weight = 1.13,
	help = "10分钟之内加投骰".toPlainText(),
	msgLength = 4..10
) {
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message {
		val num = result["num"]?.run{ value.trim().toIntOrNull() } ?: 1
		var cache: DiceResult = CQBotCOC.cache[event.sender.id] ?: return "10分钟之内没有投任何骰子".toPlainText()
		val dice: DiceResult = when (CQBotCOC.cheater) {
			true -> DiceResult(num.toLong(), IntArray(num), cache.max)
			false -> DiceResult.dice(num, cache.max)
		}
		cache += dice
		CQBotCOC.cache[event.sender.id] = cache
		return "${dice.origin}：[${dice.list}]=${dice.sum}\n[${cache.list.joinToString(", ")}]".toPlainText()
	}
}