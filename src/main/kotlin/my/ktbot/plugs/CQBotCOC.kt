package my.ktbot.plugs

import my.ktbot.annotation.AutoCall
import my.ktbot.annotation.MsgLength
import my.ktbot.annotation.RegexAnn
import my.ktbot.database.COCShortKey
import my.ktbot.database.TCOCShortKey
import my.ktbot.interfaces.Plug
import my.ktbot.utils.CacheMap
import my.ktbot.utils.DiceResult
import my.ktbot.utils.sqlite.Sqlite
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.removeIf
import org.ktorm.entity.toList
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
	msgLength = 4..500
) {
	@JvmStatic
	private val diceRegex = Regex("[^+\\-*d\\d#]", IGNORE_CASE)

	@JvmStatic
	private val cache = CacheMap<Long, DiceResult>()

	@JvmStatic
	var cheater: Boolean = false
	override suspend fun invoke(event: MessageEvent, result: MatchResult): Message? {
		val times: Int = result["times"]?.run { value.trim().toIntOrNull() } ?: 1
		var dice: String = result["dice"]?.value ?: return null

		for (sk in Sqlite[TCOCShortKey]) {
			dice = dice.replace(sk.key, sk.value, true)
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
		return foldRight(0L to 1L) { c, arr -> c.op(arr, c.sum) }.first
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
		val max = result["max"]?.run { value.toIntOrNull() } ?: return Calc(
			op = op,
			sum = num.toLong(),
			origin = num.toString(),
			max = 0
		)
		val dices: DiceResult = when (cheater) {
			true -> DiceResult(num, max)
			false -> DiceResult.dice(num, max)
		}
		return Calc(op = op, sum = dices.sum, list = dices.list, max = dices.max, origin = dices.origin)
	}

	private class Calc(
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

	private enum class Operator(private val s: String) {
		Add("+") {
			override fun invoke(sc: Pair<Long, Long>, num: Long): Pair<Long, Long> = (sc.first + num * sc.second) to 1
		},
		Sub("-") {
			override fun invoke(sc: Pair<Long, Long>, num: Long): Pair<Long, Long> = (sc.first - num * sc.second) to 1
		},
		Mul("*") {
			override fun invoke(sc: Pair<Long, Long>, num: Long): Pair<Long, Long> = sc.first to (sc.second * num)
		},
		;

		override fun toString(): String = s
		abstract operator fun invoke(sc: Pair<Long, Long>, num: Long): Pair<Long, Long>
	}

	@JvmStatic
	private var specialEffects: Effects = Effects.bug

	@Suppress("EnumEntryName", "unused")
	private enum class Effects(val state: String) {
		bug("默认") {
			override fun invoke(calc: Calc) {}
		},
		wrf("温柔f") {
			override fun invoke(calc: Calc) {
				calc.list?.also {
					if (it.size > 2 && it[0] == it[1]) {
						++it[1]
						calc.state = "[温柔]"
					}
				}
			}
		},
		cbf("残暴f") {
			override fun invoke(calc: Calc) {
				calc.list?.also {
					if (it.size > 2) {
						it[1] = it[0]
						calc.state = "[残暴]"
					}
				}
			}
		},
		ajf("傲娇f") {
			override fun invoke(calc: Calc) = if (Math.random() < 0.5) wrf(calc) else cbf(calc)
		},
		wr("温柔") {
			override fun invoke(calc: Calc) = if (Math.random() < 0.5) wrf(calc) else bug(calc)
		},
		cb("残暴") {
			override fun invoke(calc: Calc) = if (Math.random() < 0.5) cbf(calc) else bug(calc)
		},
		aj("傲娇") {
			override fun invoke(calc: Calc) = arrayOf(wrf, cbf, bug).random()(calc)
		},
		;

		abstract operator fun invoke(calc: Calc)
	}

	@AutoCall(
		name = "骰子:打开全1模式",
		regex = RegexAnn("^[.．。]dall1$", IGNORE_CASE),
		weight = 1.11,
		msgLength = MsgLength(5, 10),
	)
	private fun cheaterAllOne(): String {
		cheater = !cheater
		return "全1" + if (cheater) "开" else "关"
	}

	@AutoCall(
		name = "骰子：简写",
		regex = RegexAnn("^[.．。]dstat$", IGNORE_CASE),
		weight = 1.01,
		help = "查看全部简写",
		msgLength = MsgLength(5, 7),
	)
	private fun statsMap(): String {
		val list = Sqlite[TCOCShortKey].toList()
		return if (list.isEmpty()) "空"
		else list.joinToString("\n") { sk ->
			"${sk.key}=${sk.value}"
		}
	}

	@AutoCall(
		name = "骰子：删除[设置]简写",
		regex = RegexAnn("^[.．。]dset +(?<key>\\w[\\w\\d]+)(?:=(?<value>[+\\-*d\\d#]+))?", IGNORE_CASE),
		weight = 1.02,
		help = "删除[设置]简写",
		msgLength = MsgLength(5, 100),
	)
	private fun statsSet(result: MatchResult): Message {
		val key = result["key"]?.value
		val value = result["value"]?.value
		if (key === null || key.length < 2) {
			return "key格式错误或长度小于2".toPlainText()
		}
		val shortKey = Sqlite[TCOCShortKey]
		if (value === null) {
			shortKey.removeIf { it.key eq key }
			return "删除key:${key}".toPlainText()
		}
		if (value.length > 10) {
			return "value长度不大于10".toPlainText()
		}
		shortKey.add(COCShortKey {
			this.key = key
			this.value = value
		})
		return "添加key:${key}=${value}".toPlainText()
	}

	@AutoCall(
		name = "骰子：加骰",
		regex = RegexAnn("^[.．。]dp(?<num> ?\\d*)", IGNORE_CASE),
		weight = 1.13,
		help = "10分钟之内加投骰",
		msgLength = MsgLength(3, 500)
	)
	private fun addedDice(event: MessageEvent, result: MatchResult): String {
		val num = result["num"]?.run { value.trim().toIntOrNull() } ?: 1
		var cache: DiceResult = CQBotCOC.cache[event.sender.id] ?: return "10分钟之内没有投任何骰子"
		val dice: DiceResult = when (cheater) {
			true -> DiceResult(num, cache.max)
			false -> DiceResult.dice(num, cache.max)
		}
		cache += dice
		CQBotCOC.cache[event.sender.id] = cache
		return """${dice.origin}：[${dice.list.joinToString(", ")}]=${dice.sum}
			|[${cache.list.joinToString(", ")}]
		""".trimMargin()
	}

	@AutoCall(
		name = "骰子：特殊模式",
		regex = RegexAnn("^[.．。]d(?<operator>bug|(?:wr|cb|aj)f?)$", IGNORE_CASE),
		weight = 1.12,
		help = "打开/关闭特殊模式",
		msgLength = MsgLength(2, 10),
	)
	private fun setSpecial(result: MatchResult): String? {
		val operator = result["operator"]?.value ?: return null
		return if (operator == "bug") {
			specialEffects = Effects.bug
			"进入默认状态"
		}
		else try {
			specialEffects = Effects.valueOf(operator)
			"进入${specialEffects.state}状态"
		} catch (e: IllegalArgumentException) {
			"未知状态:$operator"
		}
	}
}
