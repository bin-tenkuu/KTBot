package my.ktbot.command

import my.ktbot.PlugConfig
import my.ktbot.PluginMain
import my.ktbot.PluginPerm
import my.ktbot.utils.calculator.Calculator
import my.ktbot.utils.toMessage
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.Command.Companion.allNames
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.AbstractCommandValueParameter
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import kotlin.reflect.KClass

/**
 * @author bin
 * @since 2023/04/24
 */
@OptIn(ConsoleExperimentalApi::class)
@Suppress("unused")
object HelperCommand : BaseCommandList(PluginMain, PluginPerm.common) {

    object Ping : SubCommand("ping", "用于测试连通性") {
        @Handler
        suspend fun CommandSender.invoke() {
            sendMessage("pong!")
        }
    }

    object Developer : SubCommand("data", "开发者信息") {
        @Handler
        suspend fun CommandSender.invoke() {
            sendMessage(
                    """
                开发者QQ：2938137849
                项目地址github：2938137849/KTBot
                轮子github：mamoe/mirai
                """.trimIndent()
            )
        }
    }

    object SendToAdmin : SubCommand("report", "发送消息给管理员") {
        @Handler
        suspend fun CommandSender.invoke(@Name("消息") vararg msgs: Message) {
            val bot = bot
            val msg = msgs.toMessage()
            if (bot == null) {
                println(msg.toString())
            } else if (msg == null) {
                sendMessage("消息不能为空")
            } else {
                runCatching {
                    PlugConfig.getAdmin(bot).sendMessage(msg)
                }.onSuccess {
                    sendMessage("已发送")
                }.onFailure {
                    sendMessage("发送失败")
                }
            }
        }
    }

    object Calc : SubCommand("calc", "简易计算器,表达式间不允许出现空格") {
        @Handler
        suspend fun CommandSender.invoke(@Name("表达式") expr: String) {
            try {
                sendMessage("结果为${Calculator(expr).v}".toPlainText())
            } catch (e: Exception) {
                sendMessage("表达式错误：${e.message}".toPlainText())
            }
        }
    }

    object HelpCommand : SubCommand("?", "筛选指令并显示略微详细的帮助") {

        @Handler
        suspend fun CommandSender.handle(@Name("指令关键词") keyword: String = "") {
            val message: String
            if (keyword.isBlank()) {
                message = BuiltInCommands.HelpCommand.generateDefaultHelp(permitteeId)
                sendMessage(message)
                return
            }
            val permitteeId = this.permitteeId
            val filter = ArrayList<Command>()
            for (command in CommandManager.allRegisteredCommands) {
                if (!PluginPerm.test(permitteeId, command.permission)) {
                    continue
                }
                if (command.allNames.any { it.equals(keyword, true) }) {
                    filter.clear()
                    filter.add(command)
                    break
                }
                if (command.allNames.any { it.contains(keyword, true) }) {
                    filter.add(command)
                }
            }
            if (filter.isEmpty()) {
                sendMessage("无匹配的指令")
                return
            }
            if (filter.size == 1) {
                val command = filter[0]
                @OptIn(ExperimentalCommandDescriptors::class)
                message = buildString {
                    append(command.usage)
                    appendLine()
                    val secondaryNames = command.secondaryNames
                    if (secondaryNames.isNotEmpty()) {
                        append("别名：")
                        secondaryNames.joinTo(this, ", ")
                        appendLine()
                    }
                }
                sendMessage(message)
                return
            }
            message = filter
                    .joinToString("\n\n") { command ->
                        val lines = command.usage.lines()
                        if (lines.isEmpty()) "/${command.primaryName} ${command.description}"
                        else
                            "◆ " + lines.first() + "\n" + lines.drop(1).joinToString("\n") { "  $it" }
                    }.lines().filterNot(String::isBlank).joinToString("\n")
            sendMessage(message)
        }

        @ExperimentalCommandDescriptors
        private fun <T> AbstractCommandValueParameter<T>.render(): String {
            return when (this) {
                is AbstractCommandValueParameter.Extended,
                is AbstractCommandValueParameter.UserDefinedType<*>,
                -> {
                    val nameToRender = this.name ?: (this.type as KClass<*>).simpleName
                    if (isOptional) "[$nameToRender]" else "<$nameToRender>"
                }
                is AbstractCommandValueParameter.StringConstant -> {
                    this.expectingValue
                }
            }
        }
    }

}
