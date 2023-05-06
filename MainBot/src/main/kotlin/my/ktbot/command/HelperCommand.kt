package my.ktbot.command

import my.ktbot.PlugConfig
import my.ktbot.PluginMain
import my.ktbot.PluginPerm
import my.ktbot.utils.calculator.Calculator
import my.ktbot.utils.toMessage
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.EmptyCommandArgumentContext
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
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
    private val parentPermission = PluginPerm.common
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
}
