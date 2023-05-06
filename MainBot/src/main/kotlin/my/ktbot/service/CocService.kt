package my.ktbot.service

import my.ktbot.utils.CacheMap
import my.ktbot.utils.DiceResult

/**
 *  @Date:2023/5/3
 *  @author bin
 *  @version 1.0.0
 */
object CocService {

    @JvmStatic
    val cache = CacheMap<Long?, DiceResult>()

    @JvmStatic
    var cheater: Boolean = false

    var specialEffects: Effects = Effects.bug

    @JvmStatic
    private val splitDiceRegex = Regex("(?=[+\\-*])")

    @JvmStatic
    fun dice(str: String, qq: Long?): String {
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
    private val castStringRegex = Regex("^(?<op>[+\\-*])?(?<num>\\d+)?(?:d(?<max>\\d+))?$", RegexOption.IGNORE_CASE)

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

    enum class Operator(private val s: String) {
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

    @Suppress("EnumEntryName", "unused")
    enum class Effects(val state: String) {
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
