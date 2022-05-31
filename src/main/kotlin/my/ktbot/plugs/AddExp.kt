package my.ktbot.plugs

import my.ktbot.database.Gmt.Companion.add
import my.ktbot.utils.Counter
import my.miraiplus.annotation.MessageHandle
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/10
 */
object AddExp {
	@MessageHandle(priority = EventPriority.LOWEST)
	@JvmStatic
	fun invoke(event: GroupMessageEvent) {
		Counter.groups[event.group.id].add(1.0)
		Counter.members[event.sender.id].add(1.0)
	}

	@MessageHandle(priority = EventPriority.LOWEST)
	@JvmStatic
	fun invoke(event: FriendMessageEvent) {
		Counter.members[event.sender.id].add(0.5)
	}
}
