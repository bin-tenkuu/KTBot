package my.ktbot.ktor.mirai

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.friendgroup.FriendGroups
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext

@OptIn(MiraiInternalApi::class)
@Suppress("UNCHECKED_CAST")
class ServerBot(private val sender: ServerCommandSender) : Bot {
    override val asFriend: Friend get() = TODO("Not yet implemented")
    override val asStranger: Stranger get() = TODO("Not yet implemented")
    override val configuration: BotConfiguration get() = BotConfiguration()
    override val coroutineContext: CoroutineContext get() = ConsoleCommandSender.coroutineContext

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
