package my.ktbot.plugin.plugs

import my.ktbot.plugin.annotation.Plug
import my.ktbot.plugin.utils.CacheMap
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*

/**
 *  @Date:2022/1/8
 *  @author bin
 *  @version 1.0.0
 */
object CQBotRepeat : Plug(
	name = "(复读)",
	regex = Regex("^"),
	weight = 90.0,
	msgLength = 0..50,
	hidden = true,
	canPrivate = false
) {

	@JvmStatic
	private val cache = CacheMap<Long, RepeatCache>()
	override suspend fun invoke(event: GroupMessageEvent, result: MatchResult): Message? {
		val node = cache[event.group.id]
		if (node === null || node.msg != event.message.contentToString()) {
			cache[event.group.id] = RepeatCache(event.message.contentToString()).also {
				it += event.sender.id
			}
			return null
		}
		node += event.sender.id
		if (node.size != 4) return EmptyMessageChain
		val msg: String = event.message.filterIsInstance<PlainText>().joinToString("") {
			it.contentToString()
		}
		if (msg.length < 4) return msg.toPlainText()
		return msg.shuffle().toPlainText()
	}

	@JvmStatic
	private fun String.shuffle(): String {
		return String(toCharArray().apply(CharArray::shuffle))
	}

	private class RepeatCache(val msg: String) {
		val users = mutableSetOf<Long>()
		val size get() = users.size

		operator fun plusAssign(qq: Long) {
			users += qq
		}
	}
}