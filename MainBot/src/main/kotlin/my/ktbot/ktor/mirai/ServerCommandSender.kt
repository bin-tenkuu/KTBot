package my.ktbot.ktor.mirai

import my.ktbot.ktor.dao.RoomConfig
import my.ktbot.ktor.vo.Message.Text
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext

/**
 * @author bin
 * @since 2023/05/06
 */
class ServerCommandSender(
        private val room: RoomConfig,
        override val name: String
) : CommandSender {
    override val bot: Bot? get() = null
    override val coroutineContext: CoroutineContext get() = ConsoleCommandSender.coroutineContext
    override val permitteeId: PermitteeId get() = AbstractPermitteeId.AnyMember(0)
    override val subject: Contact? get() = null
    override val user: User get() = ServerUser()

    override suspend fun sendMessage(message: String): MessageReceipt<Contact>? {
        val text = Text(message)
        room.save(text, "bot")
        room.sendAll(text)
        return null
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Contact>? {
        val text = Text(message.contentToString())
        room.save(text, "bot")
        room.sendAll(text)
        return null
    }

    private inner class ServerUser : Stranger {
        override val bot: Bot get() = TODO("Not yet implemented")
        override val coroutineContext: CoroutineContext get() = this@ServerCommandSender.coroutineContext
        override val id: Long = room.roles.map { it.key }.indexOf(name).toLong()
        override val nick: String get() = name
        override val remark: String get() = name
        override suspend fun delete() {
        }

        override suspend fun sendMessage(message: Message): MessageReceipt<Stranger> {
            TODO("Not yet implemented")
        }

        override suspend fun uploadImage(resource: ExternalResource): Image {
            TODO("Not yet implemented")
        }

    }
}
