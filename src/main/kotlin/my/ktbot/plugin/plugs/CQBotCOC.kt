package my.ktbot.plugin.plugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.database.TCOCShortKey
import my.ktbot.plugin.database.component1
import my.ktbot.plugin.database.component2
import my.ktbot.plugin.utils.*
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import kotlin.text.RegexOption.IGNORE_CASE

/**
 *  @since:2022/1/2
 *  @author bin
 */
object CQBotCOC : Plug(
	name = "骰子主功能",
	regex = Regex("^[.．。]d +(?:(?<times>\\d)#)?(?<dice>[^ ]+)", IGNORE_CASE),
	weight = 1.1,
	help = "骰子主功能，附带简单表达式计算".toPlainText(),
	msgLength = 4..500,
) {
	@JvmStatic
	private val diceRegex = Regex("[^+\\-*d0-9#]", IGNORE_CASE)

	@JvmStatic
	val cache = CacheMap<Long, DiceResult>()

	@JvmStatic
	var cheater: Boolean = false
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
		val times: Int = result["times"]?.run { value.trim().toIntOrNull() } ?: 1
		var dice: String = result["dice"]?.value ?: return null

		for ((k, v) in Sqlite[TCOCShortKey]) {
			dice = dice.replace(k, v, true)
		}

		if (diceRegex.matches(dice)) {
			return ".d错误参数".toPlainText()
		}
		val str = Array(times) { dice(dice, event.sender.id) }.joinToString("\n")
		return str.toPlainText()
	}

	@JvmStatic
	private val splitDiceRegex = Regex("(?=[+\\-*])")

	@JvmStatic
	private fun dice(str: String, qq: Long): String {
		val handles = splitDiceRegex.split(str).map {
			castString(it, this.cheater)
		}
		if (handles.size == 1) {
			val calc: Calc = handles[0]
			return if (calc.list === null) "${calc.op}${calc.origin}=${calc.sum}"
			else {
				this.cache[qq] = DiceResult(calc.sum, calc.list, calc.max)
				specialEffects(calc)
				"${calc.origin}：[${calc.list.joinToString()}]=${calc.sum}${calc.state}"
			}
		}
		val preRet: String = handles.filter {
			it.list !== null
		}.joinToString(separator = "\n") {
			"${it.origin}：[${it.list!!.joinToString()}]=${it.sum}"
		}
		val s = handles.joinToString("") { "${it.op}${it.origin}" }
		return "${preRet}\n${s}=${handles.calculate()}"
	}

	@JvmStatic
	private fun List<Calc>.calculate(): Long {
		return foldRight(0L to 1L) { c, arr -> c.op.func(arr, c.sum) }.first
	}

	@JvmStatic
	private val castStringRegex = Regex("^(?<op>[+\\-*])?(?<num>\\d+)?(?:d(?<max>\\d+))?$", IGNORE_CASE)

	@JvmStatic
	private fun castString(origin: String, cheater: Boolean): Calc {
		val result =
			castStringRegex.matchEntire(origin) ?: return Calc(op = Operator.Add, sum = 0, origin = origin, max = 0)
		val num: Int = result["num"]?.run { value.toIntOrNull() } ?: 1
		val op = when (result["op"]?.value) {
			"+" -> Operator.Add
			"-" -> Operator.Sub
			"*" -> Operator.Mul
			else -> Operator.Add
		}
		val max = result["max"]?.run { value.toIntOrNull() } ?: return Calc(op = op,
			sum = num.toLong(),
			origin = num.toString(),
			max = 0)
		val dices: DiceResult = when (cheater) {
			true -> DiceResult(num, max)
			false -> DiceResult.dice(num, max)
		}
		return Calc(op = op, sum = dices.sum, list = dices.list, max = dices.max, origin = dices.origin)
	}

	class Calc(
		val op: Operator,
		val sum: Long,
		val list: IntArray? = null,
		val origin: String,
		val max: Int,
	) {
		var state: String = ""
			set(v) {
				field = if (v == "") "" else "\n$v"
			}
	}

	enum class Operator(private val s: String, val func: (Pair<Long, Long>, Long) -> Pair<Long, Long>) {
		Add("+", { (first, second), num ->
			(first + num * second) to 1
		}),
		Sub("-", { (first, second), num ->
			(first - num * second) to 1
		}),
		Mul("*", { (first, second), num ->
			first to (second * num)
		});

		override fun toString(): String = s
	}

	@JvmStatic
	var specialEffects: Effects = Effects.bug

	@Suppress("EnumEntryName", "unused")
	enum class Effects(val state: String, private val func: (Calc) -> Unit) {
		bug("默认", { }),
		wrf("温柔f", { c ->
			c.list?.also {
				if (it.size > 2 && it[0] == it[1]) {
					++it[1]
					c.state = "[温柔]"
				}
			}
		}),
		cbf("残暴f", { c ->
			c.list?.also {
				if (it.size > 2) {
					it[1] = it[0]
					c.state = "[残暴]"
				}
			}
		}),
		ajf("傲娇f", { if (Math.random() < 0.5) wrf(it) else cbf(it) }),
		wr("温柔", { if (Math.random() < 0.5) wrf(it) else bug(it) }),
		cb("残暴", { if (Math.random() < 0.5) cbf(it) else bug(it) }),
		aj("傲娇", { arrayOf(wrf, cbf, bug).random()(it) }),
		;

		operator fun invoke(calc: Calc) = func(calc)
	}
}
