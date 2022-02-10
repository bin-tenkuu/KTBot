package my.ktbot.plugin.utils

import my.ktbot.plugin.PlugConfig
import my.ktbot.plugin.PluginMain
import my.ktbot.plugin.database.Gmt
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.error
import org.ktorm.entity.*
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table

fun <T> T.println(): T {
	println(toString())
	return this
}

@Suppress("UNCHECKED_CAST")
inline fun <E : Entity<E>, T : Table<E>> EntitySequence<E, T>.findOrAdd(
	predicate: (T) -> ColumnDeclaring<Boolean>,
	block: E.() -> Unit,
): E {
	return firstOrNull(predicate) ?: (Entity.create(sourceTable.entityClass!!) as E).also {
		block(it)
		add(it)
	}
}

inline fun <T : Gmt<T>> T.update(block: (T.() -> Unit) = {}) {
	block()
	gmtModified = System.currentTimeMillis()
	flushChanges()
}

/**
 * 给管理员发送消息
 */
suspend fun BotEvent.sendAdmin(msg: String) = sendAdmin(msg.toPlainText())

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

