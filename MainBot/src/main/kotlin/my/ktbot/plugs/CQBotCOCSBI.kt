package my.ktbot.plugs

import my.ktbot.annotation.Helper
import my.ktbot.annotation.SendAuto
import my.ktbot.utils.CacheMap
import my.ktbot.utils.DiceResult
import my.miraiplus.NeverEvent
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.Qualifier
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

object CQBotCOCSBI {

    @JvmStatic
    val cache = CacheMap<Long, DiceResult>()
    val cacheString = CacheMap<String, DiceResult>()

    @JvmStatic
    val cheater: Boolean get() = CQBotCOC.cheater

    @MiraiEventHandle("骰子SBI：主功能")
    @RegexAnn("^[.．。]s +(?<num>\\d*)d(?<max>\\d*)", RegexOption.IGNORE_CASE)
    @Helper("SBI骰子主功能")
    @SendAuto
    @JvmStatic
    private fun invoke(event: MessageEvent, groups: MatchGroupCollection): Message? {
        val num = (groups["num"]?.value?.toIntOrNull() ?: return null).coerceAtLeast(3)
        val max = groups["max"]?.value?.toIntOrNull() ?: return null
        val diceResult = DiceResult(num, max)
        if (!cheater) diceResult.dice()
        cache[event.sender.id] = diceResult
        return "${diceResult.origin}：[${diceResult.list.joinToString()}]（${getRes(diceResult.list)}）".toPlainText()
    }

    @MiraiEventHandle("骰子SBI：主功能", eventType = NeverEvent::class)
    @RegexAnn("^[.．。]s +(?<num>\\d*)d(?<max>\\d*)", RegexOption.IGNORE_CASE)
    @JvmStatic
    private fun invoke(@Qualifier("id") id: String, groups: MatchGroupCollection): Message? {
        val num = (groups["num"]?.value?.toIntOrNull() ?: return null).coerceAtLeast(3)
        val max = groups["max"]?.value?.toIntOrNull() ?: return null
        val diceResult = DiceResult(num, max)
        if (!cheater) diceResult.dice()
        cacheString[id] = diceResult
        return "${diceResult.origin}：[${diceResult.list.joinToString()}]（${getRes(diceResult.list)}）".toPlainText()
    }

    private val split = Regex("[ ,]+")

    @MiraiEventHandle("骰子SBI：结果处理")
    @RegexAnn("^[.．。]stest(?<nums>[\\d ,]+)", RegexOption.IGNORE_CASE)
    @Helper("10分钟之内加投骰")
    @SendAuto
    @JvmStatic
    private fun test(groups: MatchGroupCollection): String? {
        val s = groups["nums"]?.value ?: return null
        return getRes(s.split(split).map { it.toInt() }.toIntArray())
    }

    private fun getRes(list: IntArray): String {
        if (list.size < 3) return "数量过少"
        setOf(list[0], list[1], list[2]).sorted().apply {
            if (size == 1) return "大失败"
            if (size == 3 && sum() == 6) return "大成功，成功度${list.count { 1 == it }}"
        }
        val intArray = list.toSortedSet().toIntArray()
        val arr = intArrayOf(intArray[0], 0)
        for (i in intArray) {
            if (i - arr[0] == 1) {
                if (arr[1] == 1) return "成功，成功度${list.count { 1 == it }}"
                else arr[1] = 1
            } else arr[1] = 0
            arr[0] = i
        }
        return "失败"
    }

    @MiraiEventHandle("骰子SBI：加骰")
    @RegexAnn("^[.．。]sp(?<num> ?\\d*)", RegexOption.IGNORE_CASE)
    @Helper("10分钟之内加投骰")
    @SendAuto
    @JvmStatic
    private fun addedDice(event: MessageEvent, groups: MatchGroupCollection): String {
        val num = groups["num"]?.run { value.trim().toIntOrNull() } ?: 1
        val id = event.sender.id
        var diceResult: DiceResult = cache[id] ?: return "10分钟之内没有投任何骰子"
        val dice = DiceResult(num, diceResult.max)
        if (!cheater) dice.dice()
        diceResult += dice
        cache[id] = diceResult
        return """${dice.origin}：[${dice.list.joinToString(", ")}]=${dice.sum}
			|[${diceResult.list.joinToString(", ")}]（${getRes(diceResult.list)}）
		""".trimMargin()
    }

    @MiraiEventHandle("骰子SBI：加骰", eventType = NeverEvent::class)
    @RegexAnn("^[.．。]sp(?<num> ?\\d*)", RegexOption.IGNORE_CASE)
    @JvmStatic
    private fun addedDice(@Qualifier("id") id: String, groups: MatchGroupCollection): String {
        val num = groups["num"]?.run { value.trim().toIntOrNull() } ?: 1
        var diceResult: DiceResult = cacheString[id] ?: return "10分钟之内没有投任何骰子"
        val dice = DiceResult(num, diceResult.max)
        if (!cheater) dice.dice()
        diceResult += dice
        cacheString[id] = diceResult
        return """${dice.origin}：[${dice.list.joinToString(", ")}]=${dice.sum}
			|[${diceResult.list.joinToString(", ")}]（${getRes(diceResult.list)}）
		""".trimMargin()
    }
}
