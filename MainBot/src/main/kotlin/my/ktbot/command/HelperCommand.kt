package my.ktbot.command

import my.ktbot.PlugConfig
import my.ktbot.PluginMain
import my.ktbot.utils.SystemInfoUtil
import my.ktbot.utils.calculator.Calculator
import my.ktbot.utils.toMessage
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.EmptyCommandArgumentContext
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

/**
 * @author bin
 * @since 2023/04/24
 */
@OptIn(ConsoleExperimentalApi::class)
@Suppress("unused")
object HelperCommand {
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

    private object Ping : SubCommand("ping", "用于测试连通性") {
        @Handler
        suspend fun CommandSender.invoke() {
            sendMessage("pong!")
        }
    }

    private object Developer : SubCommand("data", "开发者信息") {
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

    private object SendToAdmin : SubCommand("report", "发送消息给管理员") {
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

    private object AdminSend : SubCommand("send", "管理员手动发送消息,普通") {
        @Handler
        suspend fun CommandSender.invoke(@Name("目标") target: String, @Name("消息") vararg msgs: Message) {
            val bot = bot
            val msg = msgs.toMessage()
            if (bot == null) {
                println(msg.toString())
            } else if (msg == null) {
                sendMessage("消息不能为空")
            } else {
                val contact: Contact? = if (target.startsWith('g', true)) {
                    bot.getGroup(target.substring(1).toLong())
                } else {
                    bot.getFriend(target.toLong())
                }
                if (contact == null) {
                    sendMessage("目标 '$target' 不存在")
                } else {
                    runCatching {
                        contact.sendMessage(msg)
                    }.onSuccess {
                        sendMessage("已发送")
                    }.onFailure {
                        sendMessage("发送失败")
                    }
                }
            }
        }
    }

    private object SystemInfo : SubCommand("系统信息", "获取系统信息") {
        @Handler
        suspend fun CommandSender.invoke() {
            sendMessage(SystemInfoUtil())
        }
    }

    private object Calc : SubCommand("calc", "简易计算器,表达式间不允许出现空格") {
        @Handler
        suspend fun CommandSender.invoke(@Name("表达式") expr: String) {
            try {
                sendMessage("结果为${Calculator(expr).v}".toPlainText())
            } catch (e: Exception) {
                sendMessage("表达式错误：${e.message}".toPlainText())
            }
        }
    }
}
