package my.ktbot.command

import my.ktbot.PluginMain
import my.ktbot.PluginPerm
import my.ktbot.database.TCOCShortKey
import my.ktbot.service.CocService
import my.ktbot.utils.DiceResult
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 *  @Date:2023/5/3
 *  @author bin
 *  @version 1.0.0
 */
@OptIn(ConsoleExperimentalApi::class)
@Suppress("unused")
object CocCommand : BaseCommandList(PluginMain, PluginPerm.coc) {

    object D : SubCommand("d", "掷骰子，附带简单计算（+-*），形如 '9#9d9+9'") {
        private val diceRegex = Regex(
                """^(?:(?<times>\d+)#)?(?<dice>[+\-*d\d]+)""",
                RegexOption.IGNORE_CASE
        )

        @Handler
        suspend fun CommandSender.invoke(@Name("表达式") expression: String) {
            val result = diceRegex.find(expression)
            if (result == null) {
                sendMessage(".d 表达式错误, 请检查。")
                return
            }
            val times = result.groups["times"]?.run { value.toIntOrNull() } ?: 1
            var dice = result.groups["dice"]?.value ?: run {
                sendMessage(Array(times) { 0 }.joinToString(", "))
                return
            }
            for ((key, value) in TCOCShortKey.all.entries) {
                dice = dice.replace(key, value, true)
            }

            val str = Array(times) {
                CocService.dice(dice, user?.id ?: 0)
            }.joinToString("\n")
            sendMessage(str)
        }
    }

    object CheaterAllOne : SubCommand("dall1", "骰子：打开全1模式") {
        @Handler
        suspend fun CommandSender.invoke() {
            CocService.cheater = !CocService.cheater
            sendMessage("全1" + if (CocService.cheater) "开" else "关")
        }
    }

    object StatsMap : SubCommand("dstat", "骰子：查看全部简写") {
        @Handler
        suspend fun CommandSender.invoke() {
            if (TCOCShortKey.all.isEmpty()) {
                sendMessage("空")
            } else {
                val message = TCOCShortKey.all.entries.joinToString("\n") {
                    "${it.key} -> ${it.value}"
                }
                sendMessage(message)
            }
        }
    }

    object StatsSet : SubCommand("dset", "骰子：设置简写") {
        @Handler
        suspend fun CommandSender.invoke(@Name("简写") key: String, @Name("全称") value: String) {
            TCOCShortKey.all[key] = value
            sendMessage("设置成功")
        }
    }

    object Dp : SubCommand("dp", "10分钟之内加投骰，只有在上一次.d只有骰子时有效") {

        @Handler
        suspend fun CommandSender.invoke(@Name("数量") num: Int = 1) {
            val id = user?.id
            var cacheResult: DiceResult = CocService.cache[id] ?: run {
                sendMessage("10分钟之内没有投任何骰子")
                return
            }
            val dice = DiceResult(num, cacheResult.max)
            if (!CocService.cheater) dice.dice()
            cacheResult += dice
            CocService.cache[id] = cacheResult
            val msg = """${dice.origin}：[${dice.list.joinToString(", ")}]=${dice.sum}
                |[${cacheResult.list.joinToString(", ")}]
            """.trimMargin()
            sendMessage(msg)
        }
    }

    object SetSpecial : SubCommand("dsp", "骰子：设置特殊骰子") {
        @Handler
        suspend fun CommandSender.invoke(@Name("特殊骰子") special: CocService.Effects) {
            CocService.specialEffects = special
            sendMessage("进入 ${special.state} 状态")
        }
    }

    object R : SubCommand("r", "标准irc骰子") {
        private val diceRegex = Regex("""(?<num>\d*)d(?<max>\d*)""", RegexOption.IGNORE_CASE)

        @Handler
        suspend fun CommandSender.invoke(@Name("骰子") dice: String, @Name("检定") type: String = "") {
            val matchResult = diceRegex.find(dice) ?: run {
                sendMessage("骰子格式错误")
                return
            }
            val groups = matchResult.groups
            val num = groups["num"]!!.value.toIntOrNull() ?: 1
            val max = groups["max"]!!.value.toIntOrNull() ?: 0
            val diceResult = DiceResult(num, max)
            if (!CocService.cheater) diceResult.dice()
            val pre = (user?.nick ?: "") + "进行" + type + "检定：\n"
            val msg = if (num == 1) {
                "${diceResult.origin} = ${diceResult.sum}"
            } else {
                "${diceResult.origin} = ${diceResult.list.joinToString("+")} = ${diceResult.sum}"
            }
            sendMessage(pre + msg)
        }
    }
}
