package my.ktbot.plugin.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import my.ktbot.plugin.PluginMain
import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.database.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.ktorm.dsl.eq
import java.time.Duration

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/26
 */
object Counter {
	@JvmStatic
	private val ttl = Duration.ofDays(1).toMillis() shr 1

	@JvmStatic
	val groups = CacheSql(TGroup)

	@JvmStatic
	val members = CacheSql(TMembers)

	@JvmStatic
	private val plugMap = object {
		operator fun component1(): MutableMap<Long, PlugMap> = groups
		operator fun component2(): MutableMap<Long, PlugMap> = members

		val groups: MutableMap<Long, PlugMap> = mutableMapOf()
		val members: MutableMap<Long, PlugMap> = mutableMapOf()

	}

	private class PlugMap : HashMap<Plug, Int>() {
		override operator fun get(key: Plug): Int {
			return getOrDefault(key, 0)
		}

		operator fun plusAssign(key: Plug) {
			put(key, get(key) + 1)
		}
	}

	init {
		PluginMain.launch {
			delay(Duration.ofHours(1).toMillis())
			while (true) {
				try {
					save()
				} catch (e: Exception) {
					Plug.logger.error(e)
				}
				delay(Duration.ofHours(6).toMillis())
			}
		}
	}

	@JvmStatic
	fun save() {
		members.size.also {
			Plug.logger.info("保存开始（Members）：$it")
			members.flash()
			Plug.logger.info("保存结束（Members）：释放数量:${it - members.size},剩余数量:${members.size}")
		}
		groups.size.also {
			Plug.logger.info("保存开始（Groups）：$it")
			groups.flash()
			Plug.logger.info("保存结束（Groups）：总数:${groups.size}")
		}
	}

	@JvmStatic
	fun log(event: MessageEvent, plug: Plug) {
		plugMap.members.getOrPut(event.sender.id) { PlugMap() } += plug
		if (event is GroupEvent) {
			plugMap.groups.getOrPut(event.group.id) { PlugMap() } += plug
		}
	}

	@JvmStatic
	fun state(context: Contact): Message {
		return ForwardMessageBuilder(context, 2).apply {
			val bot = context.bot
			toMSG(plugMap.groups, "群", bot)
			toMSG(plugMap.members, "人", bot)
		}.build()
	}

	@JvmStatic
	fun clear() {
		plugMap.groups.clear()
		plugMap.members.clear()
	}

	private fun ForwardMessageBuilder.toMSG(plugMap: Map<Long, PlugMap>, string: String, contact: UserOrBot) {
		if (plugMap.isNotEmpty()) for ((id, num) in plugMap.map {
			it.key to it.value.values.sum()
		}.sortedByDescending { it.second }) contact says "${string}：${id}：\n${num}次".toPlainText()
		else contact says "${string}：无".toPlainText()
	}

	class CacheSql<E : Gmt<E>, T : TGmt<E>>(private val table: T) {
		private val cacheMap = mutableMapOf<Long, E>()
		operator fun get(id: Long): E {
			var members = cacheMap[id]
			if (members === null) {
				members = Sqlite[table].findOrAdd({ it.id eq id }) { this.id = id }
				cacheMap[id] = members
			}
			return members
		}

		val size get() = cacheMap.size
		operator fun iterator(): MutableIterator<E> = cacheMap.values.iterator()

		fun flash() {
			cacheMap.values.removeIf {
				val g = it.apply { flushChanges() }
				!g.isBaned && System.currentTimeMillis() - g.gmtModified > ttl
			}
		}
	}
}
