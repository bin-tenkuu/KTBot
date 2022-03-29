package my.ktbot.plugin.plugs

import my.ktbot.plugin.annotation.*
import my.ktbot.plugin.database.add
import my.ktbot.plugin.utils.Counter
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/10
 */
object AddExp : Plug(
	name = "(活跃增长)",
	regex = Regex("^"),
	weight = Double.MAX_VALUE,
	expPrivate = 0.5,
	expGroup = 1.0,
	msgLength = 0..Int.MAX_VALUE
) {
	override suspend fun invoke(event: GroupMessageEvent, result: MatchResult): Message? {
		Counter.groups[event.group.id].add(expGroup)
		Counter.members[event.sender.id].add(expGroup)
		return null
	}

	override suspend fun invoke(event: FriendMessageEvent, result: MatchResult): Message? {
		Counter.members[event.sender.id].add(expPrivate)
		return null
	}
}