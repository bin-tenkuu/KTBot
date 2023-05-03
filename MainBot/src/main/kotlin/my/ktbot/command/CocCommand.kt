package my.ktbot.command

import my.ktbot.PluginMain
import my.ktbot.database.TCOCShortKey
import my.ktbot.service.CocService
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.EmptyCommandArgumentContext
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 *  @Date:2023/5/3
 *  @author bin
 *  @version 1.0.0
 */
@OptIn(ConsoleExperimentalApi::class)
@Suppress("unused")
object CocCommand {
    private val owner = PluginMain
    private val parentPermission = PluginMain.parentPermission
    private val overrideContext: CommandArgumentContext = EmptyCommandArgumentContext

    val all: Array<out Command> by lazy {
        this::class.nestedClasses.mapNotNull {
            it.objectInstance as? Command
        }.toTypedArray()
    }

    private open class SubCommand(
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

    private object D : SubCommand("d", "掷骰子，附带简单计算（+-*），形如 '9#9d9+9'") {
        private val diceRegex = Regex(
                "^(?:(?<times>\\d+)#)?(?<dice>[^+\\-*d\\d])",
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

    private object CheaterAllOne : SubCommand("dall1", "骰子：打开全1模式") {
        @Handler
        suspend fun CommandSender.invoke() {
            CocService.cheater = !CocService.cheater
            sendMessage("全1" + if (CocService.cheater) "开" else "关")
        }
    }

    private object StatsMap : SubCommand("dstat", "骰子：查看全部简写") {
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

    private object StatsSet : SubCommand("dset", "骰子：设置简写") {
        @Handler
        suspend fun CommandSender.invoke(@Name("简写") key: String, @Name("全称") value: String) {
            TCOCShortKey.all[key] = value
            sendMessage("设置成功")
        }
    }
}
