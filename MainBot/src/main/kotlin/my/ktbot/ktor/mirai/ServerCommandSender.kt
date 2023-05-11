package my.ktbot.ktor.mirai

import my.ktbot.ktor.dao.RoleConfig
import my.ktbot.ktor.dao.RoomConfig
import my.ktbot.ktor.vo.Message.Text
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext

/**
 * @author bin
 * @since 2023/05/06
 */
@Suppress("OverridingDeprecatedMember", "DEPRECATION_ERROR")
class ServerCommandSender(
        private val room: RoomConfig,
        val role: RoleConfig,
) : CommandSender {
    internal val logger = MiraiLogger.Factory.create(ServerCommandSender::class)
    override val bot: Bot = ServerBot(this)
    override val coroutineContext: CoroutineContext get() = ConsoleCommandSender.coroutineContext
    override val permitteeId: PermitteeId get() = AbstractPermitteeId.AnyMember(0)
    override val subject: Contact get() = user
    override val user: User = ServerUser(this)
    val id: Long get() = role.id.toLong()
    override val name: String = role.name

    override suspend fun sendMessage(message: String): MessageReceipt<Contact>? {
        return sendMessage(message, user)
    }

    suspend fun sendMessage(message: String, contect: Contact): Nothing? {
        val text = Text(message)
        room.save(text, -10)
        return null
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Contact>? {
        return sendMessage(message.contentToString(), user)
    }
}
