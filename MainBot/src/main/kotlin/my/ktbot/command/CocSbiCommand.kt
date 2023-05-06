package my.ktbot.command

import my.ktbot.PluginMain
import my.ktbot.PluginPerm
import my.ktbot.service.CocService
import my.ktbot.utils.CacheMap
import my.ktbot.utils.DiceResult
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.EmptyCommandArgumentContext
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * @author bin
 * @since 2023/05/05
 */
@OptIn(ConsoleExperimentalApi::class)
@Suppress("unused")
object CocSbiCommand {
    private val owner = PluginMain
    private val parentPermission = PluginPerm.coc
    private val overrideContext: CommandArgumentContext = EmptyCommandArgumentContext

    val all: Array<out Command> by lazy {
        this::class.nestedClasses.mapNotNull {
            it.objectInstance as? Command
        }.toTypedArray()
    }

    open class SubCommand(
            @ResolveContext(ResolveContext.Kind.COMMAND_NAME) primaryName: String,
            description: String = "no description available",
            @ResolveContext(ResolveContext.Kind.COMMAND_NAME) vararg secondaryNames: String,
    ) : SimpleCommand(
            owner = owner,
            parentPermission = parentPermission,
            primaryName = primaryName,
            secondaryNames = secondaryNames,
            description = description,
            overrideContext = overrideContext
    )

    @JvmStatic
    val cache = CacheMap<Long?, DiceResult>()

    private fun sbiResult(list: IntArray): String {
        if (list.size < 3) {
            return "数量过少"
        }
        setOf(list[0], list[1], list[2]).sorted().apply {
            if (size == 1) {
                return "大失败"
            }
            if (size == 3 && sum() == 6) {
                return "大成功，成功度${list.count { 1 == it }}"
            }
        }
        val intArray = list.toSortedSet().toIntArray()
        val arr = intArrayOf(intArray[0], 0)
        for (i in intArray) {
            if (i - arr[0] == 1) {
                if (arr[1] == 1) {
                    return "成功，成功度${list.count { 1 == it }}"
                } else {
                    arr[1] = 1
                }
            } else {
                arr[1] = 0
            }
            arr[0] = i
        }
        return "失败"
    }

    object S : SubCommand("s", "SBI骰子，如 6d6") {
        private val diceRegex = Regex("""(?<num>\d*)d(?<max>\d*)""", RegexOption.IGNORE_CASE)

        @Handler
        suspend fun CommandSender.invoke(@Name("骰子") dice: String) {
            val matchResult = diceRegex.find(dice) ?: run {
                sendMessage("骰子格式错误")
                return
            }
            val groups = matchResult.groups
            val num = (groups["num"]!!.value.toIntOrNull() ?: 0).coerceAtLeast(3)
            val max = groups["max"]!!.value.toIntOrNull() ?: 0
            val diceResult = DiceResult(num, max)
            if (!CocService.cheater) diceResult.dice()
            cache[user?.id] = diceResult
            val msg = "${diceResult.origin}：[${diceResult.list.joinToString()}]（${sbiResult(diceResult.list)}）"
            sendMessage(msg)
        }
    }

    object STest : SubCommand("stest", "SBI骰子结果处理测试") {
        @Handler
        suspend fun CommandSender.invoke(@Name("数字") vararg dices: Int) {
            sendMessage(sbiResult(dices))
        }
    }

    object Sp : SubCommand("sp", "SBI骰子加骰") {
        @Handler
        suspend fun CommandSender.invoke(@Name("数量") num: Int = 1) {
            val id = user?.id
            var diceResult: DiceResult = cache[id] ?: run {
                sendMessage("10分钟之内没有投任何骰子")
                return
            }
            val dice = DiceResult(num, diceResult.max)
            if (!CocService.cheater) dice.dice()
            diceResult += dice
            cache[id] = diceResult
            sendMessage("""${dice.origin}：[${dice.list.joinToString(", ")}]=${dice.sum}
                |[${diceResult.list.joinToString(", ")}]（${sbiResult(diceResult.list)}）
            """.trimMargin())
        }
    }
}
