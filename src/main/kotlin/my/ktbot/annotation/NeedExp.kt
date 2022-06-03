package my.ktbot.annotation

import my.ktbot.database.Gmt.Companion.add
import my.ktbot.utils.Counter
import my.miraiplus.Caller
import my.miraiplus.ObjectMap
import my.miraiplus.injector.Injector
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent

/**
 *  @Date:2022/6/2
 *  @author bin
 *  @version 1.0.0
 */
annotation class NeedExp(val private: Double, val group: Double) {
	object Inject : Injector.Message<NeedExp> {
		override val weight: Double
			get() = -5.0
		override suspend fun doBefore(ann: NeedExp, event: MessageEvent, tmpMap: ObjectMap, caller: Caller): Boolean {
			return when (event) {
				is FriendMessageEvent -> ann.private > 0
					|| Counter.members[event.sender.id].exp > 0
				is GroupMessageEvent -> ann.group > 0
					|| Counter.groups[event.group.id].exp > 0
					|| Counter.members[event.sender.id].exp > 0
				else -> false
			}
		}

		override suspend fun doAfter(ann: NeedExp, event: MessageEvent, tmpMap: ObjectMap, caller: Caller, result: Any?) {
			when (event) {
				is FriendMessageEvent -> {
					Counter.members[event.sender.id].add(ann.private)
				}
				is GroupMessageEvent -> {
					Counter.groups[event.group.id].add(ann.group)
					Counter.members[event.sender.id].add(ann.group)
				}
				else -> {}
			}
		}
	}
}
