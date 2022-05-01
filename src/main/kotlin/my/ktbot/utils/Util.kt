package my.ktbot.utils

import my.ktbot.PlugConfig
import my.ktbot.PluginMain
import my.ktbot.database.Gmt
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.error
import org.ktorm.entity.*
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table

fun <T> T.println(): T {
	println(toString())
	return this
}

@Suppress("UNCHECKED_CAST")
fun <E : Entity<E>, T : Table<E>> EntitySequence<E, T>.findOrAdd(
	predicate: (T) -> ColumnDeclaring<Boolean>,
	block: E.() -> Unit,
): E {
	return firstOrNull(predicate) ?: (Entity.create(sourceTable.entityClass!!) as E).also {
		block(it)
		add(it)
	}
}

fun <T : Gmt<T>> T.update(block: (T.() -> Unit) = {}) {
	block()
	gmtModified = System.currentTimeMillis()
	flushChanges()
}

/**
 * 给管理员发送消息
 */
suspend fun BotEvent.sendAdmin(msg: CharSequence) = sendAdmin(PlainText(msg))

/**
 * 给管理员发送消息
 */
suspend fun BotEvent.sendAdmin(msg: Message) {
	try {
		PlugConfig.getAdmin(bot).sendMessage(msg)
	} catch (e: Exception) {
		PluginMain.logger.error({ "管理员消息发送失败" }, e)
	}
}

fun <T : Any?> T.toMassage(): Message? {
	return when (this) {
		null -> null
		Unit -> null
		is Message -> this
		is CharSequence -> PlainText(this)
		is Array<*> -> buildMessageChain {
			for (any in this@toMassage) any.toMassage()?.unaryPlus()
		}
		is Iterable<*> -> buildMessageChain {
			for (any in this@toMassage) any.toMassage()?.unaryPlus()
		}
		else -> PlainText(this.toString())
	}
}
