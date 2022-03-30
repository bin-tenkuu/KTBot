package my.ktbot.plugin.annotation

import kotlinx.coroutines.sync.Mutex
import my.ktbot.plugin.PlugConfig
import my.ktbot.plugin.database.add
import my.ktbot.plugin.utils.*
import net.mamoe.mirai.contact.SendMessageFailedException
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentBlank
import net.mamoe.mirai.utils.MiraiLogger
import java.time.Duration
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.abs

/**
 *  @since:2022/1/2
 *  @author bin
 */
abstract class Plug(
	/**名称*/
	val name: String,
	/**正则匹配*/
	val regex: Regex,
	/**权重*/
	val weight: Double,
//	/**是否为合并转发消息*/
//	val forward: Boolean = false,
	/**是否需要管理员*/
	val needAdmin: Boolean = false,
	/**帮助文本*/
	val help: Message? = null,
	/**存在时延时固定时间撤回，单位ms*/
	val deleteMSG: Long = 0,
	/**存在时启用调用限速，单位ms*/
	val speedLimit: Long = 0,
	/**私聊经验*/
	val expPrivate: Double = 0.0,
	/**群聊经验*/
	val expGroup: Double = 0.0,
	val msgLength: IntRange = 0..100,
	val hidden: Boolean = false,
	/**群聊可用*/
	var canGroup: Boolean = true,
	/**私聊可用*/
	var canPrivate: Boolean = true,
) : Comparable<Plug> {

	private val lock = Mutex()

	/**是否启用，当为空时表示报错*/
	var isOpen: Boolean? = true
	protected open suspend operator fun invoke(event: MessageEvent, result: MatchResult): Message? {
		return null
	}

	protected open suspend operator fun invoke(event: GroupMessageEvent, result: MatchResult): Message? {
		return invoke(event as MessageEvent, result)
	}

	protected open suspend operator fun invoke(event: FriendMessageEvent, result: MatchResult): Message? {
		return invoke(event as MessageEvent, result)
	}

	final override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Plug) return false

		if (name != other.name) return false
		if (regex != other.regex) return false
		if (weight != other.weight) return false

		return true
	}

	final override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + regex.hashCode()
		result = 31 * result + weight.hashCode()
		return result
	}

	final override fun compareTo(other: Plug): Int {
		return weight.compareTo(other = other.weight)
	}

	private fun lock(): Boolean {
		if (speedLimit > 0) {
			if (isOpen != true) return false
			if (!lock.tryLock(this)) return false
			if (isOpen != true) return false
			isOpen = false
		}
		return true
	}

	private fun unlock() {
		if (speedLimit > 0) {
			if (isOpen == false) isOpen = true
			lock.unlock(this)
		}
	}

	protected operator fun MatchResult.get(key: String): MatchGroup? {
		return groups[key]
	}

	companion object {
		@JvmStatic
		val logger = MiraiLogger.Factory.create(Plug::class, "Plug")

		@JvmStatic
		private val timer = Timer("Plug", true)

		@JvmStatic
		private val cacheMap = CacheMap<Long, Unit>(Duration.ofMinutes(1).toMillis())

		// region plugs
		@JvmStatic
		val plugs: MutableList<Plug> = mutableListOf()

		@JvmStatic
		private fun addAll(list: List<*>) {
			for (plug in list) {
				if (plug is Plug) plugs.add(plug)
				if (plug is SubPlugs) addAll(plug.subPlugs)
			}
		}

		operator fun plusAssign(list: List<*>) {
			addAll(list)
			plugs.sort()
		}

		// endregion

		// region invoke
		suspend operator fun invoke(event: GroupMessageEvent): Plug? {
			if (cacheMap.contains(event.group.id)) {
				return null
			}
			for (plug in plugs) {
				@Suppress("DuplicatedCode")
				val result = plug[event] ?: continue
				if (!event.addExp(plug)) continue
				if (!plug.lock()) continue
				val msg = plug(event, result)
				if (msg === null) {
					plug.unlock()
					continue
				}
				runCatching {
					if (!msg.isContentBlank()) return@runCatching event.group.sendMessage(msg)
					else return@runCatching null
				}.recoverCatching {
					if (it is SendMessageFailedException && it.reason != SendMessageFailedException.Reason.MESSAGE_TOO_LARGE) {
						cacheMap[event.group.id] = Unit
						return@recoverCatching null
					}
					it.printStackTrace()
					logger.error(it)
					event.sendAdmin("${plug.name}调用失败:\n消息${it.message}")
					event.group.sendMessage("群聊消息发送失败：${plug.name}")
					return@recoverCatching null
				}.getOrNull()?.apply {
					if (!PlugConfig.isAdmin(event) && plug.deleteMSG > 0) recallIn(plug.deleteMSG)
				}
				plug.laterOpen()
				if (plug.hidden) return null
				return plug
			}
			return null
		}

		suspend operator fun invoke(event: FriendMessageEvent): Plug? {
			for (plug in plugs) {
				@Suppress("DuplicatedCode")
				val result = plug[event] ?: continue
				if (!event.addExp(plug)) continue
				if (!plug.lock()) continue
				val msg = plug(event, result)
				if (msg === null) {
					plug.unlock()
					continue
				}
				runCatching {
					if (!msg.isContentBlank()) event.sender.sendMessage(msg)
				}.recoverCatching {
					it.printStackTrace()
					logger.error(it)
					event.sendAdmin("${plug.name}调用失败:\n消息${it.message}")
					event.sender.sendMessage("好友消息发送失败：${plug.name}")
				}
				plug.laterOpen()
				if (plug.hidden) return null
				return plug
			}
			return null
		}

		// endregion

		// region addExp, get, laterOpen
		private fun GroupMessageEvent.addExp(p: Plug): Boolean = abs(p.expGroup) < 0.001
			|| Counter.groups[group.id].add(p.expGroup)
			|| Counter.members[sender.id].add(p.expGroup)

		private fun MessageEvent.addExp(p: Plug): Boolean = abs(p.expPrivate) < 0.001
			|| Counter.members[sender.id].add(p.expPrivate)

		private fun Plug.get(event: MessageEvent): MatchResult? {
			if (isOpen != true) return null
			if (event.message.contentToString().length !in msgLength) return null
			if (needAdmin && !PlugConfig.isAdmin(event)) return null
			if (Counter.members[event.sender.id].isBaned) return null
			return regex.find(event.message.contentToString())
		}

		private operator fun Plug.get(event: GroupMessageEvent): MatchResult? {
			if (!canGroup) return null
			if (Counter.groups[event.group.id].isBaned) return null
			return get(event as MessageEvent)
		}

		private operator fun Plug.get(event: FriendMessageEvent): MatchResult? {
			if (!canPrivate) return null
			return get(event as MessageEvent)
		}

		private fun Plug.laterOpen() {
			if (speedLimit <= 0) unlock()
			else timer.schedule(speedLimit) { unlock() }
		}

		// endregion

	}

	class StringPlug(
		/**名称*/
		name: String,
		/**正则匹配*/
		regex: Regex,
		/**权重*/
		weight: Double,
		/**是否需要管理员*/
		needAdmin: Boolean = false,
		/**帮助文本*/
		help: Message? = null,
		deleteMSG: Long = 0,
		speedLimit: Long = 0,
		expPrivate: Double = 0.0,
		expGroup: Double = 0.0,
		msgLength: IntRange = 0..100,
		hidden: Boolean = false,
		canGroup: Boolean = true,
		canPrivate: Boolean = true,
		private val msg: Message,
	) : Plug(
		name,
		regex,
		weight,
		needAdmin,
		help,
		deleteMSG,
		speedLimit,
		expPrivate,
		expGroup,
		msgLength,
		hidden,
		canGroup,
		canPrivate,
	) {
		override suspend fun invoke(event: MessageEvent, result: MatchResult) = msg
	}
}
