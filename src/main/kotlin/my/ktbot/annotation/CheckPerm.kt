package my.ktbot.annotation

import my.ktbot.database.PermCheck
import my.ktbot.database.TPermCheck
import my.ktbot.utils.Sqlite
import my.miraiplus.Caller
import my.miraiplus.ObjectMap
import my.miraiplus.annotation.MessageHandle
import my.miraiplus.injector.Injector
import net.mamoe.mirai.event.events.GroupEvent
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.removeIf

object CheckPerm : Injector<MessageHandle, GroupEvent> {
	override val event = GroupEvent::class

	private val map = HashMap<Long, HashSet<String>>()

	init {
		for (check in Sqlite[TPermCheck]) {
			map.getOrPut(check.id) { HashSet() }.add(check.name)
		}
	}

	override suspend fun doBefore(ann: MessageHandle, event: GroupEvent, tmpMap: ObjectMap, caller: Caller): Boolean {
		return check(event.group.id, caller.name)
	}

	/**
	 *
	 * @param group [GroupEvent]
	 * @param name [String] 使用 [Caller.name]
	 * @return [Boolean]
	 */
	fun check(group: Long, name: String): Boolean {
		val set = map[group] ?: return true
		return name !in set
	}

	fun open(group: Long, name: String) {
		val set = map[group] ?: return
		set -= name
		Sqlite[TPermCheck].removeIf { (it.id eq group).and(it.name eq name) }
	}

	fun close(group: Long, name: String) {
		map.getOrPut(group) { HashSet() }.add(name)
		Sqlite[TPermCheck].add(PermCheck {
			this.id = group
			this.name = name
		})
	}
}


