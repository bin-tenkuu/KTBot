package my.ktbot.plugs

import my.ktbot.annotation.AutoSend
import my.ktbot.annotation.Helper
import my.ktbot.utils.CacheMap
import my.ktbot.utils.DiceResult
import my.ktbot.utils.get
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

object CQBotSBI {

	@JvmStatic
	val cache = CacheMap<Long, DiceResult>()

	@JvmStatic
	val cheater: Boolean get() = CQBotCOC.cheater

	@MessageHandle("骰子：SBI特化功能")
	@RegexAnn("^[.．。]s +(?<num>\\d*)d(?<max>\\d*)", RegexOption.IGNORE_CASE)
	@Helper("SBI骰子主功能")
	@AutoSend
	@JvmStatic
	private fun invoke(event: MessageEvent, result: MatchResult): Message? {
		val num = (result["num"]?.value?.toIntOrNull() ?: return null).coerceAtLeast(3)
		val max = result["max"]?.value?.toIntOrNull() ?: return null
		val diceResult = when (cheater) {
			true -> DiceResult(num, max)
			false -> DiceResult.dice(num, max)
		}
		cache[event.sender.id] = diceResult
		return "${diceResult.origin}：[${diceResult.list.joinToString()}]（${getRes(diceResult.list)}）".toPlainText()
	}

	private fun getRes(list: IntArray): String {
		if (list.size < 3) return "数量过少"
		setOf(list[0], list[1], list[2]).sorted().apply {
			if (size == 1) {
				return "大失败"
			}
			if (size == 3 && sum() == 6) {
				return "大成功，成功度${list.count(1::equals)}"
			}
		}
		val intArray = list.toSortedSet().toIntArray()
		val arr = intArrayOf(intArray[0], 0)
		for (i in intArray) {
			if (i - arr[0] == 1) {
				if (arr[1] == 1) return "成功，成功度${list.count(1::equals)}"
				else arr[1] = 1
			}
			else arr[1] = 0
			arr[0] = i
		}
		return "失败"
	}

	@MessageHandle("骰子：SBI加骰")
	@RegexAnn("^[.．。]sp(?<num> ?\\d*)", RegexOption.IGNORE_CASE)
	@Helper("10分钟之内加投骰")
	@AutoSend
	@JvmStatic
	private fun addedDice(event: MessageEvent, result: MatchResult): String {
		val num = result["num"]?.run { value.trim().toIntOrNull() } ?: 1
		var diceResult: DiceResult = cache[event.sender.id] ?: return "10分钟之内没有投任何骰子"
		val dice: DiceResult = when (cheater) {
			true -> DiceResult(num, diceResult.max)
			false -> DiceResult.dice(num, diceResult.max)
		}
		diceResult += dice
		cache[event.sender.id] = diceResult
		return """${dice.origin}：[${dice.list.joinToString(", ")}]=${dice.sum}
			|[${diceResult.list.joinToString(", ")}]（${getRes(diceResult.list)}）
		""".trimMargin()
	}
}
