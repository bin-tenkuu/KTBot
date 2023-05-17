package my.ktbot.command

import my.ktbot.PluginMain
import my.ktbot.PluginPerm
import my.ktbot.utils.SystemInfoUtil
import my.ktbot.utils.toMessage
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message

/**
 * @author bin
 * @since 2023/05/05
 */
@OptIn(ConsoleExperimentalApi::class)
@Suppress("unused")
object AdminCommand : BaseCommandList(PluginMain, PluginPerm.admin) {

    object AdminSend : SubCommand("send", "管理员手动发送消息,普通") {
        @Handler
        suspend fun CommandSender.invoke(@Name("目标") target: String, @Name("消息") vararg msgs: Message) {
            val bot = bot
            val msg = msgs.toMessage()
            if (bot == null) {
                println(msg.toString())
                return
            }
            if (msg == null) {
                sendMessage("消息不能为空")
                return
            }
            val contact: Contact? = if (target.startsWith('g', true)) {
                bot.getGroup(target.substring(1).toLong())
            } else {
                bot.getFriend(target.toLong())
            }
            if (contact == null) {
                sendMessage("目标 '$target' 不存在")
                return
            }
            runCatching {
                contact.sendMessage(msg)
            }.onSuccess {
                sendMessage("已发送")
            }.onFailure {
                sendMessage("发送失败")
            }
        }
    }

    object SystemInfo : SubCommand("系统信息", "获取系统信息") {
        @Handler
        suspend fun CommandSender.invoke() {
            sendMessage(SystemInfoUtil())
        }
    }

    object BotList : SubCompositeCommand("botlist", "获取bot列表") {
        @SubCommand("all")
        @Description("查看bot内列表")
        suspend fun CommandSender.all() {
            val bot = bot
            if (bot == null) {
                sendMessage("控制台无指定bot")
                return
            }
            val msg = buildString {
                append("总共 ${bot.groups.size} 个群聊:\n")
                append("总共 ${bot.friends.size} 个好友:\n")
                append("总共 ${bot.strangers.size} 个陌生人:\n")
            }
            sendMessage(msg)
        }
    }
}
