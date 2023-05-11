package my.ktbot.ktor.mirai

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.UserNudge
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext

class ServerUser(private val sender: ServerCommandSender) : User {
    override val bot: Bot get() = sender.bot
    override val coroutineContext: CoroutineContext get() = sender.coroutineContext
    override val id: Long = sender.id
    override val nick: String get() = sender.name
    override val remark: String get() = sender.name
    override fun nudge(): UserNudge {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Stranger> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        return ServerImage()
    }
}
