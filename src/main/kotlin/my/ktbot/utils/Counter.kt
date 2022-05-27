package my.ktbot.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import my.ktbot.PluginMain
import my.ktbot.database.*
import my.ktbot.interfaces.Plug
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Message
import org.ktorm.dsl.eq
import java.time.Duration

/**
 * 统计类
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

	/**
	 * 群聊事件统计
	 */
	private val groupMap = HashMap<Long, HashMap<Long, Int>>()

	/**
	 * 私聊事件统计
	 */
	private val memberMap = HashMap<Long, Int>()

	init {
		PluginMain.launch {
			delay(Duration.ofHours(1).toMillis())
			while (true) {
				try {
					save()
				} catch (e: Exception) {
					Plug.logger.error(e)
				}
				System.gc()
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
			Plug.logger.info("保存结束（Groups）：释放数量:${it - groups.size},剩余数量:${groups.size}")
		}
	}

	@JvmStatic
	fun log(event: MessageEvent) {
		operator fun HashMap<Long, Int>.invoke(key: Long) = set(key, getOrDefault(key, 0) + 1)
		if (event !is GroupEvent) memberMap(event.sender.id)
		else groupMap.getOrPut(event.group.id) { HashMap() }(event.sender.id)
	}

	@JvmStatic
	fun state(context: Contact): Message {
		if (groupMap.isEmpty() && memberMap.isEmpty()) {
			return EmptyMessageChain
		}
		return ForwardMessageBuilder(context, 2).apply {
			val bot = context.bot
			fun StringBuilder.saysTo(bot: Bot) {
				bot.says(toString())
				clear()
			}

			fun Bot.says(pre: String, list: List<String>) {
				val sb = StringBuilder(pre)
				var i = 0
				for (msg in list) {
					i++
					sb.appendLine(msg)
					if (i >= 10) {
						i = 0
						sb.saysTo(this)
					}
				}
				if (sb.isNotEmpty()) sb.saysTo(this)
			}

			fun Map<Long, Int>.toMSG() = map { (id, num) -> "${id}：${num}次" }
			for ((group, list) in groupMap) {
				bot.says("群($group)：", list.toMSG())
			}
			bot.says("个人：", memberMap.toMSG())
		}.build()
	}

	@JvmStatic
	fun clear() {
		groupMap.clear()
		memberMap.clear()
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
