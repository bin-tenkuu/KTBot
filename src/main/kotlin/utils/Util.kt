package my.ktbot.plugin.utils

import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.buildMessageChain

/**
 *
 * @author yangqidong
 * @version 1.0.0
 * @date 2021/12/29
 */
suspend inline fun BotEvent.sendAdmin(block: MessageChainBuilder.() -> Unit): MessageReceipt<Friend>? {
    return (bot.getFriend(2938137849) ?: return null).sendMessage(buildMessageChain(block))
}

inline fun <T> MutableIterator<T>.remove(block: T.() -> Unit) {
    block(next())
    remove()
}