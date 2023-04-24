package my.ktbot.command

import my.ktbot.PlugConfig
import my.ktbot.PluginMain
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandContext
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.EmptyCommandArgumentContext
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message

/**
 * @author bin
 * @since 2023/04/24
 */
@OptIn(ConsoleExperimentalApi::class)
@Suppress("unused")
object HelperCommand {
    val owner = PluginMain
    val parentPermission = PluginMain.parentPermission
    val overrideContext: CommandArgumentContext = EmptyCommandArgumentContext

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

    object PingCommand : SubCommand("ping", "用于测试连通性") {
        @Handler
        suspend fun CommandSender.invoke() {
            sendMessage("pong!")
        }
    }

    object DeveloperCommand : SubCommand("data", "开发者信息") {
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
        suspend fun invoke(context: CommandContext, @Suppress("UNUSED_PARAMETER") @Name("消息") txt: Message) {
            val bot = context.sender.bot
            if (bot == null) {
                println(context.originalMessage.toString())
            } else {
                PlugConfig.getAdmin(bot).sendMessage(context.originalMessage)
            }
        }
    }
}
