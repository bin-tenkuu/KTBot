package my.ktbot.plugs

import my.ktbot.annotation.Helper
import my.ktbot.annotation.SendAuto
import my.ktbot.database.TCOCShortKey
import my.ktbot.utils.CacheMap
import my.ktbot.utils.DiceResult
import my.ktbot.utils.Sqlite
import my.miraiplus.NeverEvent
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.ktorm.dsl.eq
import org.ktorm.entity.removeIf
import org.ktorm.entity.toList
import kotlin.text.RegexOption.IGNORE_CASE

/**
 *  @since:2022/1/2
 *  @author bin
 */
object CQBotCOC {
    @JvmStatic
    private val diceRegex = Regex("[^+\\-*d\\d#]", IGNORE_CASE)

    @JvmStatic
    private val cache = CacheMap<Long, DiceResult>()

    @JvmStatic
    var cheater: Boolean = false

    @MiraiEventHandle("骰子主功能")
    @RegexAnn("^[.．。]d +(?:(?<times>\\d)#)?(?<dice>[^ ]+)", IGNORE_CASE)
    @Helper("骰子主功能，附带简单表达式计算")
    @SendAuto
    fun invoke(event: MessageEvent, groups: MatchGroupCollection): Message? {
        val times: Int = groups["times"]?.run { value.trim().toIntOrNull() } ?: 1
        var dice: String = groups["dice"]?.value ?: return null

        for (sk in Sqlite[TCOCShortKey]) {
            dice = dice.replace(sk.key, sk.value, true)
        }

        if (diceRegex.matches(dice)) {
            return ".d错误参数".toPlainText()
        }
        val str = Array(times) { dice(dice, event.sender.id) }.joinToString("\n")
        return str.toPlainText()
    }

    @MiraiEventHandle("骰子主功能", eventType = NeverEvent::class)
    @RegexAnn("^[.．。]d +(?:(?<times>\\d)#)?(?<dice>[^ ]+)", IGNORE_CASE)
    fun invoke(groups: MatchGroupCollection): Message? {
        val times: Int = groups["times"]?.run { value.trim().toIntOrNull() } ?: 1
        var dice: String = groups["dice"]?.value ?: return null

        for (sk in Sqlite[TCOCShortKey]) {
            dice = dice.replace(sk.key, sk.value, true)
        }

        if (diceRegex.matches(dice)) {
            return ".d错误参数".toPlainText()
        }
        val str = Array(times) { dice(dice, 0) }.joinToString("\n")
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
        val groups = castStringRegex.matchEntire(origin)?.groups
            ?: return Calc(op = Operator.Add, sum = 0, origin = origin, max = 0)
        val num: Int = groups["num"]?.run { value.toIntOrNull() } ?: 1
        val op = when (groups["op"]?.value) {
            "+" -> Operator.Add
            "-" -> Operator.Sub
            "*" -> Operator.Mul
            else -> Operator.Add
        }
        val max = groups["max"]?.run { value.toIntOrNull() } ?: return Calc(
            op = op,
            sum = num.toLong(),
            origin = num.toString(),
            max = 0
        )
        val dices: DiceResult = if (cheater) DiceResult(num, max)
        else DiceResult(num, max).dice()

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

    @MiraiEventHandle("骰子：打开全1模式")
    @RegexAnn("^[.．。]dall1$", IGNORE_CASE)
    @SendAuto
    @JvmStatic
    private fun cheaterAllOne(): String {
        cheater = !cheater
        return "全1" + if (cheater) "开" else "关"
    }

    @MiraiEventHandle("骰子：简写")
    @RegexAnn("^[.．。]dstat$", IGNORE_CASE)
    @Helper("查看全部简写")
    @SendAuto
    @JvmStatic
    private fun statsMap(): String {
        val list = Sqlite[TCOCShortKey].toList()
        return if (list.isEmpty()) "空"
        else list.joinToString("\n") { sk ->
            "${sk.key}=${sk.value}"
        }
    }

    @MiraiEventHandle("骰子：删除[设置]简写")
    @RegexAnn("""^[.．。]dset +(?<key>\w\w+)(?:=(?<value>[+\-*d\d#]+))?""", IGNORE_CASE)
    @Helper("删除[设置]简写")
    @SendAuto
    @JvmStatic
    private fun statsSet(groups: MatchGroupCollection): Message {
        val key = groups["key"]?.value
        val value = groups["value"]?.value
        if (key === null || key.length < 2) {
            return "key格式错误或长度小于2".toPlainText()
        }
        if (value === null) {
            Sqlite[TCOCShortKey].removeIf { it.key eq key }
            return "删除key:${key}".toPlainText()
        }
        if (value.length > 10) {
            return "value长度不大于10".toPlainText()
        }
        Sqlite.insertOrUpdate(TCOCShortKey) {
            set(it.key, key)
            set(it.value, value)
        }
        return "添加key:${key}=${value}".toPlainText()
    }

    @MiraiEventHandle("骰子：加骰")
    @RegexAnn("^[.．。]dp(?<num> ?\\d*)", IGNORE_CASE)
    @Helper("10分钟之内加投骰")
    @SendAuto
    @JvmStatic
    private fun addedDice(event: MessageEvent, groups: MatchGroupCollection): String {
        val num = groups["num"]?.run { value.trim().toIntOrNull() } ?: 1
        var cacheResult: DiceResult = cache[event.sender.id] ?: return "10分钟之内没有投任何骰子"
        val dice = DiceResult(num, cacheResult.max)
        if (!cheater) dice.dice()
        cacheResult += dice
        cache[event.sender.id] = cacheResult
        return """${dice.origin}：[${dice.list.joinToString(", ")}]=${dice.sum}
			|[${cacheResult.list.joinToString(", ")}]
		""".trimMargin()
    }

    @MiraiEventHandle("骰子：特殊模式")
    @RegexAnn("^[.．。]d(?<operator>bug|(?:wr|cb|aj)f?)$", IGNORE_CASE)
    @Helper("打开/修改/关闭特殊模式")
    @SendAuto
    @JvmStatic
    private fun setSpecial(groups: MatchGroupCollection): String? {
        val operator = groups["operator"]?.value ?: return null
        return if (operator == "bug") {
            specialEffects = Effects.bug
            "进入默认状态"
        } else try {
            specialEffects = Effects.valueOf(operator)
            "进入${specialEffects.state}状态"
        } catch (e: IllegalArgumentException) {
            "未知状态:$operator"
        }
    }

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

}
