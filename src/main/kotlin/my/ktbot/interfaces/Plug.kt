package my.ktbot.interfaces

import kotlinx.coroutines.sync.Mutex
import my.ktbot.PlugConfig
import my.ktbot.database.add
import my.ktbot.utils.CacheMap
import my.ktbot.utils.Counter
import my.ktbot.utils.sendAdmin
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
	open val needAdmin: Boolean = false,
	/**帮助文本*/
	open val help: Message? = null,
	/**存在时延时固定时间撤回，单位ms*/
	open val deleteMSG: Long = 0,
	/**存在时启用调用限速，单位ms*/
	open val speedLimit: Long = 0,
	/**私聊经验*/
	open val expPrivate: Double = 0.0,
	/**群聊经验*/
	open val expGroup: Double = 0.0,
	open val msgLength: IntRange = 0..100,
	open val hidden: Boolean = false,
	/**群聊可用*/
	open var canGroup: Boolean = true,
	/**私聊可用*/
	open var canPrivate: Boolean = true,
) : Comparable<Plug> {

	private val lock = Mutex()

	/**是否启用，当为空时表示报错*/
	var isOpen: Boolean? = true
	protected open suspend operator fun invoke(event: MessageEvent, result: MatchResult): Message? {
		return when (event) {
			is FriendMessageEvent -> this(event, result)
			is GroupMessageEvent -> this(event, result)
			else -> null
		}
	}

	protected open suspend operator fun invoke(event: GroupMessageEvent, result: MatchResult): Message? {
		return null
	}

	protected open suspend operator fun invoke(event: FriendMessageEvent, result: MatchResult): Message? {
		return null
	}

	final override fun equals(other: Any?): Boolean {
		return when {
			this === other -> true
			other !is Plug -> false
			name != other.name -> false
			regex != other.regex -> false
			weight != other.weight -> false
			else -> true
		}
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

	protected operator fun MatchResult.get(key: String): MatchGroup? {
		return groups[key]
	}

	protected suspend operator fun invoke(event: MessageEvent): Message? {
		if (!this.lock()) return null
		val result = this[event] ?: run {
			unlock(false)
			return null
		}
		val msg = this(event, result)
		unlock(msg !== null)
		return msg
	}

	// region get,(un)lock
	private fun lock(): Boolean {
		if (speedLimit > 0) {
			when {
				isOpen != true -> return false
				!lock.tryLock(this) -> return false
				isOpen != true -> return false
			}
			isOpen = false
		}
		return true
	}

	private fun unlock(needDelay: Boolean) {
		if (speedLimit <= 0) return
		if (isOpen == false) isOpen = true
		if (needDelay) timer.schedule(speedLimit) { lock.unlock(this@Plug) }
		else lock.unlock(this@Plug)
	}

	private operator fun get(event: MessageEvent): MatchResult? {
		return when {
			event.message.contentToString().length !in msgLength -> null
			needAdmin && !PlugConfig.isAdmin(event) -> null
			Counter.members[event.sender.id].isBaned -> null
			event is GroupMessageEvent && get(event) -> regex.find(event.message.contentToString())
			event is FriendMessageEvent && get(event) -> regex.find(event.message.contentToString())
			else -> null
		}
	}

	private fun get(event: GroupMessageEvent): Boolean {
		return canGroup && !Counter.groups[event.group.id].isBaned && (expGroup == 0.0
			|| Counter.groups[event.group.id].add(expGroup)
			|| Counter.members[event.sender.id].add(expGroup)
			)
	}

	private fun get(event: FriendMessageEvent): Boolean {
		return canPrivate && (expPrivate == 0.0
			|| Counter.members[event.sender.id].add(expPrivate)
			)
	}

	// endregion

	companion object {
		@JvmField
		val logger = MiraiLogger.Factory.create(Plug::class, "Plug")

		@JvmStatic
		private val timer = Timer("Plug", true)

		@JvmStatic
		private val cacheMap = CacheMap<Long, Unit>(Duration.ofMinutes(1).toMillis())

		// region plugs
		@JvmField
		val plugs: MutableList<Plug> = ArrayList()

		@JvmStatic
		private fun addAll(list: List<*>) {
			for (plug in list) {
				if (plug is Plug) plugs.add(plug)
				if (plug is SubPlugs) addAll(plug.subPlugs)
			}
		}

		@JvmName("plusAssign1")
		@JvmStatic
		operator fun plusAssign(list: List<Plug>) {
			addAll(list)
			plugs.sort()
		}

		@JvmStatic
		operator fun plusAssign(list: List<SubPlugs>) {
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
				val msg = plug(event) ?: continue
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
				if (plug.hidden) return null
				return plug
			}
			return null
		}

		suspend operator fun invoke(event: FriendMessageEvent): Plug? {
			for (plug in plugs) {
				val msg = plug(event) ?: continue
				runCatching {
					if (!msg.isContentBlank()) event.sender.sendMessage(msg)
				}.recoverCatching {
					it.printStackTrace()
					logger.error(it)
					event.sendAdmin("${plug.name}调用失败:\n消息${it.message}")
					event.sender.sendMessage("好友消息发送失败：${plug.name}")
				}
				if (plug.hidden) return null
				return plug
			}
			return null
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
		/**帮助文本*/
		override val help: Message? = null,
		override val deleteMSG: Long = 0,
		override val msgLength: IntRange = 0..100,
		private val msg: Message,
	) : Plug(
		name = name,
		regex = regex,
		weight = weight,
	) {
		override suspend fun invoke(event: MessageEvent, result: MatchResult) = msg
	}
}
