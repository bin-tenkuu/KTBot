package my.ktbot.command

import my.ktbot.PluginMain
import my.ktbot.PluginPerm
import my.ktbot.utils.SystemInfoUtil
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

/**
 * @author bin
 * @since 2023/05/05
 */
@OptIn(ConsoleExperimentalApi::class)
@Suppress("unused")
object AdminCommand {
    private val owner = PluginMain
    private val parentPermission = PluginPerm.admin
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

}
