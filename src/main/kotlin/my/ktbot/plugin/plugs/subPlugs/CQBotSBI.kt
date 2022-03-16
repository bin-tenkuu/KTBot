package my.ktbot.plugin.plugs.subPlugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.plugs.CQBotCOC
import my.ktbot.plugin.utils.CacheMap
import my.ktbot.plugin.utils.DiceResult
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

object CQBotSBI : Plug(
	name = "骰子：SBI特化功能",
	regex = Regex("^[.．。]s +(?<num>\\d*)d(?<max>\\d*)", RegexOption.IGNORE_CASE),
	weight = 1.1,
	help = "SBI骰子主功能".toPlainText(),
	msgLength = 4..500,
) {

	@JvmStatic
	val cache = CacheMap<Long, DiceResult>()

	@JvmStatic
	val cheater: Boolean
		get() = CQBotCOC.cheater

	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
		val num = (result["num"]?.value?.toIntOrNull() ?: return null).coerceAtLeast(3)
		val max = result["max"]?.value?.toIntOrNull() ?: return null
		val diceResult = when (cheater) {
			true -> DiceResult(num, max)
			false -> DiceResult.dice(num, max)
		}
		cache[event.sender.id] = diceResult
		return "${diceResult.origin}：[${diceResult.list.joinToString()}]（${getRes(diceResult.list)}）".toPlainText()
	}

	fun getRes(list: IntArray): String {
		if (list[0] == list[1] && list[0] == list[2]) {
			return "大失败"
		}
		if (list[0] + list[1] + list[2] == 6) {
			return "大成功，成功度${list.count(1::equals)}"
		}
		val arr = intArrayOf(-2, 0)
		for (i in list.toMutableSet().toIntArray().apply(IntArray::sort)) {
			if (i - arr[0] == 1) {
				if (arr[1] == 1) return "成功，成功度${list.count(1::equals)}"
				else arr[1] = 1
			}
			else arr[1] = 0
			arr[0] = i
		}
		return "失败"
	}
}