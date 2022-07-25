@file:JvmName("Util")

package my.ktbot.utils

import my.ktbot.PlugConfig
import my.ktbot.PluginMain
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.error

operator fun MatchResult.get(key: String): MatchGroup? {
	return groups[key]
}

/**
 * 给管理员发送消息
 */
suspend fun BotEvent.sendAdmin(msg: Message) {
	try {
		PlugConfig.getAdmin(bot).sendMessage(msg)
	}
	catch (e: Exception) {
		PluginMain.logger.error({ "管理员消息发送失败" }, e)
	}
}

fun Any?.toMessage(): Message? {
	return when (this) {
		null -> null
		Unit -> null
		is Message -> this
		is CharSequence -> if (isEmpty()) emptyMessageChain() else PlainText(this)
		is Array<*> -> buildMessageChain {
			addAll(this@toMessage.mapNotNull(Any?::toMessage))
		}
		is Iterable<*> -> buildMessageChain {
			addAll(this@toMessage.mapNotNull(Any?::toMessage))
		}
		else -> PlainText(toString())
	}
}

inline fun <reified T> createLogger(identity: String? = null) = MiraiLogger.Factory.create(T::class, identity)
