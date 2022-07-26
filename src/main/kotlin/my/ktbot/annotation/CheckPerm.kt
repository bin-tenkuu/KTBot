package my.ktbot.annotation

import my.ktbot.database.TPermCheck
import my.ktbot.utils.Sqlite
import my.miraiplus.Caller
import my.miraiplus.ObjectMap
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.injector.Injector
import net.mamoe.mirai.event.events.GroupEvent

object CheckPerm : Injector<MessageHandle, GroupEvent> {
	override val event = GroupEvent::class

	private val map = HashMap<Long, HashSet<String>>()

	init {
		for (check in Sqlite[TPermCheck]) {
			map.getOrPut(check.id) { HashSet() }.add(check.name)
		}
	}

	override suspend fun doBefore(ann: MessageHandle, event: GroupEvent, tmpMap: ObjectMap, caller: Caller): Boolean {
		val set = map[event.group.id] ?: return true
		return caller.name !in set
	}

}


