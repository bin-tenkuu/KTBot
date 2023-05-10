package my.ktbot.ktor.mirai

import my.ktbot.ktor.dao.RoleConfig
import my.ktbot.ktor.dao.RoomConfig
import my.ktbot.ktor.vo.Message.Text
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.friendgroup.FriendGroups
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext

/**
 * @author bin
 * @since 2023/05/06
 */
@OptIn(MiraiInternalApi::class)
class ServerCommandSender(
        private val room: RoomConfig,
        val role: RoleConfig,
) : CommandSender {
    private val logger = MiraiLogger.Factory.create(ServerCommandSender::class)
    override val bot: Bot = ServerBot(this)
    override val coroutineContext: CoroutineContext get() = ConsoleCommandSender.coroutineContext
    override val permitteeId: PermitteeId get() = AbstractPermitteeId.AnyMember(0)
    override val subject: Contact? get() = null
    override val user: User = ServerStranger(this)
    val id: Long get() = role.id.toLong()
    override val name: String = role.name

    override suspend fun sendMessage(message: String): MessageReceipt<Contact>? {
        val text = Text(message)
        room.save(text, -10)
        room.sendAll(text)
        return null
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Contact>? {
        val text = Text(message.contentToString())
        room.save(text, -10)
        room.sendAll(text)
        return null
    }

    private inner class ServerBot(private val sender: ServerCommandSender) : Bot {
        override val asFriend: Friend get() = ServerFriend(sender)
        override val asStranger: Stranger get() = ServerStranger(sender)
        override val configuration: BotConfiguration get() = BotConfiguration()
        override val coroutineContext: CoroutineContext get() = ConsoleCommandSender.coroutineContext

        @Suppress("UNCHECKED_CAST")
        override val eventChannel: EventChannel<BotEvent> get() = GlobalEventChannel as EventChannel<BotEvent>
        override val friendGroups: FriendGroups get() = TODO("Not yet implemented")
        override val friends: ContactList<Friend> get() = ContactList()
        override val groups: ContactList<Group> get() = ContactList()
        override val id: Long get() = -10
        override val isOnline: Boolean get() = true
        override val logger: MiraiLogger get() = sender.logger
        override val nick: String get() = "bot"
        override val otherClients: ContactList<OtherClient> get() = ContactList()
        override val strangers: ContactList<Stranger> get() = ContactList()

        override fun close(cause: Throwable?) {
        }

        override suspend fun login() {
        }
    }

    private abstract class ServerUser<T : User>(private val sender: ServerCommandSender) : User {
        override val bot: Bot get() = sender.bot
        override val coroutineContext: CoroutineContext get() = sender.coroutineContext
        override val id: Long = sender.id
        override val nick: String get() = sender.name
        override val remark: String get() = ""

        suspend fun delete() {
        }

        override suspend fun sendMessage(message: Message): MessageReceipt<T> {
            TODO("Not yet implemented")
        }

        override suspend fun uploadImage(resource: ExternalResource): Image {
            TODO("Not yet implemented")
        }
    }

    private class ServerStranger(sender: ServerCommandSender) : ServerUser<Stranger>(sender), Stranger

    private class ServerFriend(sender: ServerCommandSender) : ServerUser<Friend>(sender), Friend {
        override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio {
            return ServerOfflineAudio()
        }

        override val friendGroup: FriendGroup
            get() = TODO("Not yet implemented")
        override val roamingMessages: RoamingMessages
            get() = TODO("Not yet implemented")
    }

    private class ServerOfflineAudio : OfflineAudio {
        override val codec: AudioCodec get() = AudioCodec.AMR
        override val extraData: ByteArray? get() = null
        override val fileMd5: ByteArray get() = ByteArray(0)
        override val fileSize: Long get() = 0
        override val filename: String get() = ""

        override fun toString(): String = ""
    }

    @Deprecated("")
    private class ServerOutgoingToGroup : OnlineMessageSource.Outgoing.ToGroup() {
        override val bot: Bot
            get() = TODO("Not yet implemented")
        override val ids: IntArray
            get() = TODO("Not yet implemented")
        override val internalIds: IntArray
            get() = TODO("Not yet implemented")
        override val isOriginalMessageInitialized: Boolean
            get() = TODO("Not yet implemented")
        override val originalMessage: MessageChain
            get() = TODO("Not yet implemented")
        override val sender: Bot
            get() = TODO("Not yet implemented")
        override val target: Group
            get() = TODO("Not yet implemented")
        override val time: Int
            get() = TODO("Not yet implemented")

    }

}
